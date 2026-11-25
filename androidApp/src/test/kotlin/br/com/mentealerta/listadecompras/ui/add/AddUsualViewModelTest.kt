package br.com.mentealerta.listadecompras.ui.add

import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/** "Você costuma comprar" na tela de adicionar (#65). */
@OptIn(ExperimentalCoroutinesApi::class)
class AddUsualViewModelTest {
    private val fixture = AddFixture()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun usualProductsUseThePreferredTermAndTheProductIcon() = runTest {
        fixture.preferredTerms.terms["apple"] = "maçã gala"
        fixture.buyEvery10Days("apple", "Maçã")
        fixture.buyEvery10Days(null, "Sabão de coco")
        val viewModel = fixture.viewModel()

        val usual = usualOf(viewModel)()
        assertEquals(listOf("maçã gala", "Sabão de coco"), usual.map { it.name })
        assertEquals(
            listOf(R.drawable.catalog_fluent_red_apple, R.drawable.catalog_fluent_shopping_cart),
            usual.map { it.icon },
        )
    }

    @Test
    fun touchingPutsOnTheListTellsAndLeavesTheSuggestions() = runTest {
        fixture.buyEvery10Days("banana", "banana")
        val viewModel = fixture.viewModel()
        val usual = usualOf(viewModel)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.added.collect {} }

        viewModel.usual.onAdd(usual().single())

        val item = fixture.added.single()
        assertEquals("banana", item.name)
        assertEquals("banana", item.productId)
        assertEquals("produce", item.sectionId)
        assertNull(item.quantity)
        assertEquals(AddedUi("banana"), viewModel.uiState.value.lastAdded)
        assertEquals(listOf("banana"), viewModel.added.value.map { it.name })
        assertEquals(emptyList<UsualUi>(), usual())
    }

    @Test
    fun touchingTwiceAddsOnce() = runTest {
        fixture.buyEvery10Days("banana", "Banana")
        val viewModel = fixture.viewModel()
        val banana = usualOf(viewModel)().single()

        viewModel.usual.onAdd(banana)
        viewModel.usual.onAdd(banana)

        assertEquals(1, fixture.added.size)
    }

    @Test
    fun comesBackWhenTakenOffTheList() = runTest {
        fixture.buyEvery10Days("banana", "Banana")
        val viewModel = fixture.viewModel()
        val usual = usualOf(viewModel)

        viewModel.usual.onAdd(usual().single())
        assertEquals(emptyList<UsualUi>(), usual())

        fixture.remove(fixture.added.single().id)
        assertEquals(listOf("Banana"), usual().map { it.name })
    }

    @Test
    fun productAlreadyOnTheListIsNotSuggested() = runTest {
        fixture.buyEvery10Days("banana", "Banana")
        fixture.buyEvery10Days("apple", "Maçã")
        val viewModel = fixture.viewModel()
        val usual = usualOf(viewModel)

        fixture.items += ListItem(ListItemId("1"), "bananas", productId = "banana", sectionId = "produce")
        fixture.check(ListItemId("1"))

        assertEquals(listOf("Maçã"), usual().map { it.name })
    }

    /** "Você costuma comprar", coletando como a tela faria. */
    private fun TestScope.usualOf(viewModel: AddViewModel): () -> List<UsualUi> {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.usual.products.collect {} }
        return { viewModel.usual.products.value }
    }
}
