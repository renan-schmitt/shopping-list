package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.EmbeddedCatalog
import br.com.mentealerta.listadecompras.normalization.ItemTextParser
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Opções da tela de adicionar enquanto a pessoa digita (#53). */
class SearchProductsUseCaseTest {
    private val index = CatalogIndex(EmbeddedCatalog.catalog)
    private val preferredTerms = InMemoryPreferredTermRepository()
    private val search = SearchProductsUseCase(index, ItemTextParser(index), preferredTerms)

    private suspend fun foundIds(text: String) = search(text).found.map { it.match?.product?.id }

    @Test
    fun findsWithoutAccentsWhileTyping() = runTest {
        assertEquals("apple", foundIds("maca").first())
        assertEquals("Maçã", search("maca").found.first().name)
        assertEquals("cassava", foundIds("macax").first())
        assertEquals("beans", foundIds("fejao").first())
    }

    @Test
    fun exactProductComesFirstWithoutTheUnknownOption() = runTest {
        val options = search("Leites")

        assertEquals("milk", options.found.first().match?.product?.id)
        assertEquals("Leite", options.found.first().name)
        assertNull(options.unknown)
    }

    @Test
    fun regionalismKeepsTheTermThatMatched() = runTest {
        val first = search("macaxeiras").found.first()

        assertEquals("cassava", first.match?.product?.id)
        assertEquals("macaxeira", first.name)
    }

    @Test
    fun quantityGoesToEveryOption() = runTest {
        val options = search("2 litros de leite")
        val twoLiters = Quantity(2.0, QuantityUnit.LITER)

        assertEquals("milk", options.found.first().match?.product?.id)
        assertTrue(options.found.isNotEmpty())
        options.found.forEach { assertEquals(twoLiters, it.quantity) }
    }

    @Test
    fun detailStaysOnTheProductAndTheWholeTextIsAlsoOffered() = runTest {
        val options = search("café da marca X")

        val first = options.found.first()
        assertEquals("coffee", first.match?.product?.id)
        assertEquals("da marca X", first.detail)
        assertEquals("café da marca X", assertNotNull(options.unknown).name)
    }

    @Test
    fun unknownProductIsOfferedAsTyped() = runTest {
        val options = search("2 pacotes de xyzzy")

        assertTrue(options.found.isEmpty())
        val unknown = assertNotNull(options.unknown)
        assertEquals("xyzzy", unknown.name)
        assertNull(unknown.match)
        assertEquals(Quantity(2.0, QuantityUnit.PACKAGE), unknown.quantity)
    }

    @Test
    fun eachProductAppearsOnce() = runTest {
        listOf("maca", "leite", "pao", "arroz").forEach { text ->
            val ids = foundIds(text)
            assertEquals(ids.distinct(), ids, text)
        }
    }

    @Test
    fun firstOptionIsTheTopFoundOrTheUnknown() = runTest {
        assertEquals("apple", search("maca").first()?.match?.product?.id)
        assertEquals("xyzzy", search("xyzzy").first()?.name)
    }

    @Test
    fun unknownComesFirstWhenOnlyApproximateProductsWereFound() = runTest {
        val options = search("nescau")

        assertTrue(options.unknownFirst)
        assertEquals("nescau", options.first()?.name)
    }

    @Test
    fun productStartingWithTheTextStaysBeforeTheUnknown() = runTest {
        listOf("lei", "arr", "moid", "café da marca X").forEach { text ->
            assertFalse(search(text).unknownFirst, text)
        }
        assertEquals("milk", search("lei").first()?.match?.product?.id)
    }

    @Test
    fun blankTextOrOnlyQuantityHasNoOptions() = runTest {
        assertTrue(search("").isEmpty())
        assertTrue(search("   ").isEmpty())
        assertTrue(search("2 kg").isEmpty())
        assertNull(search("").first())
    }

    @Test
    fun approximateOptionsShowTheTermThePersonUses() = runTest {
        assertEquals("macaxeira", search("macax").found.first().name)

        preferredTerms.learn("cassava", "aipim")
        val first = search("macax").found.first()

        assertEquals("cassava", first.match?.product?.id)
        assertEquals("aipim", first.name)
    }

    @Test
    fun productNamedByTheTextKeepsTheTypedTerm() = runTest {
        preferredTerms.learn("cassava", "aipim")

        assertEquals("Mandioca", search("mandiocas").found.first().name)
    }
}
