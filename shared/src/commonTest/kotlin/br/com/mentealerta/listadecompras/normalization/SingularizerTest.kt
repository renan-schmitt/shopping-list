package br.com.mentealerta.listadecompras.normalization

import kotlin.test.Test
import kotlin.test.assertEquals

class SingularizerTest {
    private fun assertSingular(expected: String, plural: String) = assertEquals(expected, singularize(plural), plural)

    @Test
    fun removesFinalS() {
        assertSingular("banana", "bananas")
        assertSingular("tomate", "tomates")
        assertSingular("abacaxi", "abacaxis")
        assertSingular("caqui", "caquis")
        assertSingular("maca", "macas")
        assertSingular("ovo", "ovos")
        assertSingular("cafe", "cafes")
        assertSingular("chuchu", "chuchus")
        assertSingular("grao", "graos")
        assertSingular("alface", "alfaces")
        assertSingular("maionese", "maioneses")
        assertSingular("nugget", "nuggets")
    }

    @Test
    fun turnsAoPluralsIntoAo() {
        assertSingular("limao", "limoes")
        assertSingular("feijao", "feijoes")
        assertSingular("pimentao", "pimentoes")
        assertSingular("melao", "meloes")
        assertSingular("camarao", "camaroes")
        assertSingular("pao", "paes")
        assertSingular("mao", "maos")
    }

    @Test
    fun turnsIsPluralsIntoL() {
        assertSingular("cereal", "cereais")
        assertSingular("integral", "integrais")
        assertSingular("sal", "sais")
        assertSingular("pastel", "pasteis")
        assertSingular("descartavel", "descartaveis")
        assertSingular("lencol", "lencois")
        assertSingular("girassol", "girassois")
    }

    @Test
    fun turnsNsIntoM() {
        assertSingular("atum", "atuns")
        assertSingular("pudim", "pudins")
        assertSingular("vagem", "vagens")
        assertSingular("bombom", "bombons")
    }

    @Test
    fun removesEsAfterROrZ() {
        assertSingular("acucar", "acucares")
        assertSingular("colher", "colheres")
        assertSingular("hamburguer", "hamburgueres")
        assertSingular("flor", "flores")
        assertSingular("licor", "licores")
        assertSingular("noz", "nozes")
    }

    @Test
    fun usesIrregularPlurals() {
        assertSingular("mae", "maes")
        assertSingular("frances", "franceses")
        assertSingular("ingles", "ingleses")
        assertSingular("japones", "japoneses")
        assertSingular("mes", "meses")
        assertSingular("gas", "gases")
        assertSingular("pure", "pures")
        assertSingular("pernil", "pernis")
        assertSingular("barril", "barris")
        assertSingular("funil", "funis")
        assertSingular("azul", "azuis")
        assertSingular("boi", "bois")
        assertSingular("util", "uteis")
    }

    @Test
    fun keepsWordsThatEndInSInTheSingular() {
        listOf("lapis", "ananas", "gas", "mes", "pires", "onibus", "virus", "simples", "anis", "frances", "ingles")
            .forEach { assertSingular(it, it) }
    }

    @Test
    fun keepsSingularWords() {
        listOf("arroz", "leite", "limao", "pao", "acucar", "pastel", "atum", "feijao", "cuscuz", "noz")
            .forEach { assertSingular(it, it) }
    }

    @Test
    fun singularizesEveryWordOfAPhrase() {
        assertSingular("pao frances", "paes franceses")
        assertSingular("feijao preto", "feijoes pretos")
        assertSingular("pao de queijo", "paes de queijo")
        assertSingular("lencol de casal", "lencois de casal")
    }

    @Test
    fun treatsHyphenAsWordSeparator() {
        assertSingular("grao-de-bico", "graos-de-bico")
        assertSingular("couve-flor", "couves-flores")
        assertSingular("banana-da-terra", "bananas-da-terra")
        assertSingular("erva-mate", "erva-mate")
    }

    @Test
    fun keepsShortWordsAndWordsWithDigits() {
        assertSingular("os", "os")
        assertSingular("leite 2l", "leites 2l")
        assertSingular("12ovos", "12ovos")
        assertSingular("", "")
    }

    @Test
    fun singularAndPluralShareTheSearchKey() {
        listOf(
            "Maçã" to "Maçãs",
            "Pão francês" to "Pães franceses",
            "Feijão" to "Feijões",
            "Limão" to "limões",
            "Pastel" to "pastéis",
            "Açúcar" to "açúcares",
            "Purê" to "purês",
            "Grão-de-bico" to "Grãos-de-bico",
            "Mãe" to "mães",
        ).forEach { (singular, plural) -> assertEquals(searchKey(singular), searchKey(plural), plural) }
    }

    @Test
    fun searchKeyNormalizesThenSingularizes() {
        assertEquals("pao frances", searchKey("  PÃES   Franceses "))
    }
}
