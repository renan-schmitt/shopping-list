package br.com.mentealerta.listadecompras.ui.componentes

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import br.com.mentealerta.listadecompras.ui.tema.EsquemaClaro
import br.com.mentealerta.listadecompras.ui.tema.EsquemaEscuro
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Contraste das cores usadas pelos componentes base (plano §1.7: alto
 * contraste). Mínimo de 4,5:1 (WCAG AA para texto) em todos os pares, para
 * valer também nos textos menores.
 */
class ContrasteTest {

    @Test
    fun paletaClara() = conferir(EsquemaClaro)

    @Test
    fun paletaEscura() = conferir(EsquemaEscuro)

    private fun conferir(cores: ColorScheme) {
        val pares = mapOf(
            "texto do botão principal" to (cores.onPrimary to cores.primary),
            "texto do botão secundário" to (cores.primary to cores.surface),
            "título de seção" to (cores.inverseOnSurface to cores.inverseSurface),
            "texto do aviso e do cartão" to (cores.onSurface to cores.surface),
        )
        pares.forEach { (nome, par) ->
            val razao = contraste(par.first, par.second)
            assertTrue("Contraste de $nome é ${"%.2f".format(razao)}:1", razao >= MINIMO)
        }
    }

    private fun contraste(a: Color, b: Color): Float {
        val (claro, escuro) = listOf(a.luminance(), b.luminance()).sortedDescending()
        return (claro + 0.05f) / (escuro + 0.05f)
    }

    private companion object {
        const val MINIMO = 4.5f
    }
}
