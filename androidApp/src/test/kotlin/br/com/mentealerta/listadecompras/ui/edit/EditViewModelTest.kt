package br.com.mentealerta.listadecompras.ui.edit

import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.domain.QuantityUnit
import br.com.mentealerta.listadecompras.domain.UndoLastActionUseCase
import br.com.mentealerta.listadecompras.domain.undo.UndoableAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Edição do produto (#55): rascunho na tela, gravado ao sair; − / +; trocar o nome; tirar da lista. */
@OptIn(ExperimentalCoroutinesApi::class)
class EditViewModelTest {
    private val fixture = EditFixture()
    private val beans = fixture.put(ListItem(ListItemId("1"), "feijão", "beans", "pantry", detail = "preto"))

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun opened(id: ListItemId = beans.id) = fixture.editViewModel().apply { open(id) }

    private val EditViewModel.editing get() = uiState.value as EditUiState.Editing

    private val stored get() = fixture.items.single()

    @Test
    fun opensTheProductWithItsFields() {
        assertEquals(
            EditUiState.Editing(
                name = "feijão",
                detail = "preto",
                quantity = null,
                canDecrease = false,
                icon = R.drawable.catalog_fluent_hot_beverage,
            ),
            opened().uiState.value,
        )
    }

    @Test
    fun productNoLongerOnTheListCloses() {
        assertEquals(EditUiState.Closed(null), opened(ListItemId("outro")).uiState.value)
    }

    @Test
    fun plusAndMinusChangeTheDraftAndAreSavedOnClose() {
        val viewModel = opened()
        viewModel.onIncrease()
        viewModel.onIncrease()
        assertEquals(Quantity(3.0), viewModel.editing.quantity)
        assertTrue(viewModel.editing.canDecrease)
        viewModel.onDecrease()
        assertEquals(Quantity(2.0), viewModel.editing.quantity)
        // Nada é gravado enquanto a pessoa está na tela.
        assertNull(stored.quantity)

        viewModel.onClose()
        assertEquals(Quantity(2.0), stored.quantity)
        assertEquals(UndoableAction.ItemEdited(beans), fixture.undoStack.lastAction.value)
    }

    @Test
    fun minusBackToOneClearsTheQuantity() {
        val viewModel = opened()
        viewModel.onIncrease()
        viewModel.onDecrease()
        assertNull(viewModel.editing.quantity)
        assertFalse(viewModel.editing.canDecrease)
    }

    @Test
    fun otherUnitsKeepTheirUnit() {
        val half = fixture.put(
            ListItem(ListItemId("2"), "carne moída", null, "pantry", Quantity(0.5, QuantityUnit.KILOGRAM)),
        )
        val viewModel = opened(half.id)
        viewModel.onIncrease()
        assertEquals(Quantity(1.0, QuantityUnit.KILOGRAM), viewModel.editing.quantity)
    }

    @Test
    fun renamingToAnotherKnownProductChangesIconAndSection() {
        val viewModel = opened()
        viewModel.onNameChange("Banana prata")
        assertEquals("Banana prata", viewModel.editing.name)
        assertEquals(R.drawable.catalog_fluent_banana, viewModel.editing.icon)

        viewModel.onClose()
        assertEquals("Banana prata", stored.name)
        assertEquals("banana", stored.productId)
        assertEquals("produce", stored.sectionId)
    }

    @Test
    fun blankNameKeepsTheOldNameAndDetailIsTrimmed() {
        val viewModel = opened()
        viewModel.onNameChange("   ")
        viewModel.onDetailChange("  carioca ")
        viewModel.onClose()
        assertEquals("feijão", stored.name)
        assertEquals("carioca", stored.detail)
    }

    @Test
    fun blankDetailIsRemoved() {
        val viewModel = opened()
        viewModel.onDetailChange(" ")
        viewModel.onClose()
        assertNull(stored.detail)
    }

    @Test
    fun closingWithoutChangesRecordsNothing() {
        opened().onClose()
        assertEquals(beans, stored)
        assertNull(fixture.undoStack.lastAction.value)
    }

    @Test
    fun savingInTheBackgroundThenClosingIsOneOpsVoltar() = runTest {
        val viewModel = opened()
        viewModel.onIncrease()
        viewModel.onSave()
        assertEquals(Quantity(2.0), stored.quantity)
        assertTrue(viewModel.uiState.value is EditUiState.Editing)

        viewModel.onClose()
        val undo = UndoLastActionUseCase(fixture.repository, fixture.undoStack)
        assertEquals(UndoableAction.ItemEdited(beans), undo())
        assertEquals(beans, stored)
        assertNull(undo())
    }

    @Test
    fun openingTheSameProductAgainKeepsTheDraft() {
        val viewModel = opened()
        viewModel.onIncrease()
        viewModel.open(beans.id)
        assertEquals(Quantity(2.0), viewModel.editing.quantity)
    }

    @Test
    fun openingAfterClosingStartsFromWhatIsSaved() {
        val viewModel = opened()
        viewModel.onIncrease()
        viewModel.onClose()
        viewModel.open(beans.id)
        assertEquals(Quantity(2.0), viewModel.editing.quantity)
    }

    @Test
    fun removingTakesItOffTheListWithTheNameOnScreen() = runTest {
        val viewModel = opened()
        viewModel.onNameChange("feijão preto")
        viewModel.onIncrease()
        viewModel.onRemove()

        assertEquals(EditUiState.Closed("feijão preto"), viewModel.uiState.value)
        assertTrue(fixture.items.isEmpty())
        // O rascunho não foi gravado: o "Ops, voltar" devolve o produto como estava.
        UndoLastActionUseCase(fixture.repository, fixture.undoStack)()
        assertEquals(listOf(beans), fixture.items)
    }
}
