package br.com.mentealerta.listadecompras.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import br.com.mentealerta.listadecompras.R

/** Bangers: só logo, títulos de seção e botões principais (plano §4.8). */
val Bangers = FontFamily(Font(R.font.bangers_regular, FontWeight.Normal))

/** Lexend Deca negrito: lista e demais textos (plano §4.8). */
val LexendDeca = FontFamily(Font(R.font.lexend_deca_bold, FontWeight.Bold))

private val LexendLetterSpacing = 0.04.em
private val BangersLetterSpacing = 0.05.em

private fun bangers(size: Int) = TextStyle(
    fontFamily = Bangers,
    fontWeight = FontWeight.Normal,
    fontSize = size.sp,
    lineHeight = (size * 1.1).sp,
    letterSpacing = BangersLetterSpacing,
)

private fun lexend(size: Int, lineHeightFactor: Double = 1.2) = TextStyle(
    fontFamily = LexendDeca,
    fontWeight = FontWeight.Bold,
    fontSize = size.sp,
    lineHeight = (size * lineHeightFactor).sp,
    letterSpacing = LexendLetterSpacing,
)

/**
 * Tamanhos em `sp`, para acompanhar a escala de fonte do sistema (até 200%).
 *
 * Uso de cada estilo:
 * - `displayMedium`: logo "Lista de Compras"
 * - `headlineMedium`: texto do botão principal ("Terminei")
 * - `titleLarge`: título de seção
 * - `bodyLarge`: nome do produto (~28sp)
 * - `bodyMedium`: quantidade e textos de apoio maiores
 * - `labelLarge`: botões secundários
 * - `bodySmall`: textos pequenos ("Mente Alerta" acima do logo)
 *
 * Caixa alta não fica aqui: é aplicada pelos componentes, mantendo o texto
 * original para o TalkBack (CLAUDE.md).
 */
internal val ComicTypography = Typography(
    displayLarge = bangers(52),
    displayMedium = bangers(46),
    displaySmall = bangers(42),
    headlineLarge = bangers(40),
    headlineMedium = bangers(38),
    headlineSmall = bangers(34),
    titleLarge = bangers(30),
    titleMedium = lexend(24),
    titleSmall = lexend(22),
    bodyLarge = lexend(28, lineHeightFactor = 1.15),
    bodyMedium = lexend(24),
    bodySmall = lexend(18),
    labelLarge = lexend(22),
    labelMedium = lexend(20),
    labelSmall = lexend(18),
)
