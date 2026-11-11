package br.com.mentealerta.listadecompras.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Paleta do estilo quadrinhos (plano §4.8).
 *
 * O verde-folha é a única cor de destaque. Estado nunca é indicado só por cor:
 * o riscado também usa traço e marca com ✓.
 */
object ComicColors {
    /** Papel creme das superfícies (cartões, lista). Sem branco puro. */
    val Paper = Color(0xFFFBF3DC)

    /** Fundo da tela, um tom abaixo do papel, onde vai a retícula. */
    val Background = Color(0xFFF4E7C2)

    /** Tinta quase preta: texto, contornos e sombras. */
    val Ink = Color(0xFF141414)

    /** Texto de produto riscado. */
    val CrossedOut = Color(0xFF66625A)

    /** Linhas divisórias discretas entre produtos. */
    val Divider = Color(0xFFE2D5B3)

    /** Verde-folha: topo, botões principais e marca de riscado. */
    val LeafGreen = Color(0xFF2E6B3A)

    /** Texto sobre o verde-folha. */
    val OnLeafGreen = Color(0xFFFFFFFF)
}

/**
 * Paleta escura PROVISÓRIA, só para o app não quebrar no modo escuro.
 * TODO(#9): substituir pela paleta decidida e registrada no plano §4.8.
 */
object ComicColorsDark {
    val Paper = Color(0xFF26241F)
    val Background = Color(0xFF1C1A16)
    val Ink = Color(0xFFFBF3DC)
    val CrossedOut = Color(0xFFADA796)
    val Divider = Color(0xFF3D3930)
    val LeafGreen = Color(0xFF8CC79A)
    val OnLeafGreen = Color(0xFF141414)
}

internal val LightColorScheme = lightColorScheme(
    primary = ComicColors.LeafGreen,
    onPrimary = ComicColors.OnLeafGreen,
    primaryContainer = ComicColors.LeafGreen,
    onPrimaryContainer = ComicColors.OnLeafGreen,
    secondary = ComicColors.Ink,
    onSecondary = ComicColors.Paper,
    background = ComicColors.Background,
    onBackground = ComicColors.Ink,
    surface = ComicColors.Paper,
    onSurface = ComicColors.Ink,
    surfaceVariant = ComicColors.Background,
    onSurfaceVariant = ComicColors.CrossedOut,
    surfaceContainer = ComicColors.Paper,
    inverseSurface = ComicColors.Ink,
    inverseOnSurface = ComicColors.Paper,
    outline = ComicColors.Ink,
    outlineVariant = ComicColors.Divider,
    scrim = ComicColors.Ink,
)

internal val DarkColorScheme = darkColorScheme(
    primary = ComicColorsDark.LeafGreen,
    onPrimary = ComicColorsDark.OnLeafGreen,
    primaryContainer = ComicColorsDark.LeafGreen,
    onPrimaryContainer = ComicColorsDark.OnLeafGreen,
    secondary = ComicColorsDark.Ink,
    onSecondary = ComicColorsDark.Paper,
    background = ComicColorsDark.Background,
    onBackground = ComicColorsDark.Ink,
    surface = ComicColorsDark.Paper,
    onSurface = ComicColorsDark.Ink,
    surfaceVariant = ComicColorsDark.Background,
    onSurfaceVariant = ComicColorsDark.CrossedOut,
    surfaceContainer = ComicColorsDark.Paper,
    inverseSurface = ComicColorsDark.Ink,
    inverseOnSurface = ComicColorsDark.Paper,
    outline = ComicColorsDark.Ink,
    outlineVariant = ComicColorsDark.Divider,
    scrim = ComicColorsDark.Background,
)
