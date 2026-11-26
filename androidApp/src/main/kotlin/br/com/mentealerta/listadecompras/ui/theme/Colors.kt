package br.com.mentealerta.listadecompras.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
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
 * Paleta escura "papel invertido" (plano §4.8, decidida na #9): papel carvão sobre fundo quase preto
 * quente, tinta creme no texto e nos contornos. O verde-folha clareado segue como única cor de
 * destaque.
 */
object ComicColorsDark {
    /** Papel carvão das superfícies. */
    val Paper = Color(0xFF26231E)

    /** Fundo quase preto quente, onde vai a retícula. */
    val Background = Color(0xFF14120F)

    /** Tinta creme: texto e contornos. */
    val Ink = Color(0xFFF4EBD3)

    /** Sombra sólida em preto, mais escura que o fundo: dá profundidade sem parecer brilho. */
    val Shadow = Color(0xFF000000)

    /** Texto de produto riscado. */
    val CrossedOut = Color(0xFFA9A291)

    /** Linhas divisórias discretas entre produtos. */
    val Divider = Color(0xFF433E35)

    /** Verde-folha clareado: topo, botões principais e marca de riscado. */
    val LeafGreen = Color(0xFF7CC08A)

    /** Texto sobre o verde-folha clareado. */
    val OnLeafGreen = Color(0xFF141414)
}

/**
 * Cores do estilo quadrinhos que o Material 3 não tem: a sombra sólida deslocada e os pontinhos da
 * retícula. Acesso por `ComicTheme.colors`.
 */
@Immutable
data class ComicExtraColors(
    /** Sombra sólida deslocada de botões, cartões e marcas. */
    val shadow: Color,
    /** Pontinhos da retícula do fundo, já com a opacidade discreta. */
    val halftoneDot: Color,
)

internal val LightComicColors = ComicExtraColors(
    shadow = ComicColors.Ink,
    halftoneDot = ComicColors.Ink.copy(alpha = 0.13f),
)

internal val DarkComicColors = ComicExtraColors(
    shadow = ComicColorsDark.Shadow,
    halftoneDot = Color.White.copy(alpha = 0.07f),
)

val LocalComicColors = staticCompositionLocalOf { LightComicColors }

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
    scrim = ComicColorsDark.Shadow,
)
