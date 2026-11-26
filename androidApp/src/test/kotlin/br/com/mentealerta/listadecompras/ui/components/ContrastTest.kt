package br.com.mentealerta.listadecompras.ui.components

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import br.com.mentealerta.listadecompras.ui.theme.DarkColorScheme
import br.com.mentealerta.listadecompras.ui.theme.LightColorScheme
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Contraste das cores usadas pelos componentes base (plano §1.7: alto
 * contraste). Mínimo de 4,5:1 (WCAG AA para texto) em todos os pares, para
 * valer também nos textos menores. No escuro (plano §4.8, #9) a meta é mais alta: todo texto
 * a partir de 6:1 e contornos a partir de 13:1.
 */
class ContrastTest {

    @Test
    fun lightPalette() = check(LightColorScheme)

    @Test
    fun darkPalette() = check(DarkColorScheme, minimum = DARK_TEXT_MINIMUM)

    @Test
    fun darkOutlines() {
        val colors = DarkColorScheme
        assertAll(
            minimum = DARK_OUTLINE_MINIMUM,
            pairs = mapOf(
                "contorno sobre o papel" to (colors.outline to colors.surface),
                "contorno sobre o fundo" to (colors.outline to colors.background),
            ),
        )
    }

    private fun check(colors: ColorScheme, minimum: Float = MINIMUM) = assertAll(
        minimum = minimum,
        pairs = mapOf(
            "texto do botão principal" to (colors.onPrimary to colors.primary),
            "texto do botão secundário" to (colors.primary to colors.surface),
            "título de seção" to (colors.inverseOnSurface to colors.inverseSurface),
            "texto do aviso e do cartão" to (colors.onSurface to colors.surface),
            "texto riscado" to (colors.onSurfaceVariant to colors.surface),
        ),
    )

    private fun assertAll(minimum: Float, pairs: Map<String, Pair<Color, Color>>) {
        pairs.forEach { (name, pair) ->
            val ratio = contrast(pair.first, pair.second)
            assertTrue("Contraste de $name é ${"%.2f".format(ratio)}:1", ratio >= minimum)
        }
    }

    private fun contrast(a: Color, b: Color): Float {
        val (lighter, darker) = listOf(a.luminance(), b.luminance()).sortedDescending()
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    private companion object {
        const val MINIMUM = 4.5f
        const val DARK_TEXT_MINIMUM = 6f
        const val DARK_OUTLINE_MINIMUM = 13f
    }
}
