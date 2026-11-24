package br.com.mentealerta.listadecompras.data

import app.cash.turbine.test
import br.com.mentealerta.listadecompras.data.preferences.createPreferencesDataStore
import br.com.mentealerta.listadecompras.premium.BillingStore
import br.com.mentealerta.listadecompras.premium.PurchaseResult
import br.com.mentealerta.listadecompras.premium.RestoreResult
import br.com.mentealerta.listadecompras.premium.StorePurchase
import br.com.mentealerta.listadecompras.premium.StoreResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

// runCurrent: deixa as coroutines do repositório rodarem antes de conferir
@OptIn(ExperimentalCoroutinesApi::class)
class StorePremiumRepositoryTest {
    private val path = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "${Uuid.random()}.preferences_pb"
    private val store = FakeBillingStore()

    @AfterTest
    fun tearDown() {
        FileSystem.SYSTEM.delete(path, mustExist = false)
    }

    private fun TestScope.repository(scope: CoroutineScope = backgroundScope) =
        StorePremiumRepository(store, createPreferencesDataStore(path.toString(), scope), scope)

    @Test
    fun startsWithoutPremium() = runTest {
        val repository = repository()

        assertFalse(repository.isPremium.first())
    }

    @Test
    fun buyingUnlocksAndAcknowledges() = runTest {
        val repository = repository()
        store.purchaseResponse = StoreResponse.Success(listOf(paid("a")))

        repository.isPremium.test {
            assertFalse(awaitItem())
            assertEquals(PurchaseResult.Purchased, repository.buy())
            assertTrue(awaitItem())
        }
        assertEquals(listOf("a"), store.acknowledged)
    }

    @Test
    fun alreadyAcknowledgedPurchaseIsNotAcknowledgedAgain() = runTest {
        val repository = repository()
        store.purchaseResponse = StoreResponse.Success(listOf(paid("a", acknowledged = true)))

        assertEquals(PurchaseResult.Purchased, repository.buy())
        assertTrue(store.acknowledged.isEmpty())
    }

    @Test
    fun cancellingKeepsTheFreeVersion() = runTest {
        val repository = repository()
        store.purchaseResponse = StoreResponse.Cancelled

        assertEquals(PurchaseResult.Cancelled, repository.buy())
        assertFalse(repository.isPremium.first())
    }

    @Test
    fun unavailableStoreIsReported() = runTest {
        val repository = repository()
        store.purchaseResponse = StoreResponse.Unavailable

        assertEquals(PurchaseResult.Unavailable, repository.buy())
    }

    @Test
    fun pendingPaymentUnlocksOnlyWhenPaid() = runTest {
        val repository = repository()
        repository.start()
        runCurrent()
        store.purchaseResponse = StoreResponse.Success(listOf(pending("a")))

        assertEquals(PurchaseResult.Pending, repository.buy())
        assertFalse(repository.isPremium.first())
        assertTrue(store.acknowledged.isEmpty())

        // O Pix foi pago com o app aberto: a loja avisa por fora da compra.
        // A gravação vai para outra linha de execução: espera a confirmação chegar à loja.
        store.updates.emit(listOf(paid("a")))

        assertEquals(listOf("a"), store.awaitAcknowledged())
        assertTrue(repository.isPremium.first())
    }

    @Test
    fun alreadyOwnedFetchesThePurchase() = runTest {
        val repository = repository()
        store.purchaseResponse = StoreResponse.AlreadyOwned
        store.queryResponse = StoreResponse.Success(listOf(paid("a")))

        assertEquals(PurchaseResult.Purchased, repository.buy())
        assertTrue(repository.isPremium.first())
        assertEquals(listOf("a"), store.acknowledged)
    }

    @Test
    fun restoreFindsThePurchase() = runTest {
        val repository = repository()
        store.queryResponse = StoreResponse.Success(listOf(paid("a", acknowledged = true)))

        assertEquals(RestoreResult.Restored, repository.restore())
        assertTrue(repository.isPremium.first())
    }

    @Test
    fun restoreWithoutPurchaseFindsNothing() = runTest {
        val repository = repository()

        assertEquals(RestoreResult.NothingToRestore, repository.restore())
        assertFalse(repository.isPremium.first())
    }

    @Test
    fun restoreWithPendingPaymentDoesNotUnlock() = runTest {
        val repository = repository()
        store.queryResponse = StoreResponse.Success(listOf(pending("a")))

        assertEquals(RestoreResult.Pending, repository.restore())
        assertFalse(repository.isPremium.first())
    }

    @Test
    fun startRestoresSilently() = runTest {
        val repository = repository()
        store.queryResponse = StoreResponse.Success(listOf(paid("a")))

        repository.start()

        assertEquals(listOf("a"), store.awaitAcknowledged())
        assertTrue(repository.isPremium.first())
    }

    @Test
    fun startTwiceAsksTheStoreOnce() = runTest {
        val repository = repository()

        repository.start()
        repository.start()
        runCurrent()

        assertEquals(1, store.queries)
    }

    @Test
    fun purchaseIsRememberedWithoutTheStore() = runTest {
        store.purchaseResponse = StoreResponse.Success(listOf(paid("a")))
        val firstRun = CoroutineScope(backgroundScope.coroutineContext + Job())
        repository(firstRun).buy()
        firstRun.cancel()
        runCurrent()

        // App aberto de novo, sem internet: vale o que ficou guardado.
        store.queryResponse = StoreResponse.Unavailable
        val reopened = repository()
        reopened.start()
        runCurrent()

        assertTrue(reopened.isPremium.first { it })
    }

    @Test
    fun refundTakesThePremiumAway() = runTest {
        val repository = repository()
        store.purchaseResponse = StoreResponse.Success(listOf(paid("a")))
        repository.buy()

        // A loja responde que a conta não tem mais a compra.
        store.queryResponse = StoreResponse.Success(emptyList())

        assertEquals(RestoreResult.NothingToRestore, repository.restore())
        assertFalse(repository.isPremium.first())
    }

    private fun paid(token: String, acknowledged: Boolean = false) =
        StorePurchase(token, StorePurchase.State.Purchased, acknowledged)

    private fun pending(token: String) = StorePurchase(token, StorePurchase.State.Pending, acknowledged = false)
}

private class FakeBillingStore : BillingStore {
    var purchaseResponse: StoreResponse = StoreResponse.Failed
    var queryResponse: StoreResponse = StoreResponse.Success(emptyList())
    private val acknowledgedTokens = MutableStateFlow(emptyList<String>())
    val acknowledged: List<String> get() = acknowledgedTokens.value
    var queries = 0
    override val updates = MutableSharedFlow<List<StorePurchase>>()

    override suspend fun price(): String = "R$ 19,90"

    override suspend fun queryPurchases(): StoreResponse = queryResponse.also { queries++ }

    override suspend fun purchase(): StoreResponse = purchaseResponse

    override suspend fun acknowledge(token: String): Boolean {
        acknowledgedTokens.update { it + token }
        return true
    }

    /** Espera a primeira confirmação, que pode vir depois de uma gravação em outra linha de execução. */
    suspend fun awaitAcknowledged(): List<String> = acknowledgedTokens.first { it.isNotEmpty() }
}
