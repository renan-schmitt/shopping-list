package br.com.mentealerta.listadecompras.normalization

import kotlin.test.Test
import kotlin.test.assertEquals

class TextNormalizerTest {
    @Test
    fun lowercasesAndRemovesAccents() {
        assertEquals("maca", normalize("Maçã"))
        assertEquals("pao frances", normalize("PÃO FRANCÊS"))
    }

    @Test
    fun removesPortugueseAccents() {
        assertEquals("acucar", normalize("açúcar"))
        assertEquals("feijao", normalize("feijão"))
        assertEquals("limoes", normalize("limões"))
        assertEquals("cafe", normalize("café"))
        assertEquals("pessego", normalize("pêssego"))
        assertEquals("oleo", normalize("óleo"))
        assertEquals("abobora", normalize("abóbora"))
        assertEquals("pao de lo", normalize("pão de ló"))
        assertEquals("a vista", normalize("à vista"))
        assertEquals("aipim", normalize("aípim"))
        assertEquals("linguica", normalize("lingüiça"))
    }

    @Test
    fun keepsTextWithoutAccents() {
        assertEquals("arroz", normalize("arroz"))
        assertEquals("leite 1l", normalize("leite 1l"))
    }

    @Test
    fun trimsAndCollapsesSpaces() {
        assertEquals("pao de sal", normalize("  pão   de\tsal \n"))
        assertEquals("", normalize("   "))
    }

    @Test
    fun removesSeparateCombiningAccents() {
        // "a" + til combinante e "c" + cedilha combinante, como alguns teclados mandam.
        assertEquals("maca", normalize("Maçã"))
    }

    @Test
    fun keepsHyphensAndApostrophes() {
        assertEquals("farinha d'agua", normalize("Farinha d'Água"))
        assertEquals("erva-mate", normalize("Erva-Mate"))
    }
}
