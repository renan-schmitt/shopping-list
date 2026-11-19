package br.com.mentealerta.listadecompras.normalization

import kotlin.test.Test
import kotlin.test.assertEquals

class EditDistanceTest {
    @Test
    fun countsChangedMissingAndExtraLetters() {
        assertEquals(0, editDistance("arroz", "arroz", max = 2))
        assertEquals(1, editDistance("arros", "arroz", max = 2))
        assertEquals(1, editDistance("fejao", "feijao", max = 2))
        assertEquals(1, editDistance("feijjao", "feijao", max = 2))
        assertEquals(2, editDistance("fjao", "feijao", max = 2))
    }

    @Test
    fun swappedNeighborLettersCountAsOneError() {
        assertEquals(1, editDistance("mcaa", "maca", max = 2))
        assertEquals(1, editDistance("macaxiera", "macaxeira", max = 2))
    }

    @Test
    fun stopsCountingPastTheLimit() {
        assertEquals(2, editDistance("banana", "xyz", max = 1))
        assertEquals(3, editDistance("leite", "manteiga", max = 2))
        assertEquals(1, editDistance("", "a", max = 0))
    }

    @Test
    fun emptyTexts() {
        assertEquals(0, editDistance("", "", max = 1))
        assertEquals(2, editDistance("ab", "", max = 2))
    }

    @Test
    fun prefixComparesWithTheBestBeginningAtLeastAsLongAsTheTypedText() {
        assertEquals(0, editDistance("macax", "macaxeira", max = 1, prefix = true))
        assertEquals(1, editDistance("mcax", "macaxeira", max = 1, prefix = true))
        assertEquals(1, editDistance("fejao", "feijao preto", max = 1, prefix = true))
        assertEquals(1, editDistance("bananna", "banana", max = 1, prefix = true))
        assertEquals(2, editDistance("pao", "pera", max = 1, prefix = true))
    }

    @Test
    fun startsAtAWordInTheMiddleOfTheTarget() {
        assertEquals(1, editDistance("moda", "carne moida", max = 1, prefix = true, targetStart = 6))
        assertEquals(0, editDistance("moida", "carne moida", max = 1, targetStart = 6))
    }

    @Test
    fun prefixIgnoresTheRestOfALongTarget() {
        assertEquals(1, editDistance("arros", "arroz parboilizado tipo um", max = 1, prefix = true))
    }

    @Test
    fun withoutPrefixTheWholeTargetCounts() {
        assertEquals(2, editDistance("macax", "macaxeira", max = 1))
    }
}
