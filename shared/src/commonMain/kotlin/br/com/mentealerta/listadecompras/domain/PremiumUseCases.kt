package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.premium.Features
import br.com.mentealerta.listadecompras.premium.PremiumRepository
import br.com.mentealerta.listadecompras.premium.PurchaseResult
import br.com.mentealerta.listadecompras.premium.RestoreResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Se a pessoa tem a versão completa (plano §5); muda na hora em que a compra é liberada. */
class ObservePremiumUseCase(private val repository: PremiumRepository) {
    operator fun invoke(): StateFlow<Boolean> = repository.isPremium
}

/**
 * O que está liberado (plano §5); muda na hora em que a compra é liberada. As telas perguntam por uma
 * funcionalidade com [Features.isUnlocked], nunca por "é premium?".
 */
class ObserveFeaturesUseCase(private val repository: PremiumRepository) {
    operator fun invoke(): Flow<Features> = repository.isPremium.map(Features::of).distinctUntilChanged()

    /** O que está liberado neste instante, para conferir na hora (ex.: antes de mostrar um anúncio). */
    fun current(): Features = Features.of(repository.isPremium.value)
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
