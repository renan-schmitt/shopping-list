package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.domain.undo.UndoStack
import br.com.mentealerta.listadecompras.domain.undo.UndoableAction

/** Risca ou tira o risco, guardando para o "Ops, voltar". Devolve `false` se nada mudou. */
class SetItemCheckedUseCase(private val repository: ShoppingListRepository, private val undoStack: UndoStack) {
    suspend operator fun invoke(id: ListItemId, checked: Boolean): Boolean =
        undoStack.record { repository.setChecked(id, checked)?.let(UndoableAction::ItemChecked) } != null
}

/**
 * Grava a edição do item, guardando para o "Ops, voltar". Devolve `false` se o item não está mais
 * na lista; edição que não muda nada não entra na pilha.
 */
class EditItemUseCase(private val repository: ShoppingListRepository, private val undoStack: UndoStack) {
    suspend operator fun invoke(item: ListItem): Boolean {
        var found = false
        undoStack.record {
            repository.update(item)
                ?.also { found = true }
                ?.takeIf { it != item }
                ?.let(UndoableAction::ItemEdited)
        }
        return found
    }
}

/** Tira o item da lista, guardando para o "Ops, voltar". Devolve `false` se o item já não estava. */
class RemoveItemUseCase(private val repository: ShoppingListRepository, private val undoStack: UndoStack) {
    suspend operator fun invoke(id: ListItemId): Boolean = undoStack.record {
        repository.remove(id).takeUnless { it.isEmpty() }?.let(UndoableAction::ItemRemoved)
    } != null
}

/** Joga a lista fora, guardando para o "Ops, voltar". Devolve `false` se a lista já estava vazia. */
class ClearListUseCase(private val repository: ShoppingListRepository, private val undoStack: UndoStack) {
    suspend operator fun invoke(): Boolean = undoStack.record {
        repository.clear().takeUnless { it.isEmpty() }?.let(UndoableAction::ListCleared)
    } != null
}

/** "Ops, voltar": desfaz a última ação. Devolve a ação desfeita, ou `null` se não havia o que voltar. */
class UndoLastActionUseCase(private val repository: ShoppingListRepository, private val undoStack: UndoStack) {
    suspend operator fun invoke(): UndoableAction? = undoStack.undo(repository)
}
