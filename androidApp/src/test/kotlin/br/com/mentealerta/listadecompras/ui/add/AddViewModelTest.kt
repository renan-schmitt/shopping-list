package br.com.mentealerta.listadecompras.ui.add

import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.domain.QuantityUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddViewModelTest {
    private val fixture = AddFixture()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun optionsFollowWhatIsTyped() {
        val viewModel = fixture.viewModel()

        viewModel.onQueryChange("maca")
        val state = viewModel.uiState.value
        assertEquals("maca", state.query)
        assertEquals(listOf("Maçã"), state.found.map { it.name })
        assertEquals(R.drawable.catalog_fluent_red_apple, state.found.single().icon)
        assertNull(state.unknown)

        viewModel.onQueryChange("")
        assertTrue(viewModel.uiState.value.found.isEmpty())
        assertNull(viewModel.uiState.value.unknown)
    }

    @Test
    fun unknownProductUsesTheGenericIcon() {
        val viewModel = fixture.viewModel()

        viewModel.onQueryChange("2 pacotes de bergamota")
        val unknown = viewModel.uiState.value.unknown!!
        assertEquals("bergamota", unknown.name)
        assertEquals(Quantity(2.0, QuantityUnit.PACKAGE), unknown.quantity)
        assertEquals(R.drawable.catalog_fluent_shopping_cart, unknown.icon)
    }

    @Test
    fun addingPutsOnTheListClearsTheFieldAndTellsWhatWasAdded() {
        val viewModel = fixture.viewModel()
        viewModel.onQueryChange("2 bananas")

        viewModel.onAdd(viewModel.uiState.value.found.first())

        val added = fixture.added.single()
        assertEquals("banana", added.productId)
        assertEquals("Banana", added.name)
        assertEquals(Quantity(2.0), added.quantity)
        assertEquals(AddUiState(lastAdded = "Banana"), viewModel.uiState.value)
    }

    @Test
    fun typingAgainHidesTheNotice() {
        val viewModel = fixture.viewModel()
        viewModel.onQueryChange("banana")
        viewModel.onAdd(viewModel.uiState.value.found.first())

        viewModel.onQueryChange("m")
        assertNull(viewModel.uiState.value.lastAdded)
    }

    @Test
    fun submitAddsTheFirstOptionOrTheUnknown() {
        val viewModel = fixture.viewModel()
        viewModel.onQueryChange("banan")
        viewModel.onSubmit()
        viewModel.onQueryChange("bergamota")
        viewModel.onSubmit()

        assertEquals(listOf("banana", null), fixture.added.map { it.productId })
        assertEquals(listOf("Banana", "bergamota"), fixture.added.map { it.name })
    }

    @Test
    fun submitWithEmptyFieldDoesNothing() {
        val viewModel = fixture.viewModel()
        viewModel.onQueryChange("  ")
        viewModel.onSubmit()

        assertTrue(fixture.added.isEmpty())
    }

    @Test
    fun onlyTheLastSearchCounts() = runTest {
        val viewModel = fixture.viewModel(searchDispatcher = StandardTestDispatcher(testScheduler))
        viewModel.onQueryChange("maca")
        viewModel.onQueryChange("banana")
        advanceUntilIdle()

        assertEquals(listOf("Banana"), viewModel.uiState.value.found.map { it.name })
    }

    @Test
    fun closingStartsEmptyNextTime() {
        val viewModel = fixture.viewModel()
        viewModel.onQueryChange("banana")
        viewModel.onClose()

        assertEquals(AddUiState(), viewModel.uiState.value)
    }
}
