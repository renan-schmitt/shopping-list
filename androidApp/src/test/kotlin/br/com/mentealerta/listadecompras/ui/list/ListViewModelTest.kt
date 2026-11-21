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
import br.com.mentealerta.listadecompras.domain.ShoppingListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
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

    private val repository = object : ShoppingListRepository {
        override fun observeSections() = sections

        override suspend fun add(item: ListItem) = Unit

        override suspend fun update(item: ListItem): ListItem? = null

        override suspend fun setChecked(id: ListItemId, checked: Boolean): ListItem? = null

        override suspend fun remove(id: ListItemId) = RemovedItems.NONE

        override suspend fun clear() = RemovedItems.NONE

        override suspend fun restore(removed: RemovedItems) = Unit
    }

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = ListViewModel(ObserveListUseCase(repository), index)

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
}
