package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.EmbeddedCatalog
import br.com.mentealerta.listadecompras.normalization.ItemTextParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Produtos a partir do que a pessoa falou (#60, plano §3.3). */
class ParseSpeechUseCaseTest {
    private val index = CatalogIndex(EmbeddedCatalog.catalog)
    private val parseSpeech = ParseSpeechUseCase(ItemTextParser(index))

    private fun ids(vararg segments: List<String>) = parseSpeech(segments.toList()).map { it.match?.product?.id }

    @Test
    fun severalProductsInOnePhrase() {
        val items = parseSpeech(listOf(listOf("arroz feijão e dois litros de leite")))

        assertEquals(listOf("rice", "beans", "milk"), items.map { it.match?.product?.id })
        assertEquals(Quantity(2.0, QuantityUnit.LITER), items.last().quantity)
    }

    @Test
    fun eachPauseIsSeparatedOnItsOwn() {
        assertEquals(listOf("rice", "beans"), ids(listOf("arroz"), listOf("feijão")))
    }

    @Test
    fun picksTheAlternativeWithMoreCatalogProducts() {
        assertEquals(listOf("rice", "beans"), ids(listOf("arros fei jão", "arroz e feijão")))
    }

    @Test
    fun onTieKeepsTheMostLikelyAlternative() {
        val items = parseSpeech(listOf(listOf("xyzzy", "plugh")))

        assertEquals(listOf("xyzzy"), items.map { it.name })
    }

    @Test
    fun nothingHeardGivesNoProducts() {
        assertTrue(parseSpeech(emptyList()).isEmpty())
        assertTrue(parseSpeech(listOf(listOf("2 kg"))).isEmpty())
    }
}
