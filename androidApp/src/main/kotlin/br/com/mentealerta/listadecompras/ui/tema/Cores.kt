package br.com.mentealerta.listadecompras.ui.tema

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Paleta do estilo quadrinhos (plano §4.8).
 *
 * O verde-folha é a única cor de destaque. Estado nunca é indicado só por cor:
 * o riscado também usa traço e marca com ✓.
 */
object CoresQuadrinhos {
    /** Papel creme das superfícies (cartões, lista). Sem branco puro. */
    val Papel = Color(0xFFFBF3DC)

    /** Fundo da tela, um tom abaixo do papel, onde vai a retícula. */
    val Fundo = Color(0xFFF4E7C2)

    /** Tinta quase preta: texto, contornos e sombras. */
    val Tinta = Color(0xFF141414)

    /** Texto de produto riscado. */
    val Riscado = Color(0xFF66625A)

    /** Linhas divisórias discretas entre produtos. */
    val Linha = Color(0xFFE2D5B3)

    /** Verde-folha: topo, botões principais e marca de riscado. */
    val VerdeFolha = Color(0xFF2E6B3A)

    /** Texto sobre o verde-folha. */
    val SobreVerde = Color(0xFFFFFFFF)
}

/**
 * Paleta escura PROVISÓRIA, só para o app não quebrar no modo escuro.
 * TODO(#9): substituir pela paleta decidida e registrada no plano §4.8.
 */
object CoresQuadrinhosEscuro {
    val Papel = Color(0xFF26241F)
    val Fundo = Color(0xFF1C1A16)
    val Tinta = Color(0xFFFBF3DC)
    val Riscado = Color(0xFFADA796)
    val Linha = Color(0xFF3D3930)
    val VerdeFolha = Color(0xFF8CC79A)
    val SobreVerde = Color(0xFF141414)
}

internal val EsquemaClaro = lightColorScheme(
    primary = CoresQuadrinhos.VerdeFolha,
    onPrimary = CoresQuadrinhos.SobreVerde,
    primaryContainer = CoresQuadrinhos.VerdeFolha,
    onPrimaryContainer = CoresQuadrinhos.SobreVerde,
    secondary = CoresQuadrinhos.Tinta,
    onSecondary = CoresQuadrinhos.Papel,
    background = CoresQuadrinhos.Fundo,
    onBackground = CoresQuadrinhos.Tinta,
    surface = CoresQuadrinhos.Papel,
    onSurface = CoresQuadrinhos.Tinta,
    surfaceVariant = CoresQuadrinhos.Fundo,
    onSurfaceVariant = CoresQuadrinhos.Riscado,
    surfaceContainer = CoresQuadrinhos.Papel,
    inverseSurface = CoresQuadrinhos.Tinta,
    inverseOnSurface = CoresQuadrinhos.Papel,
    outline = CoresQuadrinhos.Tinta,
    outlineVariant = CoresQuadrinhos.Linha,
    scrim = CoresQuadrinhos.Tinta,
)

internal val EsquemaEscuro = darkColorScheme(
    primary = CoresQuadrinhosEscuro.VerdeFolha,
    onPrimary = CoresQuadrinhosEscuro.SobreVerde,
    primaryContainer = CoresQuadrinhosEscuro.VerdeFolha,
    onPrimaryContainer = CoresQuadrinhosEscuro.SobreVerde,
    secondary = CoresQuadrinhosEscuro.Tinta,
    onSecondary = CoresQuadrinhosEscuro.Papel,
    background = CoresQuadrinhosEscuro.Fundo,
    onBackground = CoresQuadrinhosEscuro.Tinta,
    surface = CoresQuadrinhosEscuro.Papel,
    onSurface = CoresQuadrinhosEscuro.Tinta,
    surfaceVariant = CoresQuadrinhosEscuro.Fundo,
    onSurfaceVariant = CoresQuadrinhosEscuro.Riscado,
    surfaceContainer = CoresQuadrinhosEscuro.Papel,
    inverseSurface = CoresQuadrinhosEscuro.Tinta,
    inverseOnSurface = CoresQuadrinhosEscuro.Papel,
    outline = CoresQuadrinhosEscuro.Tinta,
    outlineVariant = CoresQuadrinhosEscuro.Linha,
    scrim = CoresQuadrinhosEscuro.Fundo,
)
