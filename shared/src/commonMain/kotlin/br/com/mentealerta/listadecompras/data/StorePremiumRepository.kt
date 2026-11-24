package br.com.mentealerta.listadecompras.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import br.com.mentealerta.listadecompras.premium.BillingStore
import br.com.mentealerta.listadecompras.premium.PremiumRepository
import br.com.mentealerta.listadecompras.premium.PurchaseResult
import br.com.mentealerta.listadecompras.premium.RestoreResult
import br.com.mentealerta.listadecompras.premium.StorePurchase
import br.com.mentealerta.listadecompras.premium.StoreResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * [PremiumRepository] sobre a [BillingStore] do celular, com a última resposta guardada nas
 * preferências (plano §4.3), para funcionar sem internet e não piscar ao abrir. Como no app Mente
 * Alerta:
 * - toda compra paga é confirmada à loja (sem isso o Google devolve o dinheiro em 3 dias); se a
 *   confirmação falhar, tenta de novo na próxima vez que o app abrir;
 * - pagamento pendente (Pix) só libera quando a loja confirma;
 * - a versão completa só sai quando a loja responde que a conta não tem a compra (reembolso, outra
 *   conta); sem resposta da loja, vale o que ficou guardado.
 */
@OptIn(ExperimentalAtomicApi::class)
internal class StorePremiumRepository(
    private val store: BillingStore,
    private val dataStore: DataStore<Preferences>,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : PremiumRepository {
    private val mutex = Mutex()
    private val started = AtomicBoolean(false)

    override val isPremium: StateFlow<Boolean> =
        dataStore.data.map { it[PREMIUM_OWNED] == true }.stateIn(scope, SharingStarted.Eagerly, false)

    override fun start() {
        if (!started.compareAndSet(expectedValue = false, newValue = true)) return
        scope.launch { store.updates.collect { purchases -> mutex.withLock { apply(purchases) } } }
        scope.launch { restore() }
    }

    override suspend fun price(): String? = store.price()

    override suspend fun buy(): PurchaseResult = when (val response = store.purchase()) {
        is StoreResponse.Success -> when (mutex.withLock { apply(response.purchases) }) {
            StorePurchase.State.Purchased -> PurchaseResult.Purchased
            StorePurchase.State.Pending -> PurchaseResult.Pending
            null -> PurchaseResult.Failed
        }
        // A loja diz que já é dela: busca a compra para confirmar e guardar.
        StoreResponse.AlreadyOwned -> when (restore()) {
            RestoreResult.Restored -> PurchaseResult.Purchased
            RestoreResult.Pending -> PurchaseResult.Pending
            RestoreResult.Unavailable -> PurchaseResult.Unavailable
            RestoreResult.NothingToRestore, RestoreResult.Failed -> PurchaseResult.Failed
        }
        StoreResponse.Cancelled -> PurchaseResult.Cancelled
        StoreResponse.Unavailable -> PurchaseResult.Unavailable
        StoreResponse.Failed -> PurchaseResult.Failed
    }

    override suspend fun restore(): RestoreResult = when (val response = store.queryPurchases()) {
        is StoreResponse.Success -> mutex.withLock {
            when (apply(response.purchases)) {
                StorePurchase.State.Purchased -> RestoreResult.Restored
                StorePurchase.State.Pending -> RestoreResult.Pending.also { setPremium(false) }
                null -> RestoreResult.NothingToRestore.also { setPremium(false) }
            }
        }
        StoreResponse.Unavailable -> RestoreResult.Unavailable
        StoreResponse.Cancelled, StoreResponse.AlreadyOwned, StoreResponse.Failed -> RestoreResult.Failed
    }

    /**
     * Libera a versão completa se houver uma compra paga, confirmando à loja as que faltam. Devolve o
     * melhor estado encontrado (paga antes de pendente), ou `null` sem compra nenhuma. Não tira a
     * versão completa: só quem consultou todas as compras da conta ([restore]) pode fazer isso.
     */
    private suspend fun apply(purchases: List<StorePurchase>): StorePurchase.State? {
        val paid = purchases.filter { it.state == StorePurchase.State.Purchased }
        if (paid.isNotEmpty()) {
            setPremium(true)
            paid.filterNot { it.acknowledged }.forEach { store.acknowledge(it.token) }
            return StorePurchase.State.Purchased
        }
        return if (purchases.isNotEmpty()) StorePurchase.State.Pending else null
    }

    private suspend fun setPremium(owned: Boolean) {
        dataStore.edit { it[PREMIUM_OWNED] = owned }
    }

    private companion object {
        val PREMIUM_OWNED = booleanPreferencesKey("premium_owned")
    }
}
