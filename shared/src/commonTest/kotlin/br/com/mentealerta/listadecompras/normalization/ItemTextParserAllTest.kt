package br.com.mentealerta.listadecompras.normalization

import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.EmbeddedCatalog
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.domain.QuantityUnit
import kotlin.test.Test
import kotlin.test.assertEquals

/** Frase com vários produtos → um item por produto, com o catálogo real (#48). */
class ItemTextParserAllTest {
    private val parser = ItemTextParser(CatalogIndex(EmbeddedCatalog.catalog))

    /** Resultado sem o objeto do catálogo, para comparar só o que importa. */
    private data class Parsed(
        val name: String,
        val productId: String?,
        val quantity: Quantity? = null,
        val detail: String? = null,
    )

    private fun assertParsedAll(text: String, vararg expected: Parsed) = assertEquals(
        expected.toList(),
        parser.parseAll(text).map { Parsed(it.name, it.match?.product?.id, it.quantity, it.detail) },
        text,
    )

    private fun qty(amount: Number, unit: QuantityUnit = QuantityUnit.UNIT) = Quantity(amount.toDouble(), unit)

    private val rice = Parsed("arroz", "rice")
    private val beans = Parsed("feijão", "beans")
    private val milk = Parsed("leite", "milk")

    @Test
    fun exampleFromTheIssue() {
        val twoLitersOfMilk = Parsed("leite", "milk", qty(2, QuantityUnit.LITER))
        assertParsedAll("arroz, feijão e dois litros de leite", rice, beans, twoLitersOfMilk)
        assertParsedAll("arroz feijão e dois litros de leite", rice, beans, twoLitersOfMilk)
        assertParsedAll("arroz feijão dois litros de leite", rice, beans, twoLitersOfMilk)
    }

    @Test
    fun withoutPunctuationLikeVoice() {
        assertParsedAll("arroz feijão e leite", rice, beans, milk)
        assertParsedAll("arroz feijão leite", rice, beans, milk)
        assertParsedAll(
            "Arroz Feijão Leite",
            Parsed("Arroz", "rice"),
            Parsed("Feijão", "beans"),
            Parsed("Leite", "milk"),
        )
        assertParsedAll(
            "dois quilos de arroz uma dúzia de ovos",
            Parsed("arroz", "rice", qty(2, QuantityUnit.KILOGRAM)),
            Parsed("ovos", "egg", qty(1, QuantityUnit.DOZEN)),
        )
        assertParsedAll(
            "3 bananas 2 melancias",
            Parsed("bananas", "banana", qty(3)),
            Parsed("melancias", "watermelon", qty(2)),
        )
    }

    @Test
    fun separatesOnPunctuationAndLineBreaks() {
        assertParsedAll("arroz;feijão;leite", rice, beans, milk)
        assertParsedAll("arroz. feijão. leite.", rice, beans, milk)
        assertParsedAll("arroz\nfeijão\r\nleite", rice, beans, milk)
        assertParsedAll("arroz,, feijão ,e leite", rice, beans, milk)
    }

    @Test
    fun commaAndPeriodBetweenDigitsAreDecimals() {
        assertParsedAll(
            "1,5 kg de batata, 0.5 kg de carne moída",
            Parsed("batata", "potato", qty(1.5, QuantityUnit.KILOGRAM)),
            Parsed("carne moída", "ground_beef", qty(0.5, QuantityUnit.KILOGRAM)),
        )
    }

    @Test
    fun catalogNamesWithAndOrOfStayWhole() {
        assertParsedAll("biscoito água e sal e leite", Parsed("biscoito água e sal", "water_cracker"), milk)
        assertParsedAll("biscoito água e sal leite", Parsed("biscoito água e sal", "water_cracker"), milk)
        assertParsedAll("leite de coco arroz", Parsed("leite de coco", "coconut_milk"), rice)
        assertParsedAll(
            "farinha de trigo e açúcar",
            Parsed("farinha de trigo", "wheat_flour"),
            Parsed("açúcar", "sugar"),
        )
        assertParsedAll(
            "leite desnatado feijão preto",
            Parsed("leite desnatado", "skim_milk"),
            Parsed("feijão preto", "black_beans"),
        )
    }

    @Test
    fun andSeparatesProductsOutsideCatalogNames() {
        assertParsedAll("biscoito e sal", Parsed("biscoito", "biscuit"), Parsed("sal", "salt"))
        assertParsedAll(
            "dois e meio quilos de batata e sal",
            Parsed("batata", "potato", qty(2.5, QuantityUnit.KILOGRAM)),
            Parsed("sal", "salt"),
        )
    }

    @Test
    fun linkingWordBeforeAProductDoesNotSeparate() {
        assertParsedAll("frango com batata", Parsed("frango", "whole_chicken", detail = "com batata"))
        assertParsedAll("café sem açúcar e leite", Parsed("café", "coffee", detail = "sem açúcar"), milk)
        assertParsedAll("queijo para pão de queijo", Parsed("queijo", "cheese", detail = "para pão de queijo"))
    }

    @Test
    fun wordsWithoutBoundaryAreDetailOfThePreviousProduct() {
        assertParsedAll("café da marca X arroz", Parsed("café", "coffee", detail = "da marca X"), rice)
        assertParsedAll("tomate bem maduro e leite", Parsed("tomate", "tomato", detail = "bem maduro"), milk)
        assertParsedAll("café três corações", Parsed("café", "coffee", detail = "três corações"))
        assertParsedAll("leite dois litros", Parsed("leite", "milk", detail = "dois litros"))
    }

    @Test
    fun unknownProductGoesUntilTheNextBoundary() {
        assertParsedAll("treco esquisito arroz", Parsed("treco esquisito", null), rice)
        assertParsedAll("arroz treco esquisito", Parsed("arroz", "rice", detail = "treco esquisito"))
        assertParsedAll("arroz e treco esquisito", rice, Parsed("treco esquisito", null))
        assertParsedAll("dois pacotes de xyzabc leite", Parsed("xyzabc", null, qty(2, QuantityUnit.PACKAGE)), milk)
        assertParsedAll("xyzabc 2 leites", Parsed("xyzabc", null), Parsed("leites", "milk", qty(2)))
    }

    @Test
    fun knownLimitTwoUnknownProductsInARowAreOneItem() {
        assertParsedAll("xyzabc qwerty", Parsed("xyzabc qwerty", null))
    }

    @Test
    fun singleProductIsTheSameAsParse() {
        val texts = listOf(
            "dois litros de leite desnatado",
            "café da marca X",
            "uma caixa de fósforo",
            "meia dúzia de ovos",
        )
        texts.forEach {
            assertEquals(listOf(parser.parse(it)), parser.parseAll(it), it)
        }
    }

    @Test
    fun withoutProductThereIsNothingToAdd() {
        assertParsedAll("")
        assertParsedAll("  ,  . ; \n ")
        assertParsedAll("e")
        assertParsedAll("e e, e")
        assertParsedAll("2 kg")
        assertParsedAll("e leite", milk)
        assertParsedAll("arroz e", rice)
    }
}
