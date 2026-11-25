package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.domain.undo.UndoStack
import br.com.mentealerta.listadecompras.domain.undo.UndoableAction

/** Risca ou tira o risco, guardando para o "Desfazer". Devolve `false` se nada mudou. */
class SetItemCheckedUseCase(private val repository: ShoppingListRepository, private val undoStack: UndoStack) {
    suspend operator fun invoke(id: ListItemId, checked: Boolean): Boolean =
        undoStack.record { repository.setChecked(id, checked)?.let(UndoableAction::ItemChecked) } != null
}

/**
 * Grava a edição do item, guardando para o "Desfazer". Devolve `false` se o item não está mais
 * na lista; edição que não muda nada não entra na pilha. Nome trocado ensina o termo preferido
 * do produto ([LearnPreferredTermUseCase]); seção escolhida pela pessoa ([sectionChosen]) e
 * diferente da que estava fica lembrada para o produto ([RememberSectionUseCase], #56).
 */
class EditItemUseCase(
    private val repository: ShoppingListRepository,
    private val undoStack: UndoStack,
    private val learnPreferredTerm: LearnPreferredTermUseCase,
    private val rememberSection: RememberSectionUseCase,
) {
    suspend operator fun invoke(item: ListItem, sectionChosen: Boolean = false): Boolean {
        var previous: ListItem? = null
        undoStack.record {
            repository.update(item)
                ?.also { previous = it }
                ?.takeIf { it != item }
                ?.let(UndoableAction::ItemEdited)
        }
        val before = previous ?: return false
        if (before.name != item.name) learnPreferredTerm(item)
        if (sectionChosen && before.sectionId != item.sectionId) rememberSection.learn(item)
        return true
    }
}

/** Tira o item da lista, guardando para o "Desfazer". Devolve `false` se o item já não estava. */
class RemoveItemUseCase(private val repository: ShoppingListRepository, private val undoStack: UndoStack) {
    suspend operator fun invoke(id: ListItemId): Boolean = undoStack.record {
        repository.remove(id).takeUnless { it.isEmpty() }?.let(UndoableAction::ItemRemoved)
    } != null
}

/** Joga a lista fora, guardando para o "Desfazer". Devolve `false` se a lista já estava vazia. */
class ClearListUseCase(private val repository: ShoppingListRepository, private val undoStack: UndoStack) {
    suspend operator fun invoke(): Boolean = undoStack.record {
        repository.clear().takeUnless { it.isEmpty() }?.let(UndoableAction::ListCleared)
    } != null
}

/**
 * "Terminei": grava a compra no histórico e tira os riscados da lista, guardando para o "Desfazer".
 * Devolve o que foi feito, ou `null` se nada estava riscado.
 */
class FinishShoppingUseCase(private val repository: ShoppingListRepository, private val undoStack: UndoStack) {
    suspend operator fun invoke(): FinishedShopping? =
        undoStack.record { repository.finishShopping()?.let(UndoableAction::ShoppingFinished) }?.finished
}

/** "Desfazer": desfaz a última ação. Devolve a ação desfeita, ou `null` se não havia o que voltar. */
class UndoLastActionUseCase(private val repository: ShoppingListRepository, private val undoStack: UndoStack) {
    suspend operator fun invoke(): UndoableAction? = undoStack.undo(repository)
}
