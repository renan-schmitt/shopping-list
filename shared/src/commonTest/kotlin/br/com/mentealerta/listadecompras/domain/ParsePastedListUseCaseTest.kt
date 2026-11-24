package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.EmbeddedCatalog
import br.com.mentealerta.listadecompras.normalization.ItemTextParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Lista recebida pelo WhatsApp → produtos (#68), com o catálogo real e listas no formato das reais. */
class ParsePastedListUseCaseTest {
    private val index = CatalogIndex(EmbeddedCatalog.catalog)
    private val parsePasted = ParsePastedListUseCase(ItemTextParser(index), index)

    /** Resultado sem o objeto do catálogo, para comparar só o que importa. */
    private data class Parsed(
        val name: String,
        val productId: String?,
        val quantity: Quantity? = null,
        val detail: String? = null,
    )

    private fun assertPasted(text: String, vararg expected: Parsed) = assertEquals(
        expected.toList(),
        parsePasted(text).map { Parsed(it.name, it.match?.product?.id, it.quantity, it.detail) },
        text,
    )

    private fun qty(amount: Number, unit: QuantityUnit = QuantityUnit.UNIT) = Quantity(amount.toDouble(), unit)

    @Test
    fun oneProductPerLine() {
        assertPasted(
            "arroz\nfeijão\nleite\n",
            Parsed("arroz", "rice"),
            Parsed("feijão", "beans"),
            Parsed("leite", "milk"),
        )
    }

    @Test
    fun textSentByMandarALista() {
        // O formato do nosso "Mandar a lista" (#67) volta igual.
        assertPasted(
            """
            *Lista de compras*

            *Hortifrúti*
            - Banana (6)

            *Frios e laticínios*
            - Leite desnatado (2 litros)
            - Queijo
            """.trimIndent(),
            Parsed("Banana", "banana", qty(6)),
            Parsed("Leite desnatado", "skim_milk", qty(2, QuantityUnit.LITER)),
            Parsed("Queijo", "cheese"),
        )
    }

    @Test
    fun bulletsAndNumbering() {
        val expected = arrayOf(Parsed("Arroz", "rice"), Parsed("Feijão", "beans"), Parsed("Café", "coffee"))
        assertPasted("• Arroz\n• Feijão\n• Café", *expected)
        assertPasted("1. Arroz\n2. Feijão\n3. Café", *expected)
        assertPasted("1) Arroz\n2) Feijão\n3) Café", *expected)
        assertPasted("1- Arroz\n2 - Feijão\n3 – Café", *expected)
        assertPasted("☐ Arroz\n[ ] Feijão\n[x] Café", *expected)
        assertPasted("🛒 Arroz\n✅ Feijão\n▪️ Café", *expected)
    }

    @Test
    fun quantityAfterTheProduct() {
        assertPasted(
            "Arroz 5kg\nLeite 2 litros\nOvos 1 dúzia\nBanana x6\nTomate 2x\nLaranja meia dúzia",
            Parsed("Arroz", "rice", qty(5, QuantityUnit.KILOGRAM)),
            Parsed("Leite", "milk", qty(2, QuantityUnit.LITER)),
            Parsed("Ovos", "egg", qty(1, QuantityUnit.DOZEN)),
            Parsed("Banana", "banana", qty(6)),
            Parsed("Tomate", "tomato", qty(2)),
            Parsed("Laranja", "orange", qty(1, QuantityUnit.HALF_DOZEN)),
        )
    }

    @Test
    fun quantityBeforeTheProduct() {
        assertPasted(
            "2 kg de arroz\n5kg feijão\n1,5 kg de carne moída",
            Parsed("arroz", "rice", qty(2, QuantityUnit.KILOGRAM)),
            Parsed("feijão", "beans", qty(5, QuantityUnit.KILOGRAM)),
            Parsed("carne moída", "ground_beef", qty(1.5, QuantityUnit.KILOGRAM)),
        )
    }

    @Test
    fun copiedMessagesWithNameAndTime() {
        assertPasted(
            """
            [18:23, 24/11/2026] Maria: Mãe, traz do mercado:
            [18:23, 24/11/2026] Maria: arroz e feijão
            [18:24, 24/11/2026] Maria: 2 litros de leite
            24/11/2026 18:25 - Maria: café
            """.trimIndent(),
            Parsed("arroz", "rice"),
            Parsed("feijão", "beans"),
            Parsed("leite", "milk", qty(2, QuantityUnit.LITER)),
            Parsed("café", "coffee"),
        )
    }

    @Test
    fun sectionTitlesAndListTitlesAreSkipped() {
        assertPasted(
            """
            LISTA DO MERCADO
            Limpeza:
            detergente
            Hortifruti: alface, tomate
            Mercearia
            arroz
            """.trimIndent(),
            Parsed("detergente", "dish_detergent"),
            Parsed("alface", "lettuce"),
            Parsed("tomate", "tomato"),
            Parsed("arroz", "rice"),
        )
    }

    @Test
    fun severalProductsInOneLine() {
        assertPasted(
            "arroz, feijão, macarrão e óleo",
            Parsed("arroz", "rice"),
            Parsed("feijão", "beans"),
            Parsed("macarrão", "pasta"),
            Parsed("óleo", "cooking_oil"),
        )
    }

    @Test
    fun boldAndItalicMarkersAreRemoved() {
        assertPasted(
            "*Arroz*\n_Feijão_\n~Café~",
            Parsed("Arroz", "rice"),
            Parsed("Feijão", "beans"),
            Parsed("Café", "coffee"),
        )
    }

    @Test
    fun unknownProductKeepsTheName() {
        assertPasted("- Zabumbinha (2)", Parsed("Zabumbinha", null, qty(2)))
    }

    @Test
    fun sameProductTwiceEntersOnce() {
        assertPasted("Leite\nArroz\nleites (2)", Parsed("Leite", "milk"), Parsed("Arroz", "rice"))
    }

    @Test
    fun textWithoutProducts() {
        assertTrue(parsePasted("").isEmpty())
        assertTrue(parsePasted("*Lista de compras*\n\n- \n:)").isEmpty())
    }
}
