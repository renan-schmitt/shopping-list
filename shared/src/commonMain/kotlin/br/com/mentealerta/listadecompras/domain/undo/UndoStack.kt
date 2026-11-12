package br.com.mentealerta.listadecompras.domain.undo

import br.com.mentealerta.listadecompras.domain.ShoppingListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Pilha do "Ops, voltar": as últimas ações, da mais recente para a mais antiga. Fica só na memória
 * (some ao fechar o app) e guarda no máximo [maxSize] ações; a mais antiga sai primeiro.
 *
 * Fazer a ação e desfazer passam pelo mesmo cadeado, para a pilha nunca ficar fora de ordem.
 */
class UndoStack(private val maxSize: Int = DEFAULT_MAX_SIZE) {
    private val mutex = Mutex()
    private val actions = ArrayDeque<UndoableAction>()
    private val mutableLastAction = MutableStateFlow<UndoableAction?>(null)

    init {
        require(maxSize > 0) { "Pilha sem espaço" }
    }

    /** A ação que o "Ops, voltar" desfaz agora; `null` quando não há o que voltar. */
    val lastAction: StateFlow<UndoableAction?> = mutableLastAction.asStateFlow()

    /** Faz a ação com [block] e guarda o que ele devolver; `null` (nada mudou) não entra na pilha. */
    internal suspend fun record(block: suspend () -> UndoableAction?): UndoableAction? = mutex.withLock {
        block()?.also { action ->
            if (actions.size == maxSize) actions.removeFirst()
            actions.addLast(action)
            mutableLastAction.value = action
        }
    }

    /**
     * Desfaz a última ação e a tira da pilha. Se desfazer falhar, a ação continua na pilha. Devolve
     * a ação desfeita, ou `null` se a pilha está vazia.
     */
    internal suspend fun undo(repository: ShoppingListRepository): UndoableAction? = mutex.withLock {
        val action = actions.lastOrNull() ?: return@withLock null
        action.undo(repository)
        actions.removeLast()
        mutableLastAction.value = actions.lastOrNull()
        action
    }

    companion object {
        const val DEFAULT_MAX_SIZE = 20
    }
}
