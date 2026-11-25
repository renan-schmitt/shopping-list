package br.com.mentealerta.listadecompras.ui.add

import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.domain.QuantityUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Colar uma lista recebida (#68) ou recebê-la pelo "Compartilhar" (#135): conferência, tirar, mudar
 * um produto (#136), pôr na lista, deixar pra lá.
 */
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
        assertEquals(PasteSource.COPIED, state.pastedNothing)
        assertNull(state.review)
        viewModel.onQueryChange("b")
        assertNull(viewModel.uiState.value.pastedNothing)
    }

    @Test
    fun sharedTextOpensTheReview() {
        val viewModel = fixture.viewModel()

        viewModel.paste.onShared(whatsApp)

        val review = viewModel.uiState.value.review!!
        assertEquals(listOf("Banana", "Maçã", "Bergamota"), review.items.map { it.name })
        assertTrue(fixture.added.isEmpty())
    }

    @Test
    fun sharedTextReplacesAnOpenReview() {
        val viewModel = fixture.viewModel()
        viewModel.paste.onPaste(whatsApp)

        viewModel.paste.onShared("arroz\nfeijão")

        assertEquals(listOf("arroz", "feijão"), viewModel.uiState.value.review!!.items.map { it.name })
    }

    @Test
    fun sharedTextWithoutProducts() {
        val viewModel = fixture.viewModel()

        viewModel.paste.onShared("*Lista de compras*")

        assertEquals(PasteSource.SHARED, viewModel.uiState.value.pastedNothing)
    }

    @Test
    fun tappingAProductOpensItsDraft() {
        val viewModel = fixture.viewModel()
        viewModel.paste.onPaste(whatsApp)

        viewModel.paste.onEdit(viewModel.uiState.value.review!!.items.first())

        val editing = viewModel.uiState.value.review!!.editing!!
        assertEquals(0, editing.index)
        assertEquals("Banana", editing.draft.name)
        assertEquals(Quantity(6.0), editing.draft.quantity)
        assertTrue(editing.draft.canDecrease)
        assertEquals("", editing.draft.detail)
    }

    @Test
    fun changedProductStaysInTheSamePlaceAndIsPut() {
        val viewModel = fixture.viewModel()
        viewModel.paste.onPaste(whatsApp)
        val bergamota = viewModel.uiState.value.review!!.items[2]
        val unknownIcon = bergamota.icon

        viewModel.paste.onEdit(bergamota)
        viewModel.paste.onEditName("Maçã verde")
        // O ícone acompanha o nome enquanto a pessoa digita.
        assertNotEquals(unknownIcon, viewModel.uiState.value.review!!.editing!!.draft.icon)
        viewModel.paste.onEditIncrease()
        viewModel.paste.onEditDetail(" desnatado ")
        viewModel.paste.onEditClose()

        val review = viewModel.uiState.value.review!!
        assertNull(review.editing)
        assertEquals(listOf("Banana", "Maçã", "Maçã verde"), review.items.map { it.name })
        val apple = review.items[2]
        assertEquals(Quantity(2.0), apple.quantity)
        assertEquals("desnatado", apple.detail)

        viewModel.paste.onConfirm()
        val added = fixture.added.last()
        assertEquals("Maçã verde", added.name)
        assertEquals("apple", added.productId)
        assertEquals(Quantity(2.0), added.quantity)
        assertEquals("desnatado", added.detail)
    }

    @Test
    fun lessAndMoreFollowTheEditScreen() {
        val viewModel = fixture.viewModel()
        viewModel.paste.onPaste("2 kg de arroz")
        viewModel.paste.onEdit(viewModel.uiState.value.review!!.items.single())

        viewModel.paste.onEditDecrease()
        val draft = viewModel.uiState.value.review!!.editing!!.draft
        assertEquals(Quantity(1.0, QuantityUnit.KILOGRAM), draft.quantity)
        assertFalse(draft.canDecrease)

        viewModel.paste.onEditDecrease()
        assertEquals(Quantity(1.0, QuantityUnit.KILOGRAM), viewModel.uiState.value.review!!.editing!!.draft.quantity)
    }

    @Test
    fun blankNameKeepsTheNameThatCame() {
        val viewModel = fixture.viewModel()
        viewModel.paste.onPaste(whatsApp)
        viewModel.paste.onEdit(viewModel.uiState.value.review!!.items[1])

        viewModel.paste.onEditName("   ")
        viewModel.paste.onEditClose()

        assertEquals(listOf("Banana", "Maçã", "Bergamota"), viewModel.uiState.value.review!!.items.map { it.name })
    }

    @Test
    fun sharedTextWhileChangingOpensTheNewReview() {
        val viewModel = fixture.viewModel()
        viewModel.paste.onPaste(whatsApp)
        viewModel.paste.onEdit(viewModel.uiState.value.review!!.items.first())

        viewModel.paste.onShared("arroz")

        val review = viewModel.uiState.value.review!!
        assertNull(review.editing)
        assertEquals(listOf("arroz"), review.items.map { it.name })
    }
}
