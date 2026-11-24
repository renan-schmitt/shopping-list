package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.premium.PremiumRepository
import br.com.mentealerta.listadecompras.premium.PurchaseResult
import br.com.mentealerta.listadecompras.premium.RestoreResult
import kotlinx.coroutines.flow.StateFlow

/** Se a pessoa tem a versão completa (plano §5); muda na hora em que a compra é liberada. */
class ObservePremiumUseCase(private val repository: PremiumRepository) {
    operator fun invoke(): StateFlow<Boolean> = repository.isPremium
}

/** Preço da versão completa formatado pela loja, ou `null` se a loja não respondeu. */
class GetPremiumPriceUseCase(private val repository: PremiumRepository) {
    suspend operator fun invoke(): String? = repository.price()
}

/** "Comprar a versão completa": abre a janela de pagamento da loja. */
class BuyPremiumUseCase(private val repository: PremiumRepository) {
    suspend operator fun invoke(): PurchaseResult = repository.buy()
}

/** "Já comprei": procura a compra na conta da loja (celular novo, app reinstalado). */
class RestorePremiumUseCase(private val repository: PremiumRepository) {
    suspend operator fun invoke(): RestoreResult = repository.restore()
}
