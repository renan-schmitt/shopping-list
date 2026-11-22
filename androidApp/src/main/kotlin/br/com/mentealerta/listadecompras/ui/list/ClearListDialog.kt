package br.com.mentealerta.listadecompras.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.ui.components.ComicCard
import br.com.mentealerta.listadecompras.ui.components.PrimaryButton
import br.com.mentealerta.listadecompras.ui.components.SecondaryButton
import br.com.mentealerta.listadecompras.ui.components.UppercaseText

/**
 * Confirmação do "Jogar a lista fora" (#58), a única ação do app que pergunta antes (guia de
 * linguagem): "Jogar a lista fora? Os 12 produtos serão tirados." · **Sim, jogar fora** · **Não**.
 * Tocar fora da caixa ou no voltar do celular é o mesmo que "Não".
 */
@Composable
fun ClearListDialog(total: Int, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    // Largura quase total: a caixa estreita padrão do diálogo apertaria a fonte em 200%.
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        ClearListQuestion(
            total = total,
            onConfirm = onConfirm,
            onDismiss = onDismiss,
            modifier = Modifier.padding(16.dp),
        )
    }
}

/**
 * Conteúdo da confirmação: título, quantos produtos saem e os dois botões empilhados em largura total.
 * Rola quando a fonte grande não cabe na tela.
 */
@Composable
fun ClearListQuestion(total: Int, onConfirm: () -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val title = stringResource(R.string.list_clear_question)
    ComicCard(modifier = modifier.widthIn(max = 480.dp).semantics { paneTitle = title }) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            UppercaseText(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth().semantics { heading() },
            )
            Text(
                text = pluralStringResource(R.plurals.list_clear_count, total, total),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            PrimaryButton(
                text = stringResource(R.string.list_clear_yes),
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
            )
            SecondaryButton(
                text = stringResource(R.string.list_clear_no),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
