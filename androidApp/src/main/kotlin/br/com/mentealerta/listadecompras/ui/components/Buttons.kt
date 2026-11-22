package br.com.mentealerta.listadecompras.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.mentealerta.listadecompras.ui.theme.ComicTheme

/** Tamanho dos ícones dentro dos botões (protótipo: 30px). */
private val IconSize = 30.dp

/** Abaixo disso, apertar e soltar conta como um toque rápido, não como segurar. */
private const val SHORT_PRESS_MILLIS = 500L

/** Botão desativado fica apagado, sem mudar de cor. */
private const val DISABLED_ALPHA = 0.4f

/**
 * Botão principal: verde-folha, letra Bangers em caixa alta, ~88dp de altura
 * (plano §4.8). Ex.: "Terminei" com o texto de apoio "Faltam 5".
 *
 * O ícone, se houver, fica acima do texto, como no protótipo; é decorativo
 * para o TalkBack, que lê só o texto. Desativado ([enabled] falso), fica
 * apagado e o TalkBack anuncia "desativado" (ex.: "Menos" na quantidade mínima).
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    supportingText: String? = null,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    ComicButton(
        onClick = onClick,
        enabled = enabled,
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
 * igualar ao botão principal quando os dois ficam lado a lado. Desativado
 * ([enabled] falso), fica apagado e o TalkBack anuncia "desativado". Com
 * [tintIcon] falso, o ícone mantém as cores dele (ex.: o cesto de lixo do
 * "Jogar a lista fora", que é colorido).
 *
 * Com [prominent], usa a letra Bangers e a altura do botão principal, mas
 * continua no papel: chama atenção sem parecer a ação principal da tela (ex.:
 * "Tirar da lista" na edição, #124).
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    tintIcon: Boolean = true,
    prominent: Boolean = false,
    minHeight: Dp = if (prominent) ComicTheme.tokens.primaryButtonHeight else ComicTheme.tokens.minTouchTarget,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    ComicButton(
        onClick = onClick,
        enabled = enabled,
        background = colors.surface,
        shadow = ComicTheme.tokens.mediumShadow,
        minHeight = minHeight,
        modifier = modifier,
    ) {
        if (icon != null) ButtonIcon(icon, if (tintIcon) colors.primary else Color.Unspecified)
        UppercaseText(
            text = text,
            style = if (prominent) typography.headlineMedium else typography.labelLarge,
            color = colors.primary,
        )
    }
}

/**
 * Botão de segurar (#123), com a cara do secundário: apertar chama [onPress] e soltar chama
 * [onRelease], com `short` quando foi só um toque rápido. O dedo pode escorregar para fora do botão
 * sem soltar. Para o TalkBack é um botão comum: tocar chama [onToggle] (abre e, de novo, fecha).
 * Com [held], fica afundado mesmo sem o dedo (ex.: microfone aberto pelo TalkBack).
 */
@Composable
fun HoldButton(
    text: String,
    onPress: () -> Unit,
    onRelease: (short: Boolean) -> Unit,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    held: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val currentOnPress by rememberUpdatedState(onPress)
    val currentOnRelease by rememberUpdatedState(onRelease)
    val currentOnToggle by rememberUpdatedState(onToggle)
    var down by remember { mutableStateOf(false) }
    val gesture = Modifier
        .pointerInput(Unit) {
            awaitEachGesture {
                val first = awaitFirstDown()
                first.consume()
                down = true
                currentOnPress()
                var releasedAt = first.uptimeMillis
                do {
                    val event = awaitPointerEvent()
                    event.changes.forEach {
                        it.consume()
                        releasedAt = it.uptimeMillis
                    }
                } while (event.changes.any { it.pressed })
                down = false
                currentOnRelease(releasedAt - first.uptimeMillis < SHORT_PRESS_MILLIS)
            }
        }
        .semantics(mergeDescendants = true) {
            role = Role.Button
            onClick {
                currentOnToggle()
                true
            }
        }
    ComicButton(
        onClick = null,
        background = colors.surface,
        shadow = ComicTheme.tokens.mediumShadow,
        minHeight = ComicTheme.tokens.minTouchTarget,
        modifier = modifier.then(gesture),
        held = down || held,
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
    onClick: (() -> Unit)?,
    background: Color,
    shadow: Dp,
    minHeight: Dp,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    held: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val tokens = ComicTheme.tokens
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressOffset by animateDpAsState(if (pressed || held) tokens.pressedOffset else 0.dp, label = "pressOffset")
    val click = if (onClick == null) {
        Modifier
    } else {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            role = Role.Button,
            onClick = onClick,
        )
    }

    Column(
        modifier = modifier
            .then(click)
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
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
