package br.com.mentealerta.listadecompras.domain

import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

/**
 * O "Não" que a pessoa respondeu ao "Está faltando?" (plano §3.4, #64), guardado no celular: o produto
 * só volta a ser sugerido depois de mais um intervalo inteiro. A chave é a do produto ([productKey]).
 * A implementação fica em `data`; quem usa recebe esta interface pelo Koin.
 */
interface SuggestionDismissalRepository {
    /** Chave do produto → quando a pessoa disse "Não" pela última vez; emite de novo a cada "Não". */
    fun observeDismissals(): Flow<Map<String, Instant>>

    /** Guarda o "Não" para o produto [key] em [at], no lugar do anterior. */
    suspend fun dismiss(key: String, at: Instant)
}
