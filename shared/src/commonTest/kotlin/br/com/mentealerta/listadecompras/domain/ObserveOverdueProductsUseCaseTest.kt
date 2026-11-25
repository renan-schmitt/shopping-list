package br.com.mentealerta.listadecompras.domain

import app.cash.turbine.test
import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.EmbeddedCatalog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant

/** Os produtos que estão faltando, a partir do histórico e da lista (#63). */
class ObserveOverdueProductsUseCaseTest {
    private val index = CatalogIndex(EmbeddedCatalog.catalog)

    /** Horário de Brasília, sem horário de verão: UTC−3. */
    private val timeZone = TimeZone.of("America/Sao_Paulo")

    /** Hoje: 25/11/2026, 1h da manhã em Brasília. */
    private val clock = object : Clock {
        override fun now(): Instant = Instant.parse("2026-11-25T04:00:00Z")
    }
    private val history = FakeHistoryRepository()
    private val list = FakeListRepository()
    private val useCase = ObserveOverdueProductsUseCase(history, list, index, clock, timeZone)

    @Test
    fun mostOverdueComesFirst() = runTest {
        // Café de 10 em 10 dias, última há 13 (1,3×); arroz de 7 em 7, última há 14 (2×).
        history.purchases.value = buyEvery("coffee", "Café", "2026-10-23", 10, 10) +
            buyEvery("rice", "Arroz", "2026-10-28", 7, 7)

        useCase().test {
            assertEquals(
                listOf("Arroz" to 14, "Café" to 13),
                awaitItem().map {
                    it.pattern.name to
                        it.daysSinceLastPurchase
                },
            )
        }
    }

    @Test
    fun productNotOverdueIsLeftOut() = runTest {
        history.purchases.value = buyEvery("coffee", "Café", "2026-11-01", 10, 10) // última há 4 dias

        useCase().test { assertEquals(emptyList(), awaitItem()) }
    }

    @Test
    fun productAlreadyOnTheListIsLeftOutEvenWithAnotherNameOrChecked() = runTest {
        history.purchases.value = buyEvery("cassava", "Mandioca", "2026-10-22", 10, 10) +
            buyEvery(null, "Sabão de coco", "2026-10-22", 10, 10)

        useCase().test {
            assertEquals(listOf("Mandioca", "Sabão de coco"), awaitItem().map { it.pattern.name })

            list.items.value = listOf(ListItem.create(name = "aipim", productId = "cassava", sectionId = "produce"))
            assertEquals(listOf("Sabão de coco"), awaitItem().map { it.pattern.name })

            list.items.value = list.items.value +
                ListItem.create(name = "sabões de coco", productId = null, sectionId = "cleaning").check()
            assertEquals(emptyList(), awaitItem())
        }
    }

    @Test
    fun buyingAgainTakesItOut() = runTest {
        val before = buyEvery("coffee", "Café", "2026-10-22", 10, 10)
        history.purchases.value = before

        useCase().test {
            assertEquals(listOf("Café"), awaitItem().map { it.pattern.name })

            history.purchases.value = before + buy("coffee", "Café", "2026-11-24")
            assertEquals(emptyList(), awaitItem())
        }
    }

    /** Compras ao meio-dia em Brasília a partir de [first], com os intervalos dados em dias. */
    private fun buyEvery(productId: String?, name: String, first: String, vararg intervals: Int): List<Purchase> {
        var day = LocalDate.parse(first)
        val days = listOf(day) + intervals.map {
            day = day.plus(it, DateTimeUnit.DAY)
            day
        }
        return days.map { buy(productId, name, it.toString()) }
    }

    private fun buy(productId: String?, name: String, day: String) = Purchase(
        PurchaseId.random(),
        Instant.parse("${day}T15:00:00Z"),
        listOf(PurchasedItem(productId, name, quantity = null)),
    )

    private class FakeHistoryRepository : PurchaseHistoryRepository {
        val purchases = MutableStateFlow(emptyList<Purchase>())

        override fun observePurchases(since: Instant?): Flow<List<Purchase>> =
            purchases.map { all -> all.filter { since == null || it.date >= since } }

        override fun observeHasPurchasesBefore(before: Instant): Flow<Boolean> =
            purchases.map { all -> all.any { it.date < before } }
    }

    /** Só a leitura da lista; as seções não importam para a previsão. */
    private inner class FakeListRepository : ShoppingListRepository {
        val items = MutableStateFlow(emptyList<ListItem>())

        override fun observeSections(): Flow<List<ListSection>> = items.map { items ->
            items.groupBy {
                it.sectionId
            }.map { (sectionId, inSection) -> ListSection(index.section(sectionId)!!, inSection) }
        }

        override suspend fun add(item: ListItem) = error("não usado")

        override suspend fun update(item: ListItem): ListItem? = error("não usado")

        override suspend fun setChecked(id: ListItemId, checked: Boolean): ListItem? = error("não usado")

        override suspend fun remove(id: ListItemId): RemovedItems = error("não usado")

        override suspend fun clear(): RemovedItems = error("não usado")

        override suspend fun restore(removed: RemovedItems) = error("não usado")

        override suspend fun finishShopping(): FinishedShopping? = error("não usado")

        override suspend fun undoFinishShopping(finished: FinishedShopping) = error("não usado")
    }
}
