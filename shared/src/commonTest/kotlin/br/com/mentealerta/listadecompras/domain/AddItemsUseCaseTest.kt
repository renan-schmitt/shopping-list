package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.CatalogSection
import br.com.mentealerta.listadecompras.catalog.EmbeddedCatalog
import br.com.mentealerta.listadecompras.data.EmbeddedCatalogRepository
import br.com.mentealerta.listadecompras.data.RoomShoppingListRepository
import br.com.mentealerta.listadecompras.data.StepClock
import br.com.mentealerta.listadecompras.data.database.createTestDatabase
import br.com.mentealerta.listadecompras.normalization.ItemTextParser
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

/** Texto entendido → itens da lista, inclusive produto que o app não reconhece (#49). */
class AddItemsUseCaseTest {
    private val database = createTestDatabase()
    private val repository = RoomShoppingListRepository(database, EmbeddedCatalogRepository(), StepClock())
    private val index = CatalogIndex(EmbeddedCatalog.catalog)
    private val preferredTerms = InMemoryPreferredTermRepository()
    private val addItems = AddItemsUseCase(repository, LearnPreferredTermUseCase(index, preferredTerms))
    private val parser = ItemTextParser(index)

    @AfterTest
    fun tearDown() {
        database.close()
    }

    private fun item(text: String): ListItem = checkNotNull(parser.parse(text)).toListItem()

    @Test
    fun knownProductTakesTheCatalogProductAndSection() {
        val item = item("dois litros de leite")

        assertEquals("leite", item.name)
        assertEquals("milk", item.productId)
        assertEquals("deli_dairy", item.sectionId)
        assertEquals(Quantity(2.0, QuantityUnit.LITER), item.quantity)
        assertFalse(item.checked)
    }

    @Test
    fun unknownProductIsAcceptedInOtherWithoutProduct() {
        val item = item("Bergamota")

        assertEquals("Bergamota", item.name)
        assertNull(item.productId)
        assertEquals(CatalogSection.OTHER_ID, item.sectionId)
        assertNull(item.quantity)
        assertNull(item.detail)
    }

    @Test
    fun unknownProductKeepsTheQuantity() {
        val item = item("2 pacotes de bolacha")

        assertEquals("bolacha", item.name)
        assertNull(item.productId)
        assertEquals(CatalogSection.OTHER_ID, item.sectionId)
        assertEquals(Quantity(2.0, QuantityUnit.PACKAGE), item.quantity)
    }

    @Test
    fun nameStartingWithAKnownProductIsThatProductWithDetail() {
        val item = item("café da marca X")

        assertEquals("coffee", item.productId)
        assertEquals("pantry", item.sectionId)
        assertEquals("da marca X", item.detail)
    }

    @Test
    fun addsKnownAndUnknownProductsOfASentenceInOrder() = runTest {
        val added = addItems(parser.parseAll("arroz, bergamota e dois litros de leite"))

        assertEquals(listOf("rice", null, "milk"), added.map { it.productId })
        assertEquals(
            listOf("deli_dairy" to listOf(added[2]), "pantry" to listOf(added[0]), "other" to listOf(added[1])),
            repository.observeSections().first().map { it.section.id to it.items },
        )
    }

    @Test
    fun addingNothingChangesNothing() = runTest {
        assertEquals(emptyList(), addItems(emptyList()))
        assertEquals(emptyList(), repository.observeSections().first())
    }

    @Test
    fun addingTeachesTheTermThePersonUses() = runTest {
        addItems(parser.parseAll("2 kg de macaxeiras, cacetinho da padaria e xyzzy"))

        assertEquals(mapOf("cassava" to "macaxeira", "french_bread" to "cacetinho"), preferredTerms.terms)
    }
}
