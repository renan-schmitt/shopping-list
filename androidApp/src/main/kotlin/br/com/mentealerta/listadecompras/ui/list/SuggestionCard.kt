package br.com.mentealerta.listadecompras.ui.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.ui.components.PrimaryButton
import br.com.mentealerta.listadecompras.ui.components.SecondaryButton
import br.com.mentealerta.listadecompras.ui.components.comicStyle
import br.com.mentealerta.listadecompras.ui.theme.ComicTheme

private val SuggestionIconSize = 48.dp

/**
 * "Está faltando?" no topo da folha (#64, plano §3.4): o ícone do produto ao lado de "Faz 12 dias que
 * você comprou café. Está faltando?" e, embaixo, um sobre o outro para caber com letra grande,
 * **Sim, pôr na lista** ([onAccept]) e **Não** ([onDismiss]). O fundo mais escuro que a folha separa
 * o cartão dos produtos da lista.
 */
@Composable
fun SuggestionCard(
    suggestion: SuggestionUi,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val tokens = ComicTheme.tokens
    Column(
        modifier = modifier
            .comicStyle(
                shape = MaterialTheme.shapes.medium,
                background = colors.background,
                outline = colors.outline,
                outlineWidth = tokens.thinOutline,
                shadow = tokens.smallShadow,
                shadowColor = ComicTheme.colors.shadow,
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Decorativo: o nome do produto está na frase ao lado.
            Image(
                painter = painterResource(suggestion.icon),
                contentDescription = null,
                modifier = Modifier.size(SuggestionIconSize),
            )
            Text(
                text = pluralStringResource(
                    R.plurals.suggestion_question,
                    suggestion.days,
                    suggestion.days,
                    suggestion.name.inSentence(),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface,
                modifier = Modifier.weight(1f),
            )
        }
        PrimaryButton(
            text = stringResource(R.string.suggestion_accept),
            onClick = onAccept,
            modifier = Modifier.fillMaxWidth(),
        )
        SecondaryButton(
            text = stringResource(R.string.suggestion_dismiss),
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Nome no meio da frase: "comprou café", mesmo gravado como "Café". Sigla em maiúsculas ("OMO") fica
 * como está.
 */
internal fun String.inSentence(): String =
    if (getOrNull(1)?.isUpperCase() == true) this else replaceFirstChar { it.lowercase() }
