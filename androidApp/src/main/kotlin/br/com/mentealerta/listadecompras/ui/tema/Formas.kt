package br.com.mentealerta.listadecompras.ui.tema

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal val FormasQuadrinhos = Shapes(
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
data class TokensQuadrinhos(
    /** Contorno de elementos pequenos (rótulos, marca de riscar). */
    val contornoFino: Dp = 3.dp,
    /** Contorno de botões, cartões e faixas. */
    val contornoGrosso: Dp = 4.dp,
    /** Sombra sólida deslocada de elementos pequenos. */
    val sombraPequena: Dp = 3.dp,
    /** Sombra sólida deslocada de botões. */
    val sombraMedia: Dp = 5.dp,
    /** Sombra sólida deslocada da folha da lista. */
    val sombraGrande: Dp = 8.dp,
    /** Deslocamento do botão ao ser pressionado. */
    val deslocamentoAoTocar: Dp = 3.dp,
    /** Área de toque mínima. */
    val areaDeToqueMinima: Dp = 64.dp,
    /** Altura mínima da linha de produto; cresce se o nome quebrar a linha. */
    val alturaMinimaDaLinha: Dp = 72.dp,
    /** Altura dos botões principais. */
    val alturaBotaoPrincipal: Dp = 88.dp,
    /** Espaço entre os pontinhos da retícula do fundo. */
    val espacoReticula: Dp = 12.dp,
)

val LocalTokensQuadrinhos = staticCompositionLocalOf { TokensQuadrinhos() }
