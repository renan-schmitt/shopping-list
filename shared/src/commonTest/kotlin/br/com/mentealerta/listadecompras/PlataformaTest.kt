package br.com.mentealerta.listadecompras

import kotlin.test.Test
import kotlin.test.assertTrue

class PlataformaTest {
    @Test
    fun nomeDaPlataformaNaoEhVazio() {
        assertTrue(nomeDaPlataforma().isNotBlank())
    }
}
