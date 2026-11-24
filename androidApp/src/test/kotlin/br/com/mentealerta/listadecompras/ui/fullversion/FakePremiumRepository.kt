package br.com.mentealerta.listadecompras.ui.fullversion

import br.com.mentealerta.listadecompras.premium.PremiumRepository
import br.com.mentealerta.listadecompras.premium.PurchaseResult
import br.com.mentealerta.listadecompras.premium.RestoreResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Loja falsa da versão completa para os testes da tela (#88): [buyResult] e [restoreResult] dizem como
 * a loja responde; `Purchased` e `Restored` liberam a versão completa, como o repositório de verdade.
 * Com [gate], a loja só responde quando o teste completar o `CompletableDeferred` (janela aberta).
 */
class FakePremiumRepository(var price: String? = "R$ 19,90") : PremiumRepository {
    override val isPremium = MutableStateFlow(false)

    var buyResult = PurchaseResult.Purchased
    var restoreResult = RestoreResult.NothingToRestore
    var gate: CompletableDeferred<Unit>? = null

    var buyCalls = 0
        private set

    override fun start() = Unit

    override suspend fun price(): String? = price

    override suspend fun buy(): PurchaseResult {
        buyCalls++
        gate?.await()
        if (buyResult == PurchaseResult.Purchased) isPremium.value = true
        return buyResult
    }

    override suspend fun restore(): RestoreResult {
        gate?.await()
        if (restoreResult == RestoreResult.Restored) isPremium.value = true
        return restoreResult
    }
}
