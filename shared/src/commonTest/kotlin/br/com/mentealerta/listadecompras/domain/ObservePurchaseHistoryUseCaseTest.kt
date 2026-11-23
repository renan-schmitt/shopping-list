package br.com.mentealerta.listadecompras.domain

import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant

class ObservePurchaseHistoryUseCaseTest {
    /** Horário de Brasília, sem horário de verão: UTC−3. */
    private val timeZone = TimeZone.of("America/Sao_Paulo")

    /** Agora: 23/11/2026, 1h da manhã em Brasília. */
    private val clock = object : Clock {
        override fun now(): Instant = Instant.parse("2026-11-23T04:00:00Z")
    }
    private val repository = FakeHistoryRepository()
    private val useCase = ObservePurchaseHistoryUseCase(repository, clock, timeZone)

    @Test
    fun countsCalendarDaysInThePhoneTimeZone() = runTest {
        val lastNight = purchase("2026-11-23T02:00:00Z") // 22/11, 23h em Brasília
        val thisMorning = purchase("2026-11-23T03:30:00Z") // 23/11, 0h30
        val twelveDaysAgo = purchase("2026-11-11T15:00:00Z")
        repository.purchases.value = listOf(thisMorning, lastNight, twelveDaysAgo)

        useCase().test {
            assertEquals(
                listOf(PastPurchase(thisMorning, 0), PastPurchase(lastNight, 1), PastPurchase(twelveDaysAgo, 12)),
                awaitItem(),
            )
        }
    }

    @Test
    fun freeVersionShowsTheLastThirtyDays() = runTest {
        val thirtyDaysAgo = purchase("2026-10-24T03:00:00Z") // 24/10, 0h em Brasília
        val thirtyOneDaysAgo = purchase("2026-10-24T02:59:59Z") // 23/10, 23h59
        repository.purchases.value = listOf(thirtyDaysAgo, thirtyOneDaysAgo)

        useCase().test { assertEquals(listOf(PastPurchase(thirtyDaysAgo, 30)), awaitItem()) }
    }

    @Test
    fun withoutLimitShowsEverything() = runTest {
        val longAgo = purchase("2025-01-10T12:00:00Z")
        repository.purchases.value = listOf(longAgo)

        useCase(historyDays = null).test { assertEquals(listOf(PastPurchase(longAgo, 682)), awaitItem()) }
    }

    @Test
    fun purchaseFromTheFutureCountsAsToday() = runTest {
        val future = purchase("2026-11-25T12:00:00Z")
        repository.purchases.value = listOf(future)

        useCase().test { assertEquals(listOf(PastPurchase(future, 0)), awaitItem()) }
    }

    private fun purchase(date: String) = Purchase.create(
        date = Instant.parse(date),
        items = listOf(ListItem.create(name = "café", productId = "coffee", sectionId = "grocery")),
    )

    /** Filtra pela data como o banco. */
    private class FakeHistoryRepository : PurchaseHistoryRepository {
        val purchases = MutableStateFlow(emptyList<Purchase>())

        override fun observePurchases(since: Instant?): Flow<List<Purchase>> =
            purchases.map { all -> all.filter { since == null || it.date >= since } }
    }
}
