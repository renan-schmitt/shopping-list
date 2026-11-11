package br.com.mentealerta.listadecompras.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.util.Locale

private val BrazilianPortuguese: Locale = Locale.forLanguageTag("pt-BR")

/**
 * Visual do estilo quadrinhos (plano §4.8): contorno grosso e sombra sólida
 * deslocada para baixo e para a direita, como o `box-shadow: 5px 5px 0` do
 * protótipo.
 *
 * O espaço da sombra é reservado no layout (à direita e embaixo), para ela não
 * ser cortada nem invadir o vizinho. Com [pressOffset] > 0, a face desce e a
 * sombra visível diminui, como o botão pressionado do protótipo.
 */
fun Modifier.comicStyle(
    shape: Shape,
    background: Color,
    outline: Color,
    outlineWidth: Dp,
    shadow: Dp,
    pressOffset: Dp = 0.dp,
): Modifier = this
    .padding(end = shadow, bottom = shadow)
    .drawBehind {
        val offset = shadow.toPx()
        translate(offset, offset) {
            drawOutline(shape.createOutline(size, layoutDirection, this), outline)
        }
    }
    .offset(x = pressOffset, y = pressOffset)
    .border(outlineWidth, outline, shape)
    .background(background, shape)
    .clip(shape)

/**
 * Texto curto em caixa alta (plano §4.8; guia, princípio 9).
 *
 * As maiúsculas são só visuais: o TalkBack recebe [text] como foi escrito,
 * para não soletrar palavras como se fossem siglas.
 *
 * Com [letterOutline], a letra ganha contorno e sombra na cor dada (logo e
 * botão principal do protótipo).
 */
@Composable
fun UppercaseText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center,
    letterOutline: Color? = null,
) {
    val visible = text.uppercase(BrazilianPortuguese)
    val fontSizePx = with(LocalDensity.current) { style.fontSize.toPx() }
    Box(
        modifier = modifier.clearAndSetSemantics { this.text = AnnotatedString(text) },
        contentAlignment = Alignment.Center,
    ) {
        if (letterOutline != null) {
            Text(
                text = visible,
                style = style.copy(
                    drawStyle = Stroke(width = fontSizePx * LetterOutlineRatio),
                    shadow = Shadow(
                        color = letterOutline,
                        offset = Offset(fontSizePx * LetterShadowRatio, fontSizePx * LetterShadowRatio),
                    ),
                ),
                color = letterOutline,
                textAlign = textAlign,
            )
        }
        Text(text = visible, style = style, color = color, textAlign = textAlign)
    }
}

/** Contorno da letra em proporção ao tamanho da fonte (1,5px em 38px no protótipo). */
private const val LetterOutlineRatio = 0.08f

/** Sombra da letra em proporção ao tamanho da fonte (2px em 38px no protótipo). */
private const val LetterShadowRatio = 0.055f

/** Letra clara pede contorno escuro; letra escura dispensa contorno. */
internal fun letterOutlineFor(letter: Color, outline: Color): Color? =
    if (letter.luminance() > 0.5f) outline else null
