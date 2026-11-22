package br.com.mentealerta.listadecompras.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import br.com.mentealerta.listadecompras.ui.theme.ComicTheme

private val FieldMinHeight = 68.dp

/**
 * Campo grande com o rótulo em cima, em caixa alta (protótipo navegável); rótulo que é frase fica
 * em caixa normal ([uppercaseLabel] falso). O TalkBack ouve o rótulo pelo próprio campo, em caixa
 * normal. [placeholder] aparece com o campo vazio, só na tela (ex.:
 * "Ex.: desnatado"). A tecla do teclado ([imeAction]) chama [onImeAction]; [focusRequester] permite
 * abrir a tela com o teclado.
 */
@Composable
fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    labelColor: Color = MaterialTheme.colorScheme.onSurface,
    uppercaseLabel: Boolean = true,
    placeholder: String? = null,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
    focusRequester: FocusRequester? = null,
) {
    val colors = MaterialTheme.colorScheme
    val tokens = ComicTheme.tokens
    val textStyle = MaterialTheme.typography.bodyLarge
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = if (uppercaseLabel) label.toUpperCase(LocaleList.current) else label,
            style = MaterialTheme.typography.labelLarge,
            color = labelColor,
            modifier = Modifier.clearAndSetSemantics {},
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = textStyle.copy(color = colors.onSurface),
            cursorBrush = SolidColor(colors.onSurface),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                autoCorrectEnabled = false,
                imeAction = imeAction,
            ),
            keyboardActions = KeyboardActions(onAny = { onImeAction() }),
            modifier = Modifier
                .fillMaxWidth()
                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                .semantics { contentDescription = label }
                .border(tokens.thickOutline, colors.outline, MaterialTheme.shapes.medium)
                .background(colors.surface, MaterialTheme.shapes.medium)
                .heightIn(min = FieldMinHeight)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty() && placeholder != null) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.clearAndSetSemantics {},
                        )
                    }
                    inner()
                }
            },
        )
    }
}
