package br.com.mentealerta.listadecompras.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.mentealerta.listadecompras.ui.theme.ComicTheme

/** Tamanho dos ícones dentro dos botões (protótipo: 30px). */
private val IconSize = 30.dp

/**
 * Botão principal: verde-folha, letra Bangers em caixa alta, ~88dp de altura
 * (plano §4.8). Ex.: "Terminei" com o texto de apoio "Faltam 5".
 *
 * O ícone, se houver, fica acima do texto, como no protótipo; é decorativo
 * para o TalkBack, que lê só o texto.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    supportingText: String? = null,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    ComicButton(
        onClick = onClick,
        background = colors.primary,
        shadow = ComicTheme.tokens.mediumShadow,
        minHeight = ComicTheme.tokens.primaryButtonHeight,
        modifier = modifier,
    ) {
        if (icon != null) ButtonIcon(icon, colors.onPrimary)
        UppercaseText(
            text = text,
            style = typography.headlineMedium,
            color = colors.onPrimary,
            letterOutline = letterOutlineFor(colors.onPrimary, colors.outline),
        )
        if (supportingText != null) {
            UppercaseText(text = supportingText, style = typography.labelMedium, color = colors.onPrimary)
        }
    }
}

/**
 * Botão secundário: papel com contorno e letra verde em Lexend Deca. Ex.:
 * "Adicionar" ao lado do "Terminei", ou "Ops, voltar" no aviso.
 *
 * Altura mínima de 64dp (área de toque, plano §1.7); [minHeight] permite
 * igualar ao botão principal quando os dois ficam lado a lado.
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    minHeight: Dp = ComicTheme.tokens.minTouchTarget,
) {
    val colors = MaterialTheme.colorScheme
    ComicButton(
        onClick = onClick,
        background = colors.surface,
        shadow = ComicTheme.tokens.mediumShadow,
        minHeight = minHeight,
        modifier = modifier,
    ) {
        if (icon != null) ButtonIcon(icon, colors.primary)
        UppercaseText(text = text, style = MaterialTheme.typography.labelLarge, color = colors.primary)
    }
}

/**
 * Base dos botões: contorno, sombra sólida e o "afundar" ao tocar no lugar da
 * onda do Material. A área de toque inclui a sombra e tem no mínimo 64dp nos
 * dois sentidos; a altura cresce se o texto quebrar a linha.
 */
@Composable
private fun ComicButton(
    onClick: () -> Unit,
    background: Color,
    shadow: Dp,
    minHeight: Dp,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val tokens = ComicTheme.tokens
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressOffset by animateDpAsState(if (pressed) tokens.pressedOffset else 0.dp, label = "pressOffset")

    Column(
        modifier = modifier
            .clickable(interactionSource = interactionSource, indication = null, role = Role.Button, onClick = onClick)
            .comicStyle(
                shape = MaterialTheme.shapes.medium,
                background = background,
                outline = MaterialTheme.colorScheme.outline,
                outlineWidth = tokens.thickOutline,
                shadow = shadow,
                pressOffset = pressOffset,
            )
            .heightIn(min = minHeight)
            .widthIn(min = tokens.minTouchTarget)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content,
    )
}

@Composable
private fun ButtonIcon(icon: Painter, tint: Color) {
    Icon(painter = icon, contentDescription = null, tint = tint, modifier = Modifier.size(IconSize))
}
