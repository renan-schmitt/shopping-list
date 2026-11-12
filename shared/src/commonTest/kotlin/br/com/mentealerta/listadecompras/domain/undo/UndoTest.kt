package br.com.mentealerta.listadecompras.domain.undo

import app.cash.turbine.test
import br.com.mentealerta.listadecompras.data.EmbeddedCatalogRepository
import br.com.mentealerta.listadecompras.data.RoomShoppingListRepository
import br.com.mentealerta.listadecompras.data.StepClock
import br.com.mentealerta.listadecompras.data.database.createTestDatabase
import br.com.mentealerta.listadecompras.domain.ClearListUseCase
import br.com.mentealerta.listadecompras.domain.EditItemUseCase
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.domain.ListSection
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.domain.QuantityUnit
import br.com.mentealerta.listadecompras.domain.RemoveItemUseCase
import br.com.mentealerta.listadecompras.domain.RemovedItems
import br.com.mentealerta.listadecompras.domain.SetItemCheckedUseCase
import br.com.mentealerta.listadecompras.domain.ShoppingListRepository
import br.com.mentealerta.listadecompras.domain.UndoLastActionUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UndoTest {
    private val database = createTestDatabase()
    private val repository = RoomShoppingListRepository(database, EmbeddedCatalogRepository(), StepClock())
    private val undoStack = UndoStack(maxSize = 3)

    private val setChecked = SetItemCheckedUseCase(repository, undoStack)
    private val edit = EditItemUseCase(repository, undoStack)
    private val remove = RemoveItemUseCase(repository, undoStack)
    private val clear = ClearListUseCase(repository, undoStack)
    private val undo = UndoLastActionUseCase(repository, undoStack)

    private val milk = ListItem.create(name = "Leite", productId = "milk", sectionId = "deli_dairy")
    private val cheese = ListItem.create(name = "Queijo", productId = "cheese", sectionId = "deli_dairy")
    private val cassava = ListItem.create(name = "aipim", productId = "cassava", sectionId = "produce")

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun undoingACheckRemovesTheCheck() = runTest {
        addAll(milk, cheese)
        val before = sections()

        assertTrue(setChecked(milk.id, checked = true))
        assertEquals(UndoableAction.ItemChecked(milk), undo())

        assertEquals(before, sections())
    }

    @Test
    fun undoingAnUncheckChecksAgain() = runTest {
        addAll(milk)
        repository.setChecked(milk.id, checked = true)
        val before = sections()

        assertTrue(setChecked(milk.id, checked = false))
        undo()

        assertEquals(before, sections())
    }

    @Test
    fun checkingWhatIsAlreadyCheckedDoesNotEnterTheStack() = runTest {
        addAll(milk)

        assertFalse(setChecked(milk.id, checked = false))
        assertFalse(setChecked(ListItemId("nao-existe"), checked = true))

        assertNull(undoStack.lastAction.value)
    }

    @Test
    fun undoingAnEditBringsBackTheOldItemInTheSamePlace() = runTest {
        addAll(milk, cassava, cheese)
        val before = sections()
        val edited = milk.copy(
            name = "Leite integral",
            sectionId = "pantry",
            quantity = Quantity(2.0, QuantityUnit.LITER),
            detail = "sem lactose",
            checked = true,
        )

        assertTrue(edit(edited))
        assertEquals(UndoableAction.ItemEdited(milk), undo())

        assertEquals(before, sections())
    }

    @Test
    fun editThatChangesNothingDoesNotEnterTheStack() = runTest {
        addAll(milk)

        assertTrue(edit(milk))
        assertFalse(edit(cheese))

        assertNull(undoStack.lastAction.value)
    }

    @Test
    fun undoingARemoveBringsTheItemBackToTheSamePlace() = runTest {
        addAll(milk, cheese)
        repository.setChecked(milk.id, checked = true)
        val before = sections()

        assertTrue(remove(milk.id))
        assertIs<UndoableAction.ItemRemoved>(undo())

        assertEquals(before, sections())
    }

    @Test
    fun removingAnItemNotOnTheListDoesNotEnterTheStack() = runTest {
        assertFalse(remove(milk.id))

        assertNull(undoStack.lastAction.value)
    }

    @Test
    fun undoingAClearBringsTheWholeListBack() = runTest {
        addAll(milk, cassava, cheese)
        repository.setChecked(cassava.id, checked = true)
        val before = sections()

        assertTrue(clear())
        assertEquals(emptyList(), sections())
        assertIs<UndoableAction.ListCleared>(undo())

        assertEquals(before, sections())
    }

    @Test
    fun clearingAnEmptyListDoesNotEnterTheStack() = runTest {
        assertFalse(clear())

        assertNull(undoStack.lastAction.value)
    }

    @Test
    fun undoesFromTheLastToTheFirst() = runTest {
        addAll(milk, cheese)
        val initial = sections()

        setChecked(milk.id, checked = true)
        val afterCheck = sections()
        remove(milk.id)

        assertIs<UndoableAction.ItemRemoved>(undo())
        assertEquals(afterCheck, sections())
        assertIs<UndoableAction.ItemChecked>(undo())
        assertEquals(initial, sections())
        assertNull(undo())
    }

    @Test
    fun undoWithNothingToUndoChangesNothing() = runTest {
        addAll(milk)
        val before = sections()

        assertNull(undo())

        assertEquals(before, sections())
    }

    @Test
    fun lastActionFollowsTheStack() = runTest {
        addAll(milk, cheese)

        undoStack.lastAction.test {
            assertNull(awaitItem())

            setChecked(milk.id, checked = true)
            assertEquals(UndoableAction.ItemChecked(milk), awaitItem())

            remove(cheese.id)
            assertIs<UndoableAction.ItemRemoved>(awaitItem())

            undo()
            assertEquals(UndoableAction.ItemChecked(milk), awaitItem())

            undo()
            assertNull(awaitItem())
        }
    }

    @Test
    fun keepsOnlyTheMostRecentActions() = runTest {
        addAll(milk, cheese, cassava)

        setChecked(milk.id, checked = true)
        setChecked(cheese.id, checked = true)
        setChecked(cassava.id, checked = true)
        setChecked(milk.id, checked = false)

        repeat(3) { assertIs<UndoableAction.ItemChecked>(undo()) }
        assertNull(undo())

        assertEquals(listOf(cassava, milk.check(), cheese), sections().flatMap { it.items })
    }

    @Test
    fun actionStaysOnTheStackWhenUndoFails() = runTest {
        val action = undoStack.record { UndoableAction.ItemChecked(milk) }

        assertFails { undoStack.undo(FailingRepository()) }

        assertEquals(action, undoStack.lastAction.value)
        assertEquals(action, undo())
    }

    private suspend fun addAll(vararg items: ListItem) = items.forEach { repository.add(it) }

    private suspend fun sections(): List<ListSection> = repository.observeSections().first()

    /** Repositório que falha em tudo, para simular erro ao desfazer. */
    private class FailingRepository : ShoppingListRepository {
        override fun observeSections(): Flow<List<ListSection>> = error("falhou")

        override suspend fun add(item: ListItem) = error("falhou")

        override suspend fun update(item: ListItem): ListItem? = error("falhou")

        override suspend fun setChecked(id: ListItemId, checked: Boolean): ListItem? = error("falhou")

        override suspend fun remove(id: ListItemId): RemovedItems = error("falhou")

        override suspend fun clear(): RemovedItems = error("falhou")

        override suspend fun restore(removed: RemovedItems) = error("falhou")
    }
}
