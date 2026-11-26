package br.com.mentealerta.listadecompras.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/** Raio do pontinho em proporção ao espaço entre eles (1,4px em 12px no protótipo). */
private const val DOT_RADIUS_RATIO = 0.12f

/**
 * Fundo com retícula de pontinhos do estilo quadrinhos (plano §4.8), desenhada por trás do
 * conteúdo. Decorativa: não entra no TalkBack. [color] já vem com a opacidade discreta
 * (`ComicTheme.colors.halftoneDot`).
 */
fun Modifier.halftone(color: Color, spacing: Dp): Modifier = drawBehind {
    val step = spacing.toPx()
    val radius = step * DOT_RADIUS_RATIO
    var y = step / 2
    while (y < size.height) {
        var x = step / 2
        while (x < size.width) {
            drawCircle(color, radius, Offset(x, y))
            x += step
        }
        y += step
    }
}
