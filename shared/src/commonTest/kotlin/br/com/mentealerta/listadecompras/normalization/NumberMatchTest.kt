package br.com.mentealerta.listadecompras.normalization

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NumberMatchTest {
    private fun words(text: String) = text.split(" ")

    private fun assertNumber(expected: Double, text: String, wordCount: Int = 1) =
        assertEquals(NumberMatch(expected, wordCount), matchNumber(words(text)), text)

    private fun assertNoNumber(text: String) = assertNull(matchNumber(words(text)), text)

    @Test
    fun recognizesOneToTwentyInWords() {
        val numbers = listOf(
            "um", "dois", "três", "quatro", "cinco", "seis", "sete", "oito", "nove", "dez",
            "onze", "doze", "treze", "quatorze", "quinze", "dezesseis", "dezessete", "dezoito", "dezenove", "vinte",
        )
        numbers.forEachIndexed { index, word -> assertNumber(index + 1.0, word) }
    }

    @Test
    fun recognizesFeminine() {
        assertNumber(1.0, "uma dúzia de ovos")
        assertNumber(2.0, "duas caixas de leite")
    }

    @Test
    fun recognizesAlternativeSpellings() {
        assertNumber(14.0, "catorze")
        assertNumber(16.0, "dezasseis")
        assertNumber(17.0, "dezassete")
        assertNumber(19.0, "dezanove")
    }

    @Test
    fun ignoresCaseAndAccents() {
        assertNumber(3.0, "Três")
        assertNumber(3.0, "tres")
        assertNumber(2.0, "DUAS")
    }

    @Test
    fun recognizesHalf() {
        assertNumber(0.5, "meio quilo de carne moída")
        assertNumber(0.5, "meia melancia")
    }

    @Test
    fun recognizesWholeAndHalf() {
        assertNumber(2.5, "dois e meio quilos", wordCount = 3)
        assertNumber(1.5, "uma e meia", wordCount = 3)
        assertNumber(1.5, "1 e meio", wordCount = 3)
    }

    @Test
    fun andWithoutHalfIsNotPartOfTheNumber() {
        assertNumber(2.0, "dois e três")
        assertNumber(2.0, "dois e")
    }

    @Test
    fun halfDozenIsAUnitNotANumber() {
        assertNoNumber("meia dúzia de ovos")
        assertNoNumber("meia duzia")
        assertNoNumber("meia dz")
    }

    @Test
    fun recognizesDigits() {
        assertNumber(2.0, "2 litros de leite")
        assertNumber(12.0, "12")
        assertNumber(1.5, "1,5 kg")
        assertNumber(1.5, "1.5")
        assertNumber(0.5, "0,5")
    }

    @Test
    fun rejectsZeroAndInvalidDigits() {
        assertNoNumber("0")
        assertNoNumber("0,0")
        assertNoNumber("-2")
        assertNoNumber("1,2,3")
        assertNoNumber("1,")
        assertNoNumber(",5")
        assertNoNumber("2kg")
    }

    @Test
    fun returnsNullWithoutNumber() {
        assertNoNumber("leite")
        assertNoNumber("e meio")
        assertNull(matchNumber(emptyList()))
    }

    @Test
    fun onlyLooksAtTheFirstWord() = assertNoNumber("leite dois")
}
