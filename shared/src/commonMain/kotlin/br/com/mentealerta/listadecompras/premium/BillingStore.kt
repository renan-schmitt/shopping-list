package br.com.mentealerta.listadecompras.premium

import kotlinx.coroutines.flow.Flow

/** Id do produto da versão completa ("Premium para sempre") na loja: compra única, não consumível (plano §5). */
const val FULL_VERSION_PRODUCT_ID = "versao_completa"

/** Uma compra da versão completa, do jeito que a loja informa. */
data class StorePurchase(
    /** Identifica a compra na loja; é o que se manda para confirmar. */
    val token: String,
    val state: State,
    /** Se a compra já foi confirmada à loja. Sem a confirmação, o Google devolve o dinheiro em 3 dias. */
    val acknowledged: Boolean,
) {
    enum class State {
        /** Paga: libera a versão completa. */
        Purchased,

        /** Esperando o pagamento (Pix, boleto): ainda não libera. */
        Pending,
    }
}

/** O que a loja respondeu a um pedido. */
sealed interface StoreResponse {
    /** A loja respondeu; [purchases] traz só as compras da versão completa (pode vir vazio). */
    data class Success(val purchases: List<StorePurchase>) : StoreResponse

    /** A pessoa fechou a janela de pagamento. */
    data object Cancelled : StoreResponse

    /** A loja diz que a pessoa já tem a versão completa. */
    data object AlreadyOwned : StoreResponse

    /** Sem internet, sem Google Play ou loja fora do ar: tentar de novo mais tarde. */
    data object Unavailable : StoreResponse

    /** Qualquer outro erro da loja. */
    data object Failed : StoreResponse
}

/**
 * A loja do celular (Google Play Billing no Android; StoreKit no iPhone, fase 5). Só fala com a loja:
 * guardar a compra, confirmar e decidir o que libera fica no [PremiumRepository].
 */
interface BillingStore {
    /** Preço da versão completa já formatado pela loja ("R$ 19,90"), ou `null` se a loja não respondeu. */
    suspend fun price(): String?

    /** As compras da versão completa que a conta da loja já tem (celular novo, app reinstalado). */
    suspend fun queryPurchases(): StoreResponse

    /** Abre a janela de pagamento da loja e espera a pessoa terminar. */
    suspend fun purchase(): StoreResponse

    /** Confirma a compra à loja. `false` se não deu (tenta de novo na próxima vez que o app abrir). */
    suspend fun acknowledge(token: String): Boolean

    /**
     * Compras que chegam fora de um [purchase] em andamento, como um Pix que foi pago depois, com o
     * app aberto.
     */
    val updates: Flow<List<StorePurchase>>
}
