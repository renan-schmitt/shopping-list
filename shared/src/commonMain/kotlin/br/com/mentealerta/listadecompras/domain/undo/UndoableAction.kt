package br.com.mentealerta.listadecompras.domain.undo

import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.RemovedItems
import br.com.mentealerta.listadecompras.domain.ShoppingListRepository

/**
 * Ação que o "Ops, voltar" desfaz (guia de linguagem, princípio 6). Guarda o que é preciso para a
 * lista voltar exatamente ao que era antes; a UI usa o tipo e os itens para montar a frase.
 */
sealed class UndoableAction {
    /** Volta a lista ao que era antes da ação. */
    internal abstract suspend fun undo(repository: ShoppingListRepository)

    /** Riscou ou tirou o risco. [previous] é o item antes da mudança. */
    data class ItemChecked(val previous: ListItem) : UndoableAction() {
        override suspend fun undo(repository: ShoppingListRepository) {
            repository.setChecked(previous.id, previous.checked)
        }
    }

    /** Editou o item. [previous] é o item antes da edição. */
    data class ItemEdited(val previous: ListItem) : UndoableAction() {
        override suspend fun undo(repository: ShoppingListRepository) {
            repository.update(previous)
        }
    }

    /** Tirou o item da lista. */
    class ItemRemoved(val removed: RemovedItems) : UndoableAction() {
        override suspend fun undo(repository: ShoppingListRepository) {
            repository.restore(removed)
        }
    }

    /** Jogou a lista fora. */
    class ListCleared(val removed: RemovedItems) : UndoableAction() {
        override suspend fun undo(repository: ShoppingListRepository) {
            repository.restore(removed)
        }
    }
}
