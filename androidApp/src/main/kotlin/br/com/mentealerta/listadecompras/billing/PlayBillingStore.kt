package br.com.mentealerta.listadecompras.billing

import android.content.Context
import br.com.mentealerta.listadecompras.premium.BillingStore
import br.com.mentealerta.listadecompras.premium.FULL_VERSION_PRODUCT_ID
import br.com.mentealerta.listadecompras.premium.StorePurchase
import br.com.mentealerta.listadecompras.premium.StoreResponse
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

/**
 * [BillingStore] do Google Play Billing (plano §4.3), direto, sem serviço de terceiros. A janela de
 * pagamento abre sobre a tela que estiver na frente ([currentActivity]).
 */
class PlayBillingStore(context: Context, private val currentActivity: CurrentActivity) : BillingStore {
    private val purchaseMutex = Mutex()
    private val connectionMutex = Mutex()

    /** A compra em andamento, completada pelo [onPurchasesUpdated]. */
    @Volatile
    private var pendingPurchase: CompletableDeferred<StoreResponse>? = null

    private val updatesFlow = MutableSharedFlow<List<StorePurchase>>(extraBufferCapacity = UPDATES_BUFFER)
    override val updates: Flow<List<StorePurchase>> = updatesFlow

    private val client: BillingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener(::onPurchasesUpdated)
        // Pix e boleto: a compra fica pendente até o pagamento cair.
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .build()

    override suspend fun price(): String? = productDetails()
        ?.oneTimePurchaseOfferDetailsList
        ?.firstOrNull()
        ?.formattedPrice

    override suspend fun queryPurchases(): StoreResponse {
        if (!connect()) return StoreResponse.Unavailable
        val params = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        val result = client.queryPurchasesAsync(params)
        return storeResponseOf(result.billingResult.responseCode, fullVersionPurchases(result.purchasesList))
    }

    override suspend fun purchase(): StoreResponse = purchaseMutex.withLock {
        val activity = currentActivity.get() ?: return StoreResponse.Failed
        val details = productDetails() ?: return StoreResponse.Unavailable
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details).build()),
            )
            .build()
        val deferred = CompletableDeferred<StoreResponse>().also { pendingPurchase = it }
        try {
            val launch = client.launchBillingFlow(activity, flowParams)
            if (launch.responseCode != BillingClient.BillingResponseCode.OK) {
                return storeResponseOf(launch.responseCode, emptyList())
            }
            deferred.await()
        } finally {
            pendingPurchase = null
        }
    }

    override suspend fun acknowledge(token: String): Boolean {
        if (!connect()) return false
        val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(token).build()
        return client.acknowledgePurchase(params).responseCode == BillingClient.BillingResponseCode.OK
    }

    private fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        val mapped = fullVersionPurchases(purchases.orEmpty())
        val waiting = pendingPurchase
        if (waiting != null && !waiting.isCompleted) {
            waiting.complete(storeResponseOf(result.responseCode, mapped))
        } else if (result.responseCode == BillingClient.BillingResponseCode.OK && mapped.isNotEmpty()) {
            updatesFlow.tryEmit(mapped)
        }
    }

    private suspend fun productDetails(): ProductDetails? {
        if (!connect()) return null
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(FULL_VERSION_PRODUCT_ID)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val params = QueryProductDetailsParams.newBuilder().setProductList(listOf(product)).build()
        val result = client.queryProductDetails(params)
        if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) return null
        return result.productDetailsList?.firstOrNull { it.productId == FULL_VERSION_PRODUCT_ID }
    }

    /** Liga ao Google Play na primeira vez; depois a reconexão é automática. `false` sem Google Play. */
    private suspend fun connect(): Boolean = connectionMutex.withLock {
        if (client.isReady) return true
        suspendCancellableCoroutine { continuation ->
            client.startConnection(
                object : BillingClientStateListener {
                    override fun onBillingSetupFinished(result: BillingResult) {
                        if (continuation.isActive) {
                            continuation.resume(result.responseCode == BillingClient.BillingResponseCode.OK)
                        }
                    }

                    override fun onBillingServiceDisconnected() {
                        if (continuation.isActive) continuation.resume(false)
                    }
                },
            )
        }
    }

    private companion object {
        const val UPDATES_BUFFER = 8
    }
}
