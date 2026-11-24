package br.com.mentealerta.listadecompras.ui.add

import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.Quantity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Colar uma lista recebida (#68): conferência, tirar, pôr na lista, deixar pra lá. */
@OptIn(ExperimentalCoroutinesApi::class)
class AddPasteViewModelTest {
    private val fixture = AddFixture()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private val whatsApp = "*Lista de compras*\n\n*Hortifrúti*\n- Banana (6)\n- Maçã\n- Bergamota"

    @Test
    fun pastedTextOpensTheReviewWithoutPuttingAnything() {
        val viewModel = fixture.viewModel()

        viewModel.paste.onPaste(whatsApp)

        val review = viewModel.uiState.value.review!!
        assertEquals(listOf("Banana", "Maçã", "Bergamota"), review.items.map { it.name })
        assertEquals(Quantity(6.0), review.items.first().quantity)
        assertTrue(fixture.added.isEmpty())
    }

    @Test
    fun confirmPutsTheReviewedProducts() {
        val viewModel = fixture.viewModel()
        viewModel.paste.onPaste(whatsApp)
        val review = viewModel.uiState.value.review!!

        viewModel.paste.onRemove(review.items[1])
        viewModel.paste.onConfirm()

        assertEquals(listOf("Banana", "Bergamota"), fixture.added.map { it.name })
        assertEquals(listOf("banana", null), fixture.added.map { it.productId })
        val state = viewModel.uiState.value
        assertNull(state.review)
        assertEquals(2, state.lastAdded?.count)
        // Entram em "Adicionados", o mais recente no topo.
        assertEquals(2, fixture.items.size)
    }

    @Test
    fun productAlreadyOnTheListStaysOut() {
        fixture.items += ListItem.create(name = "Banana", productId = "banana", sectionId = "produce")
        val viewModel = fixture.viewModel()

        viewModel.paste.onPaste(whatsApp)
        val review = viewModel.uiState.value.review!!
        assertEquals(listOf("Maçã", "Bergamota"), review.items.map { it.name })
        assertEquals(listOf("Banana"), review.alreadyOnList)

        viewModel.paste.onConfirm()
        assertEquals(listOf("Maçã", "Bergamota"), fixture.added.map { it.name })
        assertEquals(listOf("Banana"), viewModel.uiState.value.lastAdded?.alreadyOnList)
    }

    @Test
    fun everythingAlreadyOnTheListOnlyTells() {
        fixture.items += ListItem.create(name = "Banana", productId = "banana", sectionId = "produce")
        val viewModel = fixture.viewModel()

        viewModel.paste.onPaste("bananas")

        val state = viewModel.uiState.value
        assertNull(state.review)
        assertEquals(AddedUi(name = "", count = 0, alreadyOnList = listOf("Banana")), state.lastAdded)
    }

    @Test
    fun removingEveryProductClosesTheReview() {
        val viewModel = fixture.viewModel()
        viewModel.paste.onPaste("banana")

        viewModel.paste.onRemove(viewModel.uiState.value.review!!.items.single())

        assertNull(viewModel.uiState.value.review)
        assertTrue(fixture.added.isEmpty())
    }

    @Test
    fun dismissPutsNothing() {
        val viewModel = fixture.viewModel()
        viewModel.paste.onPaste(whatsApp)

        viewModel.paste.onDismiss()
        viewModel.paste.onConfirm()

        assertNull(viewModel.uiState.value.review)
        assertTrue(fixture.added.isEmpty())
    }

    @Test
    fun typingLeavesTheReview() {
        val viewModel = fixture.viewModel()
        viewModel.paste.onPaste(whatsApp)

        viewModel.onQueryChange("ban")

        assertNull(viewModel.uiState.value.review)
        assertTrue(fixture.added.isEmpty())
    }

    @Test
    fun textWithoutProducts() {
        val viewModel = fixture.viewModel()

        viewModel.paste.onPaste("*Lista de compras*\n\n:)")

        val state = viewModel.uiState.value
        assertTrue(state.pastedNothing)
        assertNull(state.review)
        viewModel.onQueryChange("b")
        assertEquals(false, viewModel.uiState.value.pastedNothing)
    }
}
