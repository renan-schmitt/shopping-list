package br.com.mentealerta.listadecompras.ui.componentes

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

private val PortuguesDoBrasil: Locale = Locale.forLanguageTag("pt-BR")

/**
 * Visual do estilo quadrinhos (plano §4.8): contorno grosso e sombra sólida
 * deslocada para baixo e para a direita, como o `box-shadow: 5px 5px 0` do
 * protótipo.
 *
 * O espaço da sombra é reservado no layout (à direita e embaixo), para ela não
 * ser cortada nem invadir o vizinho. Com [afundar] > 0, a face desce e a
 * sombra visível diminui, como o botão pressionado do protótipo.
 */
fun Modifier.estiloQuadrinhos(
    forma: Shape,
    fundo: Color,
    contorno: Color,
    larguraDoContorno: Dp,
    sombra: Dp,
    afundar: Dp = 0.dp,
): Modifier = this
    .padding(end = sombra, bottom = sombra)
    .drawBehind {
        val deslocamento = sombra.toPx()
        translate(deslocamento, deslocamento) {
            drawOutline(forma.createOutline(size, layoutDirection, this), contorno)
        }
    }
    .offset(x = afundar, y = afundar)
    .border(larguraDoContorno, contorno, forma)
    .background(fundo, forma)
    .clip(forma)

/**
 * Texto curto em caixa alta (plano §4.8; guia, princípio 9).
 *
 * As maiúsculas são só visuais: o TalkBack recebe [texto] como foi escrito,
 * para não soletrar palavras como se fossem siglas.
 *
 * Com [contornoDaLetra], a letra ganha contorno e sombra na cor dada (logo e
 * botão principal do protótipo).
 */
@Composable
fun TextoCaixaAlta(
    texto: String,
    estilo: TextStyle,
    cor: Color,
    modifier: Modifier = Modifier,
    alinhamento: TextAlign = TextAlign.Center,
    contornoDaLetra: Color? = null,
) {
    val visivel = texto.uppercase(PortuguesDoBrasil)
    val tamanhoDaLetra = with(LocalDensity.current) { estilo.fontSize.toPx() }
    Box(
        modifier = modifier.clearAndSetSemantics { text = AnnotatedString(texto) },
        contentAlignment = Alignment.Center,
    ) {
        if (contornoDaLetra != null) {
            Text(
                text = visivel,
                style = estilo.copy(
                    drawStyle = Stroke(width = tamanhoDaLetra * ContornoDaLetra),
                    shadow = Shadow(
                        color = contornoDaLetra,
                        offset = Offset(tamanhoDaLetra * SombraDaLetra, tamanhoDaLetra * SombraDaLetra),
                    ),
                ),
                color = contornoDaLetra,
                textAlign = alinhamento,
            )
        }
        Text(text = visivel, style = estilo, color = cor, textAlign = alinhamento)
    }
}

/** Contorno da letra em proporção ao tamanho da fonte (1,5px em 38px no protótipo). */
private const val ContornoDaLetra = 0.08f

/** Sombra da letra em proporção ao tamanho da fonte (2px em 38px no protótipo). */
private const val SombraDaLetra = 0.055f

/** Letra clara pede contorno escuro; letra escura dispensa contorno. */
internal fun contornoParaLetra(letra: Color, contorno: Color): Color? =
    if (letra.luminance() > 0.5f) contorno else null
