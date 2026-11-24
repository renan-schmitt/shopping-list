package br.com.mentealerta.listadecompras.domain

import app.cash.turbine.test
import br.com.mentealerta.listadecompras.premium.Feature
import br.com.mentealerta.listadecompras.premium.Features
import br.com.mentealerta.listadecompras.premium.PremiumRepository
import br.com.mentealerta.listadecompras.premium.PurchaseResult
import br.com.mentealerta.listadecompras.premium.RestoreResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ObserveFeaturesUseCaseTest {
    private val repository = FakePremiumRepository()
    private val useCase = ObserveFeaturesUseCase(repository)

    @Test
    fun freeUnlocksNothingAndShowsThirtyDaysOfHistory() {
        val features = useCase.current()

        Feature.entries.forEach { assertFalse(features.isUnlocked(it), "$it liberada no grátis") }
        assertEquals(ObservePurchaseHistoryUseCase.FREE_HISTORY_DAYS, features.historyDays)
    }

    @Test
    fun completeUnlocksEverythingAndTheWholeHistory() {
        repository.isPremium.value = true

        val features = useCase.current()

        Feature.entries.forEach { assertTrue(features.isUnlocked(it), "$it bloqueada na versão completa") }
        assertNull(features.historyDays)
    }

    @Test
    fun unlocksAsSoonAsThePurchaseIsConfirmed() = runTest {
        useCase().test {
            assertEquals(Features.Free, awaitItem())
            repository.isPremium.value = true
            assertEquals(Features.Complete, awaitItem())
        }
    }

    @Test
    fun currentFollowsThePurchase() {
        assertEquals(Features.Free, useCase.current())
        repository.isPremium.value = true
        assertEquals(Features.Complete, useCase.current())
    }

    private class FakePremiumRepository : PremiumRepository {
        override val isPremium = MutableStateFlow(false)

        override fun start() = Unit

        override suspend fun price(): String? = null

        override suspend fun buy(): PurchaseResult = PurchaseResult.Failed

        override suspend fun restore(): RestoreResult = RestoreResult.Failed
    }
}
