package br.com.mentealerta.listadecompras.billing

import br.com.mentealerta.listadecompras.premium.StorePurchase
import br.com.mentealerta.listadecompras.premium.StoreResponse
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.Purchase.PurchaseState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BillingResponsesTest {
    private val purchase = StorePurchase("token", StorePurchase.State.Purchased, acknowledged = false)

    @Test
    fun okCarriesThePurchases() {
        assertEquals(StoreResponse.Success(listOf(purchase)), storeResponseOf(BillingResponseCode.OK, listOf(purchase)))
    }

    @Test
    fun userCancelledIsCancelled() {
        assertEquals(StoreResponse.Cancelled, storeResponseOf(BillingResponseCode.USER_CANCELED, emptyList()))
    }

    @Test
    fun alreadyOwnedIsAlreadyOwned() {
        assertEquals(StoreResponse.AlreadyOwned, storeResponseOf(BillingResponseCode.ITEM_ALREADY_OWNED, emptyList()))
    }

    @Test
    fun noInternetOrNoPlayStoreIsUnavailable() {
        listOf(
            BillingResponseCode.SERVICE_DISCONNECTED,
            BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingResponseCode.SERVICE_TIMEOUT,
            BillingResponseCode.BILLING_UNAVAILABLE,
            BillingResponseCode.NETWORK_ERROR,
            BillingResponseCode.ITEM_UNAVAILABLE,
        ).forEach { code ->
            assertEquals("código $code", StoreResponse.Unavailable, storeResponseOf(code, emptyList()))
        }
    }

    @Test
    fun otherErrorsFail() {
        listOf(BillingResponseCode.ERROR, BillingResponseCode.DEVELOPER_ERROR, BillingResponseCode.ITEM_NOT_OWNED)
            .forEach { code -> assertEquals("código $code", StoreResponse.Failed, storeResponseOf(code, emptyList())) }
    }

    @Test
    fun paidAndPendingPurchasesAreKept() {
        assertEquals(
            StorePurchase("a", StorePurchase.State.Purchased, acknowledged = true),
            storePurchaseOf("a", PurchaseState.PURCHASED, acknowledged = true),
        )
        assertEquals(
            StorePurchase("b", StorePurchase.State.Pending, acknowledged = false),
            storePurchaseOf("b", PurchaseState.PENDING, acknowledged = false),
        )
    }

    @Test
    fun unspecifiedStateIsDropped() {
        assertNull(storePurchaseOf("c", PurchaseState.UNSPECIFIED_STATE, acknowledged = false))
    }
}
