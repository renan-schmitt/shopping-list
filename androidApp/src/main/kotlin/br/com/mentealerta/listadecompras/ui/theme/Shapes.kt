package br.com.mentealerta.listadecompras.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal val ComicShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/**
 * Medidas do estilo quadrinhos e de acessibilidade (plano §1.7 e §4.8).
 * Usadas pelos componentes base (#12).
 */
@Immutable
data class ComicTokens(
    /** Contorno de elementos pequenos (rótulos, marca de riscar). */
    val thinOutline: Dp = 3.dp,
    /** Contorno de botões, cartões e faixas. */
    val thickOutline: Dp = 4.dp,
    /** Sombra sólida deslocada de elementos pequenos. */
    val smallShadow: Dp = 3.dp,
    /** Sombra sólida deslocada de botões. */
    val mediumShadow: Dp = 5.dp,
    /** Sombra sólida deslocada da folha da lista. */
    val largeShadow: Dp = 8.dp,
    /** Deslocamento do botão ao ser pressionado. */
    val pressedOffset: Dp = 3.dp,
    /** Área de toque mínima. */
    val minTouchTarget: Dp = 64.dp,
    /** Altura mínima da linha de produto; cresce se o nome quebrar a linha. */
    val minRowHeight: Dp = 72.dp,
    /** Altura dos botões principais. */
    val primaryButtonHeight: Dp = 88.dp,
    /** Espaço entre os pontinhos da retícula do fundo. */
    val halftoneSpacing: Dp = 12.dp,
)

val LocalComicTokens = staticCompositionLocalOf { ComicTokens() }
