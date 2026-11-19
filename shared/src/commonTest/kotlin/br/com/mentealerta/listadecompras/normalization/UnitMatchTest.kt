package br.com.mentealerta.listadecompras.normalization

import br.com.mentealerta.listadecompras.domain.QuantityUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UnitMatchTest {
    private fun words(text: String) = text.split(" ")

    private fun assertUnit(expected: QuantityUnit, text: String, wordCount: Int = 1) =
        assertEquals(UnitMatch(expected, wordCount), matchUnit(words(text)), text)

    private fun assertUnits(expected: QuantityUnit, vararg texts: String) = texts.forEach { assertUnit(expected, it) }

    @Test
    fun recognizesUnit() = assertUnits(QuantityUnit.UNIT, "unidade", "unidades", "un", "und", "unid")

    @Test
    fun recognizesKilogram() = assertUnits(
        QuantityUnit.KILOGRAM,
        "kg", "kgs", "quilo", "quilos", "kilo", "kilos", "quilograma", "quilogramas", "kilograma",
    )

    @Test
    fun recognizesGram() = assertUnits(QuantityUnit.GRAM, "g", "gr", "grs", "grama", "gramas")

    @Test
    fun recognizesLiter() = assertUnits(QuantityUnit.LITER, "l", "lt", "lts", "litro", "litros")

    @Test
    fun recognizesDozen() = assertUnits(QuantityUnit.DOZEN, "dúzia", "dúzias", "duzia", "dz")

    @Test
    fun recognizesHalfDozenAsTwoWords() {
        assertUnit(QuantityUnit.HALF_DOZEN, "meia dúzia de ovos", wordCount = 2)
        assertUnit(QuantityUnit.HALF_DOZEN, "meia duzia", wordCount = 2)
        assertUnit(QuantityUnit.HALF_DOZEN, "meia dz", wordCount = 2)
    }

    @Test
    fun recognizesPackagingUnits() {
        assertUnits(QuantityUnit.PACKAGE, "pacote", "pacotes", "pct", "pacotinho")
        assertUnits(QuantityUnit.BOX, "caixa", "caixas", "cx", "cxs", "caixinha")
        assertUnits(QuantityUnit.CAN, "lata", "latas", "latinha", "latinhas")
        assertUnits(QuantityUnit.BOTTLE, "garrafa", "garrafas", "garrafinha")
        assertUnits(QuantityUnit.BUNCH, "maço", "maços", "maco", "macinho")
        assertUnits(QuantityUnit.TRAY, "bandeja", "bandejas", "bandejinha")
        assertUnits(QuantityUnit.JAR, "pote", "potes", "potinho")
    }

    @Test
    fun ignoresCaseAndAccents() {
        assertUnits(QuantityUnit.LITER, "LITROS", "Litro")
        assertUnits(QuantityUnit.KILOGRAM, "KG", "Quilos")
        assertUnits(QuantityUnit.BUNCH, "MAÇOS")
        assertUnit(QuantityUnit.HALF_DOZEN, "Meia Dúzia", wordCount = 2)
    }

    @Test
    fun onlyLooksAtTheFirstWords() {
        assertUnit(QuantityUnit.LITER, "litros de leite desnatado")
        assertUnit(QuantityUnit.KILOGRAM, "quilo de carne moída")
        assertNull(matchUnit(words("leite de um litro")))
    }

    @Test
    fun returnsNullWithoutUnit() {
        assertNull(matchUnit(words("leite")))
        assertNull(matchUnit(words("meia")))
        assertNull(matchUnit(words("meia calça")))
        assertNull(matchUnit(words("molho de tomate")))
        assertNull(matchUnit(emptyList()))
    }

    @Test
    fun everyUnitIsRecognized() {
        val samples = listOf(
            "unidade", "kg", "grama", "litro", "duzia", "meia duzia", "pacote", "caixa", "lata", "garrafa",
            "maco", "bandeja", "pote",
        )
        assertEquals(QuantityUnit.entries.toSet(), samples.mapNotNull { matchUnit(words(it))?.unit }.toSet())
    }
}
