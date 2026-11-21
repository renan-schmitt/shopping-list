package br.com.mentealerta.listadecompras.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.ui.components.PrimaryButton
import br.com.mentealerta.listadecompras.ui.components.SecondaryButton

/**
 * Rodapé que se adapta (#52, plano §1.1). Antes de riscar: só o botão grande "Adicionar". Depois do
 * primeiro risco: "Adicionar" menor em cima e o "Terminei" grande embaixo, perto do polegar, com
 * quantos faltam. Os dois sempre empilhados, em largura total (plano §2, item 12).
 */
@Composable
fun ListFooter(
    isShopping: Boolean,
    remaining: Int,
    onAdd: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val addIcon = painterResource(R.drawable.ic_add)
    val addText = stringResource(R.string.list_add)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (isShopping) {
            SecondaryButton(text = addText, onClick = onAdd, icon = addIcon, modifier = Modifier.fillMaxWidth())
            PrimaryButton(
                text = stringResource(R.string.list_finish),
                supportingText = remainingText(remaining),
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            PrimaryButton(text = addText, onClick = onAdd, icon = addIcon, modifier = Modifier.fillMaxWidth())
        }
    }
}

/** "Falta 1", "Faltam 5" ou, com tudo riscado, "Não falta nada". */
@Composable
private fun remainingText(remaining: Int): String = if (remaining == 0) {
    stringResource(R.string.list_nothing_remaining)
} else {
    pluralStringResource(R.plurals.list_remaining, remaining, remaining)
}

/**
 * Mantém a tela acesa enquanto está na composição (plano §1.7): o mesmo que `FLAG_KEEP_SCREEN_ON`,
 * mas só enquanto a janela mostra esta tela.
 */
@Composable
fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}
