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

/** "Você costuma comprar" na tela de adicionar (#65). */
class ObserveUsualProductsUseCaseTest {
    private val index = CatalogIndex(EmbeddedCatalog.catalog)

    /** Horário de Brasília, sem horário de verão: UTC−3. */
    private val timeZone = TimeZone.of("America/Sao_Paulo")

    /** Hoje: 25/11/2026, 1h da manhã em Brasília. */
    private val clock = object : Clock {
        override fun now(): Instant = Instant.parse("2026-11-25T04:00:00Z")
    }
    private val history = InMemoryHistoryRepository()
    private val list = ReadOnlyListRepository(index)
    private val terms = InMemoryPreferredTermRepository()
    private val useCase = ObserveUsualProductsUseCase(history, list, terms, index, clock, timeZone)

    @Test
    fun usualProductsComeEvenWhenNotMissingClosestToRunningOutFirst() = runTest {
        // Arroz de 7 em 7, última há 14 (2×); café de 10 em 10, última há 4 (0,4×).
        history.purchases.value = buyEvery("coffee", "Café", "2026-11-01", 10, 10) +
            buyEvery("rice", "Arroz", "2026-10-28", 7, 7)

        useCase().test { assertEquals(listOf("Arroz", "Café"), awaitItem().map { it.name }) }
    }

    @Test
    fun withoutAPatternOrStoppedIsLeftOut() = runTest {
        history.purchases.value =
            // Duas compras só.
            buyEvery("rice", "Arroz", "2026-11-10", 7) +
            // Pão de todo dia.
            buyEvery("french_bread", "Pão francês", "2026-11-21", 1, 1, 1) +
            // De 5 em 5 dias, última há 16 (mais de 3×): deixou de comprar.
            buyEvery("coffee", "Café", "2026-10-30", 5, 5)

        useCase().test { assertEquals(emptyList(), awaitItem()) }
    }

    @Test
    fun productAlreadyOnTheListIsLeftOut() = runTest {
        history.purchases.value = buyEvery("cassava", "Mandioca", "2026-11-01", 10, 10) +
            buyEvery(null, "Sabão de coco", "2026-11-01", 10, 10)

        useCase().test {
            assertEquals(listOf("Mandioca", "Sabão de coco"), awaitItem().map { it.name })

            list.items.value = listOf(ListItem.create(name = "aipim", productId = "cassava", sectionId = "produce"))
            assertEquals(listOf("Sabão de coco"), awaitItem().map { it.name })

            list.items.value = list.items.value +
                ListItem.create(name = "sabões de coco", productId = null, sectionId = "cleaning").check()
            assertEquals(emptyList(), awaitItem())
        }
    }

    @Test
    fun usesThePreferredTermOrTheNameOfTheLastPurchase() = runTest {
        terms.terms["cassava"] = "aipim"
        history.purchases.value = buyEvery("cassava", "Mandioca", "2026-11-01", 10, 10) +
            buyEvery("coffee", "café", "2026-11-01", 10, 10)

        useCase().test {
            val usual = awaitItem().associate { it.pattern.key to it.name }
            assertEquals(mapOf("cassava" to "aipim", "coffee" to "café"), usual)
        }
    }

    @Test
    fun showsAtMostTheLimit() = runTest {
        val products = listOf("rice", "beans", "coffee", "sugar", "salt", "pasta", "cassava", "onion", "garlic")
        history.purchases.value = products.flatMap { buyEvery(it, it, "2026-11-01", 10, 10) }

        useCase().test { assertEquals(ObserveUsualProductsUseCase.LIMIT, awaitItem().size) }
    }
}
