package br.com.mentealerta.listadecompras.domain

import app.cash.turbine.test
import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.EmbeddedCatalog
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
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
    private val history = InMemoryHistoryRepository()
    private val list = ReadOnlyListRepository(index)
    private val dismissals = InMemoryDismissalRepository()
    private val useCase = ObserveOverdueProductsUseCase(history, list, dismissals, index, clock, timeZone)

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

    @Test
    fun noHidesTheProductUntilAnotherInterval() = runTest {
        // Café de 10 em 10 dias, última há 13.
        history.purchases.value = buyEvery("coffee", "Café", "2026-10-23", 10, 10)

        useCase().test {
            assertEquals(listOf("Café"), awaitItem().map { it.pattern.name })

            // "Não" há 9 dias, às 23h de Brasília (já 2h do dia seguinte em UTC): ainda escondido.
            dismissals.values.value = mapOf("coffee" to Instant.parse("2026-11-17T02:00:00Z"))
            assertEquals(emptyList(), awaitItem())

            // "Não" há 10 dias: passou um intervalo inteiro, volta.
            dismissals.values.value = mapOf("coffee" to Instant.parse("2026-11-15T15:00:00Z"))
            assertEquals(listOf("Café"), awaitItem().map { it.pattern.name })
        }
    }
}
