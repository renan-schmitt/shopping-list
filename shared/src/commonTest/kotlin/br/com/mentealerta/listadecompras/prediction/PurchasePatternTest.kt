package br.com.mentealerta.listadecompras.prediction

import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.EmbeddedCatalog
import br.com.mentealerta.listadecompras.domain.Purchase
import br.com.mentealerta.listadecompras.domain.PurchaseId
import br.com.mentealerta.listadecompras.domain.PurchasedItem
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

/** O intervalo de compra de cada produto e quando ele está faltando, com históricos sintéticos (#63). */
class PurchasePatternTest {
    private val index = CatalogIndex(EmbeddedCatalog.catalog)

    /** Horário de Brasília, sem horário de verão: UTC−3. */
    private val timeZone = TimeZone.of("America/Sao_Paulo")

    private val coffee = PurchasedItem(productId = "coffee", name = "Café", quantity = null)

    @Test
    fun needsThreePurchases() {
        assertEquals(emptyList(), patterns(buy("2026-11-01", coffee), buy("2026-11-11", coffee)))

        val pattern = patterns(buy("2026-11-01", coffee), buy("2026-11-11", coffee), buy("2026-11-21", coffee)).single()

        assertEquals(PurchasePattern("coffee", "coffee", "Café", LocalDate(2026, 11, 21), 10.0), pattern)
    }

    @Test
    fun intervalIsTheMedianSoATripDoesNotSpoilIt() {
        // Intervalos 7, 30 (viagem), 8.
        val pattern = patterns(*buyEvery(coffee, "2026-09-01", 7, 30, 8)).single()

        assertEquals(8.0, pattern.intervalDays)
    }

    @Test
    fun evenNumberOfIntervalsAveragesTheTwoInTheMiddle() {
        assertEquals(10.5, patterns(*buyEvery(coffee, "2026-09-01", 10, 11)).single().intervalDays)
        assertEquals(8.5, patterns(*buyEvery(coffee, "2026-09-01", 7, 8, 9, 30)).single().intervalDays)
    }

    @Test
    fun onlyTheLastFiveIntervalsCount() {
        // Comprava de 30 em 30 dias; agora compra de 7 em 7.
        val pattern = patterns(*buyEvery(coffee, "2026-01-01", 30, 30, 30, 7, 7, 7, 7, 7)).single()

        assertEquals(7.0, pattern.intervalDays)
    }

    @Test
    fun purchasesInAnyOrderAreSortedByDate() {
        val purchases = buyEvery(coffee, "2026-09-01", 5, 20).reversed()

        assertEquals(LocalDate(2026, 9, 26), patterns(*purchases.toTypedArray()).single().lastPurchase)
    }

    @Test
    fun twoPurchasesOnTheSameDayCountAsOne() {
        val pattern = patterns(
            buy("2026-11-01", coffee),
            buy("2026-11-11", coffee),
            buy("2026-11-11", coffee, hour = 20),
        )

        assertEquals(emptyList(), pattern)
    }

    @Test
    fun countsCalendarDaysInThePhoneTimeZone() {
        // 10/11 às 23h em Brasília é 11/11 em UTC: conta como 10/11.
        val lateAtNight = purchase(Instant.parse("2026-11-11T02:00:00Z"), coffee)

        val pattern = patterns(buy("2026-10-31", coffee), lateAtNight, buy("2026-11-20", coffee)).single()

        assertEquals(10.0, pattern.intervalDays)
    }

    @Test
    fun intervalShorterThanTwoDaysIsNotSuggested() {
        val bread = PurchasedItem(productId = "french_bread", name = "Pão", quantity = null)

        assertEquals(emptyList(), patterns(*buyEvery(bread, "2026-11-01", 1, 1, 2)))
        assertEquals(2.0, patterns(*buyEvery(bread, "2026-11-01", 2, 2)).single().intervalDays)
    }

    @Test
    fun namesOfTheSameCatalogProductCountTogetherAndTheLatestNameIsShown() {
        val pattern = patterns(
            buy("2026-11-01", PurchasedItem("cassava", "Mandioca", null)),
            buy("2026-11-08", PurchasedItem("cassava", "macaxeira", null)),
            buy("2026-11-15", PurchasedItem("cassava", "aipim", null)),
        ).single()

        assertEquals("cassava", pattern.key)
        assertEquals("aipim", pattern.name)
    }

    @Test
    fun unknownProductCountsByTheNormalizedName() {
        val pattern = patterns(
            buy("2026-11-01", PurchasedItem(null, "Sabão de coco", null)),
            buy("2026-11-11", PurchasedItem(null, "sabões de coco", null)),
            buy("2026-11-21", PurchasedItem(null, "SABAO DE COCO", null)),
        ).single()

        assertEquals("sabao de coco", pattern.key)
        assertEquals(null, pattern.productId)
    }

    @Test
    fun eachProductHasItsOwnPattern() {
        val milk = PurchasedItem("milk", "Leite", null)
        val purchases = listOf("2026-11-01", "2026-11-04", "2026-11-11", "2026-11-21").mapIndexed { i, day ->
            buy(day, *(if (i == 1) arrayOf(milk) else arrayOf(coffee, milk)))
        }

        val patterns = patterns(*purchases.toTypedArray()).associateBy { it.key }

        assertEquals(10.0, patterns.getValue("coffee").intervalDays)
        assertEquals(7.0, patterns.getValue("milk").intervalDays)
    }

    @Test
    fun overdueAfterTwentyPercentMoreThanTheInterval() {
        val pattern = pattern(lastPurchase = LocalDate(2026, 11, 1), interval = 10.0)

        assertFalse(pattern.isOverdue(LocalDate(2026, 11, 13))) // 12 dias = 1,2×
        assertTrue(pattern.isOverdue(LocalDate(2026, 11, 14))) // 13 dias
        assertEquals(13, pattern.daysSinceLastPurchase(LocalDate(2026, 11, 14)))
    }

    @Test
    fun stopsAfterThreeTimesTheInterval() {
        val pattern = pattern(lastPurchase = LocalDate(2026, 11, 1), interval = 10.0)

        assertTrue(pattern.isOverdue(LocalDate(2026, 12, 1))) // 30 dias = 3×
        assertFalse(pattern.isOverdue(LocalDate(2026, 12, 2))) // 31 dias
    }

    @Test
    fun activeUntilThreeTimesTheInterval() {
        val pattern = pattern(lastPurchase = LocalDate(2026, 11, 1), interval = 10.0)

        assertTrue(pattern.isActive(LocalDate(2026, 11, 2))) // 1 dia: ainda não falta, mas é de costume
        assertTrue(pattern.isActive(LocalDate(2026, 12, 1))) // 30 dias = 3×
        assertFalse(pattern.isActive(LocalDate(2026, 12, 2))) // 31 dias
    }

    @Test
    fun halfDayIntervalUsesTheExactLimit() {
        val pattern = pattern(lastPurchase = LocalDate(2026, 11, 1), interval = 2.5)

        assertFalse(pattern.isOverdue(LocalDate(2026, 11, 4))) // 3 dias = 1,2 × 2,5
        assertTrue(pattern.isOverdue(LocalDate(2026, 11, 5)))
    }

    @Test
    fun purchaseFromTheFutureIsNotOverdue() {
        val pattern = pattern(lastPurchase = LocalDate(2026, 11, 20), interval = 10.0)

        assertEquals(0, pattern.daysSinceLastPurchase(LocalDate(2026, 11, 10)))
        assertFalse(pattern.isOverdue(LocalDate(2026, 11, 10)))
    }

    @Test
    fun noHidesItUntilAnotherWholeInterval() {
        val pattern = pattern(lastPurchase = LocalDate(2026, 11, 1), interval = 10.0)
        val no = LocalDate(2026, 11, 14)

        assertFalse(pattern.isOverdue(LocalDate(2026, 11, 14), dismissedOn = no))
        assertFalse(pattern.isOverdue(LocalDate(2026, 11, 23), dismissedOn = no))
        assertTrue(pattern.isOverdue(LocalDate(2026, 11, 24), dismissedOn = no))
    }

    @Test
    fun noBeforeTheLastPurchaseNoLongerCounts() {
        val pattern = pattern(lastPurchase = LocalDate(2026, 11, 20), interval = 10.0)

        assertTrue(pattern.isOverdue(LocalDate(2026, 12, 3), dismissedOn = LocalDate(2026, 11, 14)))
    }

    private fun patterns(vararg purchases: Purchase) = purchasePatterns(purchases.toList(), index, timeZone)

    private fun pattern(lastPurchase: LocalDate, interval: Double) =
        PurchasePattern("coffee", "coffee", "Café", lastPurchase, interval)

    /** Compra ao meio-dia em Brasília do dia [day] ("2026-11-01"), ou na [hour] dada. */
    private fun buy(day: String, vararg items: PurchasedItem, hour: Int = 12) =
        purchase(Instant.parse("${day}T${(hour + 3).toString().padStart(2, '0')}:00:00Z"), *items)

    /** Compras de [item] a partir de [first], com os intervalos dados em dias. */
    private fun buyEvery(item: PurchasedItem, first: String, vararg intervals: Int): Array<Purchase> {
        var day = LocalDate.parse(first)
        val days = listOf(day) + intervals.map {
            day = day.plus(it, DateTimeUnit.DAY)
            day
        }
        return days.map { buy(it.toString(), item) }.toTypedArray()
    }

    private fun purchase(date: Instant, vararg items: PurchasedItem) =
        Purchase(PurchaseId.random(), date, items.toList())
}
