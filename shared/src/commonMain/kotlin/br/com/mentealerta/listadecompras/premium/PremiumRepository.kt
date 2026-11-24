package br.com.mentealerta.listadecompras.premium

import kotlinx.coroutines.flow.StateFlow

/** Como terminou a compra da versão completa. */
enum class PurchaseResult {
    /** Paga: a versão completa já está liberada. */
    Purchased,

    /** Esperando o pagamento (Pix, boleto): libera sozinha quando a loja confirmar. */
    Pending,

    /** A pessoa desistiu na janela da loja. */
    Cancelled,

    /** Sem internet ou sem Google Play: tentar de novo mais tarde. */
    Unavailable,

    /** Outro erro da loja. */
    Failed,
}

/** Como terminou o "Já comprei" (restaurar a compra). */
enum class RestoreResult {
    /** A compra foi encontrada: a versão completa está liberada. */
    Restored,

    /** A compra existe, mas o pagamento ainda não foi confirmado. */
    Pending,

    /** Esta conta da loja não tem a versão completa. */
    NothingToRestore,

    /** Sem internet ou sem Google Play: tentar de novo mais tarde. */
    Unavailable,

    /** Outro erro da loja. */
    Failed,
}

/**
 * Se a pessoa tem a versão completa ("Premium para sempre", plano §5), e como comprar e restaurar.
 * Funciona sem internet: guarda a última resposta da loja no celular. Sem servidor, a compra não é
 * conferida fora do celular (plano §4.3).
 */
interface PremiumRepository {
    /** `true` com a versão completa liberada. Começa com o que ficou guardado no celular. */
    val isPremium: StateFlow<Boolean>

    /**
     * Pergunta à loja, em silêncio, o que a pessoa já comprou e passa a ouvir os pagamentos que chegam
     * depois. Chamado uma vez, quando o app abre; chamar de novo não faz nada.
     */
    fun start()

    /** Preço formatado pela loja, ou `null` se a loja não respondeu. */
    suspend fun price(): String?

    suspend fun buy(): PurchaseResult

    suspend fun restore(): RestoreResult
}
