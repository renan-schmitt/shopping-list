package br.com.mentealerta.listadecompras.billing

import br.com.mentealerta.listadecompras.premium.FULL_VERSION_PRODUCT_ID
import br.com.mentealerta.listadecompras.premium.StorePurchase
import br.com.mentealerta.listadecompras.premium.StoreResponse
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.Purchase

/** Traduz o código de resposta do Google Play Billing para a [StoreResponse] do `shared`. */
internal fun storeResponseOf(responseCode: Int, purchases: List<StorePurchase>): StoreResponse = when (responseCode) {
    BillingResponseCode.OK -> StoreResponse.Success(purchases)
    BillingResponseCode.USER_CANCELED -> StoreResponse.Cancelled
    BillingResponseCode.ITEM_ALREADY_OWNED -> StoreResponse.AlreadyOwned
    BillingResponseCode.SERVICE_DISCONNECTED,
    BillingResponseCode.SERVICE_UNAVAILABLE,
    BillingResponseCode.SERVICE_TIMEOUT,
    BillingResponseCode.BILLING_UNAVAILABLE,
    BillingResponseCode.NETWORK_ERROR,
    BillingResponseCode.ITEM_UNAVAILABLE,
    -> StoreResponse.Unavailable
    else -> StoreResponse.Failed
}

/** Só as compras da versão completa, pagas ou pendentes; o estado "não especificado" fica de fora. */
internal fun fullVersionPurchases(purchases: List<Purchase>): List<StorePurchase> = purchases
    .filter { FULL_VERSION_PRODUCT_ID in it.products }
    .mapNotNull { purchase ->
        storePurchaseOf(purchase.purchaseToken, purchase.purchaseState, purchase.isAcknowledged)
    }

/** Uma compra do Google Play como [StorePurchase], ou `null` se o estado não for pago nem pendente. */
internal fun storePurchaseOf(token: String, purchaseState: Int, acknowledged: Boolean): StorePurchase? {
    val state = when (purchaseState) {
        Purchase.PurchaseState.PURCHASED -> StorePurchase.State.Purchased
        Purchase.PurchaseState.PENDING -> StorePurchase.State.Pending
        else -> return null
    }
    return StorePurchase(token, state, acknowledged)
}
