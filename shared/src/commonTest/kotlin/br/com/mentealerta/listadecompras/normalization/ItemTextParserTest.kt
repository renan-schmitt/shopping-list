package br.com.mentealerta.listadecompras.normalization

import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.EmbeddedCatalog
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.domain.QuantityUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Texto de um produto → quantidade, unidade, produto e detalhe, com o catálogo real (#47). */
class ItemTextParserTest {
    private val parser = ItemTextParser(CatalogIndex(EmbeddedCatalog.catalog))

    /** Resultado sem o objeto do catálogo, para comparar só o que importa. */
    private data class Parsed(
        val name: String,
        val productId: String?,
        val quantity: Quantity? = null,
        val detail: String? = null,
    )

    private fun parse(text: String): Parsed? = parser.parse(text)?.let {
        Parsed(it.name, it.match?.product?.id, it.quantity, it.detail)
    }

    private fun assertParsed(text: String, expected: Parsed) = assertEquals(expected, parse(text), text)

    private fun qty(amount: Number, unit: QuantityUnit = QuantityUnit.UNIT) = Quantity(amount.toDouble(), unit)

    @Test
    fun examplesFromThePlan() {
        assertParsed("dois litros de leite", Parsed("leite", "milk", qty(2, QuantityUnit.LITER)))
        assertParsed("meio quilo de carne moída", Parsed("carne moída", "ground_beef", qty(0.5, QuantityUnit.KILOGRAM)))
        assertParsed("uma dúzia de ovos", Parsed("ovos", "egg", qty(1, QuantityUnit.DOZEN)))
        assertParsed("1 kg de carne moída", Parsed("carne moída", "ground_beef", qty(1, QuantityUnit.KILOGRAM)))
        assertParsed("2 leites", Parsed("leites", "milk", qty(2)))
    }

    @Test
    fun catalogVariantWinsOverDetail() {
        assertParsed(
            "dois litros de leite desnatado",
            Parsed("leite desnatado", "skim_milk", qty(2, QuantityUnit.LITER)),
        )
        assertParsed("arroz integral", Parsed("arroz integral", "brown_rice"))
        assertParsed("pão de queijo congelado", Parsed("pão de queijo congelado", "frozen_cheese_bread"))
    }

    @Test
    fun leftoverAfterTheProductIsTheDetailAsWritten() {
        assertParsed("café da marca X", Parsed("café", "coffee", detail = "da marca X"))
        assertParsed("tomate bem maduro", Parsed("tomate", "tomato", detail = "bem maduro"))
        assertParsed(
            "3 litros de Leite Desnatado da Marca Boa",
            Parsed("Leite Desnatado", "skim_milk", qty(3, QuantityUnit.LITER), "da Marca Boa"),
        )
        assertParsed("2 pacotes de café pilão", Parsed("café", "coffee", qty(2, QuantityUnit.PACKAGE), "pilão"))
    }

    @Test
    fun quantityIsOptional() {
        assertParsed("leite", Parsed("leite", "milk"))
        assertParsed("Carne moída", Parsed("Carne moída", "ground_beef"))
        assertParsed("feijão preto", Parsed("feijão preto", "black_beans"))
    }

    @Test
    fun numberWithoutUnitCountsUnits() {
        assertParsed("duas melancias", Parsed("melancias", "watermelon", qty(2)))
        assertParsed("dez ovos", Parsed("ovos", "egg", qty(10)))
        assertParsed("12 bananas", Parsed("bananas", "banana", qty(12)))
        assertParsed("meia melancia", Parsed("melancia", "watermelon", qty(0.5)))
    }

    @Test
    fun unitWithoutNumberCountsOne() {
        assertParsed("pacote de arroz", Parsed("arroz", "rice", qty(1, QuantityUnit.PACKAGE)))
        assertParsed("dúzia de ovos", Parsed("ovos", "egg", qty(1, QuantityUnit.DOZEN)))
        assertParsed("meia dúzia de ovos", Parsed("ovos", "egg", qty(1, QuantityUnit.HALF_DOZEN)))
        assertParsed(
            "meia dúzia de ovos caipira",
            Parsed("ovos caipira", "free_range_egg", qty(1, QuantityUnit.HALF_DOZEN)),
        )
    }

    @Test
    fun recognizesNumbersInManyForms() {
        assertParsed("dois e meio quilos de batata", Parsed("batata", "potato", qty(2.5, QuantityUnit.KILOGRAM)))
        assertParsed("1,5 kg de batata", Parsed("batata", "potato", qty(1.5, QuantityUnit.KILOGRAM)))
        assertParsed("0.5 kg de batata", Parsed("batata", "potato", qty(0.5, QuantityUnit.KILOGRAM)))
        assertParsed("TRÊS latas de sardinha", Parsed("sardinha", "sardine", qty(3, QuantityUnit.CAN)))
        assertParsed("tres latas de sardinha", Parsed("sardinha", "sardine", qty(3, QuantityUnit.CAN)))
        assertParsed("vinte pães franceses", Parsed("pães franceses", "french_bread", qty(20)))
    }

    @Test
    fun recognizesUnitsInManyForms() {
        assertParsed("2 kg arroz", Parsed("arroz", "rice", qty(2, QuantityUnit.KILOGRAM)))
        assertParsed("500 g de queijo", Parsed("queijo", "cheese", qty(500, QuantityUnit.GRAM)))
        assertParsed("200 gramas de presunto", Parsed("presunto", "ham", qty(200, QuantityUnit.GRAM)))
        assertParsed("2 cx de leite", Parsed("leite", "milk", qty(2, QuantityUnit.BOX)))
        assertParsed("uma caixinha de leite", Parsed("leite", "milk", qty(1, QuantityUnit.BOX)))
        assertParsed("duas garrafas de água", Parsed("água", "water", qty(2, QuantityUnit.BOTTLE)))
        assertParsed("um maço de alface", Parsed("alface", "lettuce", qty(1, QuantityUnit.BUNCH)))
        assertParsed("uma bandeja de morango", Parsed("morango", "strawberry", qty(1, QuantityUnit.TRAY)))
        assertParsed("um pote de requeijão", Parsed("requeijão", "cream_cheese_spread", qty(1, QuantityUnit.JAR)))
        assertParsed("3 unidades de abacate", Parsed("abacate", "avocado", qty(3)))
    }

    @Test
    fun connectorAfterTheUnitIsOptionalAndAnyGender() {
        assertParsed("dois litros leite", Parsed("leite", "milk", qty(2, QuantityUnit.LITER)))
        assertParsed(
            "um pacote da farinha de trigo",
            Parsed("farinha de trigo", "wheat_flour", qty(1, QuantityUnit.PACKAGE)),
        )
        assertParsed("um kg do feijão", Parsed("feijão", "beans", qty(1, QuantityUnit.KILOGRAM)))
    }

    @Test
    fun synonymsAndRegionalismsResolveToTheCanonicalProduct() {
        val parsed = parser.parse("dois quilos de aipim")
        assertEquals("aipim", parsed?.name)
        assertEquals("cassava", parsed?.match?.product?.id)
        assertEquals("aipim", parsed?.match?.term?.term)
        assertEquals(qty(2, QuantityUnit.KILOGRAM), parsed?.quantity)
        assertParsed("3 macaxeiras", Parsed("macaxeiras", "cassava", qty(3)))
    }

    @Test
    fun ignoresAccentsCaseAndPunctuation() {
        assertParsed("DOIS LITROS DE LEITE", Parsed("LEITE", "milk", qty(2, QuantityUnit.LITER)))
        assertParsed("meio quilo de carne moida", Parsed("carne moida", "ground_beef", qty(0.5, QuantityUnit.KILOGRAM)))
        assertParsed("  dois   litros de leite. ", Parsed("leite", "milk", qty(2, QuantityUnit.LITER)))
    }

    @Test
    fun numberOrUnitThatIsPartOfAProductName() {
        assertParsed("meia", Parsed("meia", "socks"))
        assertParsed("duas meias", Parsed("meias", "socks", qty(2)))
        assertParsed("uma caixa de fósforo", Parsed("caixa de fósforo", "matches", qty(1)))
        assertParsed("caixa de fósforo", Parsed("caixa de fósforo", "matches"))
        assertParsed("dois potes de sorvete", Parsed("potes de sorvete", "ice_cream", qty(2)))
    }

    @Test
    fun unknownProductKeepsTheWholeRestAsName() {
        assertParsed("dois pacotes de xyzabc", Parsed("xyzabc", null, qty(2, QuantityUnit.PACKAGE)))
        assertParsed("treco esquisito", Parsed("treco esquisito", null))
        assertParsed("3 trecos esquisitos", Parsed("trecos esquisitos", null, qty(3)))
    }

    @Test
    fun withoutProductThereIsNothingToAdd() {
        assertNull(parse(""))
        assertNull(parse("   "))
        assertNull(parse("2 kg"))
        assertNull(parse("dois litros de"))
    }
}
