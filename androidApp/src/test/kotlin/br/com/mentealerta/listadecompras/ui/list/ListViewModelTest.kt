package br.com.mentealerta.listadecompras.ui.list

import app.cash.turbine.test
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.catalog.Catalog
import br.com.mentealerta.listadecompras.catalog.CatalogIcon
import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.CatalogProduct
import br.com.mentealerta.listadecompras.catalog.CatalogSection
import br.com.mentealerta.listadecompras.catalog.IconFit
import br.com.mentealerta.listadecompras.domain.ClearListUseCase
import br.com.mentealerta.listadecompras.domain.FinishShoppingUseCase
import br.com.mentealerta.listadecompras.domain.FinishedShopping
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.domain.ListSection
import br.com.mentealerta.listadecompras.domain.ObserveListUseCase
import br.com.mentealerta.listadecompras.domain.Purchase
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.domain.QuantityUnit
import br.com.mentealerta.listadecompras.domain.RemovedItems
import br.com.mentealerta.listadecompras.domain.SetItemCheckedUseCase
import br.com.mentealerta.listadecompras.domain.ShoppingListRepository
import br.com.mentealerta.listadecompras.domain.UndoLastActionUseCase
import br.com.mentealerta.listadecompras.domain.undo.UndoStack
import br.com.mentealerta.listadecompras.domain.undo.UndoableAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModelTest {
    private val produce = CatalogSection("produce", "Hortifrúti", 1, CatalogIcon.Fluent("Leafy green"))
    private val other = CatalogSection(CatalogSection.OTHER_ID, "Outros", 2, CatalogIcon.Fluent("Shopping cart"))
    private val banana = CatalogProduct("banana", "Banana", "produce", 10, CatalogIcon.Fluent("Banana"), IconFit.EXACT)

    private val index = CatalogIndex(
        Catalog(
            sections = listOf(produce, other),
            products = listOf(banana),
            terms = emptyList(),
            replacements = emptyMap(),
            retiredProductIds = emptySet(),
        ),
    )

    private val sections = MutableSharedFlow<List<ListSection>>()

    /** Itens que o repositório falso conhece, para riscar e tirar o risco. */
    private val stored = mutableMapOf<ListItemId, ListItem>()
    private val checkedCalls = mutableListOf<Pair<ListItemId, Boolean>>()
    private val undoStack = UndoStack()

    private val repository = object : ShoppingListRepository {
        override fun observeSections() = sections

        override suspend fun add(item: ListItem) = Unit

        override suspend fun update(item: ListItem): ListItem? = null

        override suspend fun setChecked(id: ListItemId, checked: Boolean): ListItem? {
            checkedCalls += id to checked
            val previous = stored[id]?.takeIf { it.checked != checked } ?: return null
            stored[id] = previous.copy(checked = checked)
            return previous
        }

        override suspend fun remove(id: ListItemId) = RemovedItems.NONE

        override suspend fun clear(): RemovedItems {
            val all = stored.values.toList().takeIf { it.isNotEmpty() } ?: return RemovedItems.NONE
            stored.clear()
            return RemovedItems.of(all.map { it to Instant.fromEpochSeconds(0) })
        }

        override suspend fun restore(removed: RemovedItems) {
            removed.items.forEach { stored[it.id] = it }
        }

        override suspend fun finishShopping(): FinishedShopping? {
            val checked = stored.values.filter { it.checked }.takeIf { it.isNotEmpty() } ?: return null
            checked.forEach { stored.remove(it.id) }
            return FinishedShopping(
                Purchase.create(Instant.fromEpochSeconds(0), checked),
                RemovedItems.of(checked.map { it to Instant.fromEpochSeconds(0) }),
                remaining = stored.size,
            )
        }

        override suspend fun undoFinishShopping(finished: FinishedShopping) = restore(finished.removed)
    }

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = ListViewModel(
        ObserveListUseCase(repository),
        index,
        SetItemCheckedUseCase(repository, undoStack),
        FinishShoppingUseCase(repository, undoStack),
        ClearListUseCase(repository, undoStack),
        UndoLastActionUseCase(repository, undoStack),
    )

    @Test
    fun startsLoadingUntilTheListArrives() = runTest {
        viewModel().uiState.test {
            assertEquals(ListUiState.Loading, awaitItem())
            sections.emit(emptyList())
            assertEquals(ListUiState.Empty, awaitItem())
        }
    }

    @Test
    fun mapsSectionsAndItemsKeepingTheOrder() = runTest {
        val bananas = ListItem(
            id = ListItemId("1"),
            name = "bananas",
            productId = "banana",
            sectionId = "produce",
            quantity = Quantity(6.0),
            checked = true,
        )
        val kale = ListItem(ListItemId("2"), "Couve", null, "produce", Quantity(1.0, QuantityUnit.BUNCH), "mineira")
        val bergamot = ListItem(ListItemId("3"), "Bergamota", null, CatalogSection.OTHER_ID)

        viewModel().uiState.test {
            skipItems(1)
            sections.emit(listOf(ListSection(produce, listOf(bananas, kale)), ListSection(other, listOf(bergamot))))
            assertEquals(
                ListUiState.Content(
                    listOf(
                        SectionUi(
                            "produce",
                            "Hortifrúti",
                            listOf(
                                ListRowUi(
                                    ListItemId("2"),
                                    "Couve",
                                    "mineira",
                                    Quantity(1.0, QuantityUnit.BUNCH),
                                    false,
                                    R.drawable.catalog_fluent_leafy_green,
                                ),
                            ),
                        ),
                        SectionUi(
                            CatalogSection.OTHER_ID,
                            "Outros",
                            listOf(
                                ListRowUi(
                                    ListItemId("3"),
                                    "Bergamota",
                                    icon = R.drawable.catalog_fluent_shopping_cart,
                                ),
                            ),
                        ),
                        // Riscados descem para "Já peguei", no fim (#121).
                        SectionUi.bought(
                            listOf(
                                ListRowUi(
                                    ListItemId("1"),
                                    "bananas",
                                    null,
                                    Quantity(6.0),
                                    true,
                                    R.drawable.catalog_fluent_banana,
                                ),
                            ),
                        ),
                    ),
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun boughtSectionComesLastInSectionOrderAndEmptySectionsDisappear() = runTest {
        val tomato = ListItem(ListItemId("1"), "Tomate", null, "produce", checked = true)
        val kale = ListItem(ListItemId("2"), "Couve", null, "produce", checked = true)
        val bergamot = ListItem(ListItemId("3"), "Bergamota", null, CatalogSection.OTHER_ID)
        val tea = ListItem(ListItemId("4"), "Chá", null, CatalogSection.OTHER_ID, checked = true)

        viewModel().uiState.test {
            skipItems(1)
            sections.emit(listOf(ListSection(produce, listOf(tomato, kale)), ListSection(other, listOf(bergamot, tea))))
            val content = awaitItem() as ListUiState.Content
            assertEquals(listOf(CatalogSection.OTHER_ID, SectionUi.BOUGHT_ID), content.sections.map { it.id })
            assertEquals(listOf("Tomate", "Couve", "Chá"), content.sections.last().rows.map { it.name })
            assertTrue(content.sections.last().bought)
            assertEquals(1, content.remaining)
            assertEquals(4, content.total)
        }
    }

    @Test
    fun withNothingCheckedThereIsNoBoughtSection() = runTest {
        val tomato = ListItem(ListItemId("1"), "Tomate", null, "produce")
        viewModel().uiState.test {
            skipItems(1)
            sections.emit(listOf(ListSection(produce, listOf(tomato))))
            assertEquals(false, (awaitItem() as ListUiState.Content).sections.any { it.bought })
        }
    }

    @Test
    fun emitsAgainWhenTheListChanges() = runTest {
        val tomato = ListItem(ListItemId("1"), "Tomate", null, "produce")
        viewModel().uiState.test {
            skipItems(1)
            sections.emit(listOf(ListSection(produce, listOf(tomato))))
            assertEquals(false, (awaitItem() as ListUiState.Content).sections.single().rows.single().checked)
            sections.emit(listOf(ListSection(produce, listOf(tomato.check()))))
            assertEquals(true, (awaitItem() as ListUiState.Content).sections.single().rows.single().checked)
            sections.emit(emptyList())
            assertEquals(ListUiState.Empty, awaitItem())
        }
    }

    @Test
    fun shoppingStartsWithTheFirstCheckAndCountsWhatIsLeft() = runTest {
        val tomato = ListItem(ListItemId("1"), "Tomate", null, "produce")
        val kale = ListItem(ListItemId("2"), "Couve", null, "produce")
        viewModel().uiState.test {
            skipItems(1)
            sections.emit(listOf(ListSection(produce, listOf(tomato, kale))))
            with(awaitItem() as ListUiState.Content) {
                assertEquals(false, isShopping)
                assertEquals(2, remaining)
            }
            sections.emit(listOf(ListSection(produce, listOf(tomato.check(), kale))))
            with(awaitItem() as ListUiState.Content) {
                assertEquals(true, isShopping)
                assertEquals(1, remaining)
            }
        }
    }

    @Test
    fun togglingChecksAnUncheckedItemAndUnchecksACheckedOne() = runTest {
        val tomato = ListItem(ListItemId("1"), "Tomate", null, "produce")
        stored[tomato.id] = tomato
        val viewModel = viewModel()

        viewModel.onToggleCheck(tomato.id, checked = false)
        assertEquals(true, stored.getValue(tomato.id).checked)
        viewModel.onToggleCheck(tomato.id, checked = true)
        assertEquals(false, stored.getValue(tomato.id).checked)
        assertEquals(listOf(tomato.id to true, tomato.id to false), checkedCalls)
    }

    @Test
    fun checkingCanBeUndoneWithOpsVoltar() = runTest {
        val tomato = ListItem(ListItemId("1"), "Tomate", null, "produce")
        stored[tomato.id] = tomato

        viewModel().onToggleCheck(tomato.id, checked = false)
        assertEquals(UndoableAction.ItemChecked(tomato), undoStack.lastAction.value)

        viewModel().onUndo()
        assertEquals(false, stored.getValue(tomato.id).checked)
        assertNull(undoStack.lastAction.value)
    }

    @Test
    fun finishingShowsHowManyWereBoughtAndHowManyAreLeft() = runTest {
        val tomato = ListItem(ListItemId("1"), "Tomate", null, "produce", checked = true)
        val kale = ListItem(ListItemId("2"), "Couve", null, "produce", checked = true)
        val beans = ListItem(ListItemId("3"), "Feijão", null, "produce")
        listOf(tomato, kale, beans).forEach { stored[it.id] = it }
        val viewModel = viewModel()

        viewModel.onFinish()

        assertEquals(ListNotice.Finished(bought = 2, remaining = 1), viewModel.notice.value)
        assertEquals(listOf(beans), stored.values.toList())
        viewModel.onNoticeShown()
        assertNull(viewModel.notice.value)
    }

    @Test
    fun finishingWithNothingCheckedShowsNothing() = runTest {
        val tomato = ListItem(ListItemId("1"), "Tomate", null, "produce")
        stored[tomato.id] = tomato
        val viewModel = viewModel()

        viewModel.onFinish()

        assertNull(viewModel.notice.value)
        assertNull(undoStack.lastAction.value)
    }

    @Test
    fun clearingTakesEverythingOutAndShowsTheNotice() = runTest {
        val tomato = ListItem(ListItemId("1"), "Tomate", null, "produce", checked = true)
        val beans = ListItem(ListItemId("2"), "Feijão", null, "produce")
        listOf(tomato, beans).forEach { stored[it.id] = it }
        val viewModel = viewModel()

        viewModel.onClearList()

        assertEquals(ListNotice.Cleared, viewModel.notice.value)
        assertTrue(stored.isEmpty())
        viewModel.onUndo()
        assertEquals(setOf(tomato, beans), stored.values.toSet())
    }

    @Test
    fun clearingAnEmptyListShowsNothing() = runTest {
        val viewModel = viewModel()

        viewModel.onClearList()

        assertNull(viewModel.notice.value)
        assertNull(undoStack.lastAction.value)
    }

    @Test
    fun totalCountsCheckedAndUncheckedItems() {
        val state = ListUiState.Content(
            listOf(
                SectionUi(
                    "produce",
                    "Hortifrúti",
                    listOf(
                        ListRowUi(ListItemId("1"), "Tomate", checked = true, icon = R.drawable.ic_add),
                        ListRowUi(ListItemId("2"), "Couve", icon = R.drawable.ic_add),
                    ),
                ),
                SectionUi(
                    "pantry",
                    "Mercearia",
                    listOf(ListRowUi(ListItemId("3"), "Feijão", icon = R.drawable.ic_add)),
                ),
            ),
        )

        assertEquals(3, state.total)
        assertEquals(2, state.remaining)
    }
}
