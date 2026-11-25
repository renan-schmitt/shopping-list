package br.com.mentealerta.listadecompras.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AccessibilityManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.ui.theme.ComicTheme
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Aviso no meio da tela, com **Desfazer** e **Certo** (guia de linguagem, 2026-11-23).
 * Ex.: "Feijão saiu da lista." · **Desfazer** · **Certo**. O mesmo aviso serve para outra ação no lugar
 * do **Desfazer** (ex.: **Conhecer a versão completa**, #89), mostrado com [showNotice].
 *
 * Uso: `UndoNoticeHost(state)` na tela, mostrando o aviso com [showUndoNotice] ou [showNotice].
 *
 * Revisto em 2026-11-25: o aviso abre num diálogo, com o fundo escurecido e desfocado
 * ([DialogBackdrop]), e nada atrás responde ao toque enquanto ele está na tela. Antes ele ficava solto
 * sobre a tela e se misturava com ela, parecendo erro. Tocar fora da caixa ou no voltar do celular
 * vale como **Certo**; no aviso que espera a pessoa responder ([showNotice] com
 * `closesOnOutsideTap = false`, ex.: o do fim da primeira semana), tocar fora não faz nada.
 */
@Composable
fun UndoNoticeHost(state: SnackbarHostState, modifier: Modifier = Modifier) {
    val data = state.currentSnackbarData ?: return
    val visuals = data.visuals as? NoticeVisuals
    Dialog(
        onDismissRequest = {
            visuals?.dismissedByPerson = true
            data.dismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = visuals?.closesOnOutsideTap ?: true,
        ),
    ) {
        DialogBackdrop()
        SnackbarHost(state, modifier) {
            UndoNotice(it, modifier = Modifier.padding(horizontal = 24.dp))
        }
    }
}

@Composable
fun UndoNotice(data: SnackbarData, modifier: Modifier = Modifier) {
    UndoNotice(
        message = data.visuals.message,
        actionText = data.visuals.actionLabel ?: stringResource(R.string.undo),
        onUndo = data::performAction,
        onDismiss = {
            (data.visuals as? NoticeVisuals)?.dismissedByPerson = true
            data.dismiss()
        },
        modifier = modifier,
    )
}

/**
 * A mensagem fica em caixa normal (pode ter mais de uma linha, guia, princípio 9) e os botões vão
 * embaixo dela, um embaixo do outro e em largura total: [actionText] (por padrão **Desfazer**) e
 * **Certo**. O TalkBack anuncia o aviso quando ele aparece.
 */
@Composable
fun UndoNotice(
    message: String,
    onUndo: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    actionText: String = stringResource(R.string.undo),
) {
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
            text = actionText,
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

/** Como o aviso saiu da tela. */
enum class NoticeResult {
    /** A pessoa tocou na ação (**Desfazer** ou a outra ação do aviso). */
    Action,

    /** A pessoa tocou em **Certo**. */
    Dismissed,

    /** O aviso sumiu sozinho, outro tomou o lugar dele ou a pessoa abriu outra tela. */
    Closed,
}

/** O aviso, lembrando se foi a pessoa que tocou em **Certo**: o `dismiss()` também fecha por código. */
private class NoticeVisuals(
    override val message: String,
    override val actionLabel: String?,
    val closesOnOutsideTap: Boolean,
) : SnackbarVisuals {
    override val withDismissAction = false
    override val duration = SnackbarDuration.Indefinite
    var dismissedByPerson = false
}

/**
 * Mostra o aviso com **Desfazer** e **Certo** por [UNDO_NOTICE_MILLIS] e diz como ele saiu. Com o
 * TalkBack ligado, o [accessibilityManager] pode pedir mais tempo (preferência de acessibilidade do
 * Android). Um aviso que já estava na tela sai na hora, sem fila: o novo é o que vale.
 */
suspend fun SnackbarHostState.showUndoNotice(
    message: String,
    accessibilityManager: AccessibilityManager?,
): NoticeResult {
    val timeout = accessibilityManager?.calculateRecommendedTimeoutMillis(
        originalTimeoutMillis = UNDO_NOTICE_MILLIS,
        containsIcons = false,
        containsText = true,
        containsControls = true,
    ) ?: UNDO_NOTICE_MILLIS
    return showNotice(message, actionText = null, timeoutMillis = timeout, closesOnOutsideTap = true)
}

/**
 * Mostra o aviso com [actionText] (`null`: **Desfazer**) e **Certo**. Sem [timeoutMillis], fica até a
 * pessoa tocar num botão ou outro aviso tomar o lugar. Com [closesOnOutsideTap], tocar fora da caixa
 * vale como **Certo**.
 */
suspend fun SnackbarHostState.showNotice(
    message: String,
    actionText: String?,
    timeoutMillis: Long? = null,
    closesOnOutsideTap: Boolean = false,
): NoticeResult {
    currentSnackbarData?.dismiss()
    val visuals = NoticeVisuals(message, actionText, closesOnOutsideTap)
    // Sumir sozinho é cancelar o aviso "Indefinite": o SnackbarHostState tira ele da tela.
    val result = if (timeoutMillis == null) {
        showSnackbar(visuals)
    } else {
        withTimeoutOrNull(timeoutMillis) { showSnackbar(visuals) }
    }
    return when {
        result == SnackbarResult.ActionPerformed -> NoticeResult.Action
        visuals.dismissedByPerson -> NoticeResult.Dismissed
        else -> NoticeResult.Closed
    }
}
