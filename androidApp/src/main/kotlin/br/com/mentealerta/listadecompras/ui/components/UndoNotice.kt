package br.com.mentealerta.listadecompras.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AccessibilityManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.ui.theme.ComicTheme
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Aviso no meio da tela, com **Desfazer** e **Certo** (guia de linguagem, 2026-11-23).
 * Ex.: "Feijão saiu da lista." · **Desfazer** · **Certo**.
 *
 * Uso: `UndoNoticeHost(state)` por cima da tela, mostrando o aviso com [showUndoNotice]. Não escurece
 * nem trava o resto da tela: tocar fora do aviso continua funcionando.
 */
@Composable
fun UndoNoticeHost(state: SnackbarHostState, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        SnackbarHost(state) {
            UndoNotice(it, modifier = Modifier.padding(horizontal = 24.dp))
        }
    }
}

@Composable
fun UndoNotice(data: SnackbarData, modifier: Modifier = Modifier) {
    UndoNotice(
        message = data.visuals.message,
        onUndo = data::performAction,
        onDismiss = data::dismiss,
        modifier = modifier,
    )
}

/**
 * A mensagem fica em caixa normal (pode ter mais de uma linha, guia, princípio 9) e os botões vão
 * embaixo dela, um embaixo do outro e em largura total. O TalkBack anuncia o aviso quando ele aparece.
 */
@Composable
fun UndoNotice(message: String, onUndo: () -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
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
                shadow = tokens.largeShadow,
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        // Um embaixo do outro, em largura total: lado a lado, "Desfazer" partia a palavra com a fonte
        // em 200%.
        SecondaryButton(
            text = stringResource(R.string.undo),
            onClick = onUndo,
            modifier = Modifier.fillMaxWidth(),
        )
        PrimaryButton(
            text = stringResource(R.string.notice_dismiss),
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Quanto tempo o aviso fica na tela: 15 s, 50% a mais que o `Long` do Material (#59, público 60+). */
const val UNDO_NOTICE_MILLIS = 15_000L

/**
 * Mostra o aviso com **Desfazer** e **Certo** por [UNDO_NOTICE_MILLIS] e devolve `true` se a pessoa
 * tocou em **Desfazer**; **Certo** só fecha o aviso. Com o TalkBack ligado, o [accessibilityManager]
 * pode pedir mais tempo (preferência de acessibilidade do Android). Um aviso que já estava na tela
 * sai na hora, sem fila: o novo é o que vale.
 */
suspend fun SnackbarHostState.showUndoNotice(message: String, accessibilityManager: AccessibilityManager?): Boolean {
    currentSnackbarData?.dismiss()
    val timeout = accessibilityManager?.calculateRecommendedTimeoutMillis(
        originalTimeoutMillis = UNDO_NOTICE_MILLIS,
        containsIcons = false,
        containsText = true,
        containsControls = true,
    ) ?: UNDO_NOTICE_MILLIS
    // Sumir sozinho é cancelar o aviso "Indefinite": o SnackbarHostState tira ele da tela.
    val result = withTimeoutOrNull(timeout) {
        showSnackbar(message = message, duration = SnackbarDuration.Indefinite)
    }
    return result == SnackbarResult.ActionPerformed
}
