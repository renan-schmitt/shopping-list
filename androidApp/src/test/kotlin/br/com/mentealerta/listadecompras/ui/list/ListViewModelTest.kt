package br.com.mentealerta.listadecompras.ui.list

import app.cash.turbine.test
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.catalog.Catalog
import br.com.mentealerta.listadecompras.catalog.CatalogIcon
import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.CatalogProduct
import br.com.mentealerta.listadecompras.catalog.CatalogSection
import br.com.mentealerta.listadecompras.catalog.IconFit
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.domain.ListSection
import br.com.mentealerta.listadecompras.domain.ObserveListUseCase
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
import org.junit.Before
import org.junit.Test

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

        override suspend fun clear() = RemovedItems.NONE

        override suspend fun restore(removed: RemovedItems) = Unit
    }

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = ListViewModel(
        ObserveListUseCase(repository),
        index,
        SetItemCheckedUseCase(repository, undoStack),
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
                                    ListItemId("1"),
                                    "bananas",
                                    null,
                                    Quantity(6.0),
                                    true,
                                    R.drawable.catalog_fluent_banana,
                                ),
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
                    ),
                ),
                awaitItem(),
            )
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
}
