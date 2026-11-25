package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.CatalogSection
import br.com.mentealerta.listadecompras.catalog.EmbeddedCatalog
import br.com.mentealerta.listadecompras.data.EmbeddedCatalogRepository
import br.com.mentealerta.listadecompras.data.RoomShoppingListRepository
import br.com.mentealerta.listadecompras.data.RoomSuggestionDismissalRepository
import br.com.mentealerta.listadecompras.data.StepClock
import br.com.mentealerta.listadecompras.data.database.createTestDatabase
import br.com.mentealerta.listadecompras.prediction.PurchasePattern
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant

/** **Sim, pôr na lista** e **Não** do "Está faltando?" (#64). */
class SuggestionUseCasesTest {
    private val database = createTestDatabase()
    private val repository = RoomShoppingListRepository(database, EmbeddedCatalogRepository(), StepClock())
    private val index = CatalogIndex(EmbeddedCatalog.catalog)
    private val sectionChoices = InMemorySectionChoiceRepository()
    private val accept = AcceptSuggestionUseCase(repository, RememberSectionUseCase(index, sectionChoices), index)

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun yesPutsTheProductWithTheNameOfTheLastPurchaseInItsSection() = runTest {
        accept(pattern(key = "cassava", productId = "cassava", name = "aipim"))

        val sections = repository.observeSections().first()
        assertEquals(listOf("produce"), sections.map { it.section.id })
        val item = sections.single().items.single()
        assertEquals("aipim", item.name)
        assertEquals("cassava", item.productId)
        assertEquals(null, item.quantity)
    }

    @Test
    fun yesUsesTheSectionThePersonChose() = runTest {
        sectionChoices.sections["coffee"] = "beverages"

        assertEquals("beverages", accept(pattern(key = "coffee", productId = "coffee", name = "Café")).sectionId)
    }

    @Test
    fun usualProductEntersWithTheGivenName() = runTest {
        val item =
            accept(pattern(key = "french_bread", productId = "french_bread", name = "pão francês"), name = "cacetinho")

        assertEquals("cacetinho", item.name)
        assertEquals("french_bread", item.productId)
    }

    @Test
    fun yesPutsAnUnknownProductInOthers() = runTest {
        val item = accept(pattern(key = "sabao de coco", productId = null, name = "Sabão de coco"))

        assertEquals(CatalogSection.OTHER_ID, item.sectionId)
        assertEquals(null, item.productId)
    }

    @Test
    fun noRecordsTheProductKeyWithTheCurrentTime() = runTest {
        val now = Instant.parse("2026-11-25T15:00:00Z")
        val clock = object : Clock {
            override fun now(): Instant = now
        }
        val dismissals = RoomSuggestionDismissalRepository(database.learnedPreferenceDao())

        DismissSuggestionUseCase(dismissals, clock)(overdue(key = "cassava", productId = "cassava", name = "aipim"))

        assertEquals(mapOf("cassava" to now), dismissals.observeDismissals().first())
    }

    private fun pattern(key: String, productId: String?, name: String) =
        PurchasePattern(key, productId, name, LocalDate.parse("2026-11-10"), intervalDays = 10.0)

    private fun overdue(key: String, productId: String?, name: String) =
        OverdueProduct(pattern(key, productId, name), daysSinceLastPurchase = 15)
}
