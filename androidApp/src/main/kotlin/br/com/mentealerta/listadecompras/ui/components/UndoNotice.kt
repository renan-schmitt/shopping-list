package br.com.mentealerta.listadecompras.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AccessibilityManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.mentealerta.listadecompras.ui.theme.ComicTheme
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Aviso curto no pé da tela, com o botão "Ops, voltar" (guia de linguagem).
 * Ex.: "Feijão saiu da lista." · **Ops, voltar**.
 *
 * Uso: `SnackbarHost(state) { UndoNotice(it) }`, mostrando o aviso com
 * [showUndoNotice].
 */
@Composable
fun UndoNotice(data: SnackbarData, modifier: Modifier = Modifier) {
    UndoNotice(
        message = data.visuals.message,
        actionLabel = data.visuals.actionLabel,
        onAction = data::performAction,
        modifier = modifier,
    )
}

/**
 * A mensagem fica em caixa normal (pode ter mais de uma linha, guia,
 * princípio 9) e o botão vai embaixo dela, para os dois caberem com a fonte
 * em 200%. O TalkBack anuncia o aviso quando ele aparece.
 */
@Composable
fun UndoNotice(message: String, actionLabel: String?, onAction: () -> Unit, modifier: Modifier = Modifier) {
    val tokens = ComicTheme.tokens
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite }
            .comicStyle(
                shape = MaterialTheme.shapes.medium,
                background = MaterialTheme.colorScheme.surface,
                outline = MaterialTheme.colorScheme.outline,
                outlineWidth = tokens.thickOutline,
                shadow = tokens.mediumShadow,
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (actionLabel != null) {
            SecondaryButton(
                text = actionLabel,
                onClick = onAction,
                modifier = Modifier.align(Alignment.End),
            )
        }
    }
}

/** Quanto tempo o aviso fica na tela: 15 s, 50% a mais que o `Long` do Material (#59, público 60+). */
const val UNDO_NOTICE_MILLIS = 15_000L

/**
 * Mostra o aviso com o botão "Ops, voltar" por [UNDO_NOTICE_MILLIS] e devolve `true` se a pessoa tocou
 * no botão. Com o TalkBack ligado, o [accessibilityManager] pode pedir mais tempo (preferência de
 * acessibilidade do Android). Um aviso que já estava na tela sai na hora, sem fila: o novo é o que vale.
 */
suspend fun SnackbarHostState.showUndoNotice(
    message: String,
    actionLabel: String,
    accessibilityManager: AccessibilityManager?,
): Boolean {
    currentSnackbarData?.dismiss()
    val timeout = accessibilityManager?.calculateRecommendedTimeoutMillis(
        originalTimeoutMillis = UNDO_NOTICE_MILLIS,
        containsIcons = false,
        containsText = true,
        containsControls = true,
    ) ?: UNDO_NOTICE_MILLIS
    // Sumir sozinho é cancelar o aviso "Indefinite": o SnackbarHostState tira ele da tela.
    val result = withTimeoutOrNull(timeout) {
        showSnackbar(message = message, actionLabel = actionLabel, duration = SnackbarDuration.Indefinite)
    }
    return result == SnackbarResult.ActionPerformed
}
