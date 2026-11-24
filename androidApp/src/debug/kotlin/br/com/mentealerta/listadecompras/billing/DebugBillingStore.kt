package br.com.mentealerta.listadecompras.billing

import android.content.Context
import androidx.core.content.edit
import br.com.mentealerta.listadecompras.premium.BillingStore
import br.com.mentealerta.listadecompras.premium.StorePurchase
import br.com.mentealerta.listadecompras.premium.StoreResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Loja falsa do build debug, como a do app Mente Alerta: comprar libera na hora, sem janela de
 * pagamento. A compra fica guardada num arquivo próprio (fora do backup), para valer depois de fechar
 * o app; limpar os dados do app volta para a versão grátis.
 */
internal class DebugBillingStore(context: Context) : BillingStore {
    private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    override val updates: Flow<List<StorePurchase>> = emptyFlow()

    override suspend fun price(): String = "R$ 0,00 (teste)"

    override suspend fun queryPurchases(): StoreResponse =
        StoreResponse.Success(if (preferences.getBoolean(KEY_OWNED, false)) listOf(PURCHASE) else emptyList())

    override suspend fun purchase(): StoreResponse {
        preferences.edit { putBoolean(KEY_OWNED, true) }
        return StoreResponse.Success(listOf(PURCHASE))
    }

    override suspend fun acknowledge(token: String): Boolean = true

    private companion object {
        const val FILE_NAME = "debug_billing"
        const val KEY_OWNED = "owned"
        val PURCHASE = StorePurchase("debug", StorePurchase.State.Purchased, acknowledged = true)
    }
}
