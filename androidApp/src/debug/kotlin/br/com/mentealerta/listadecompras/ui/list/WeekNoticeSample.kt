package br.com.mentealerta.listadecompras.ui.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.ui.components.UndoNotice
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme

// Exemplo do aviso do fim da primeira semana (#89), por cima da lista. Fica no source set `debug` para
// não entrar na versão publicada.

/** A lista antes de comprar com o aviso no meio da tela, para os screenshots e a acessibilidade. */
@Composable
fun WeekNoticeSample(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        ListContent(SampleListStateNotShopping)
        UndoNotice(
            message = stringResource(R.string.week_notice),
            actionText = stringResource(R.string.week_notice_full_version),
            onUndo = {},
            onDismiss = {},
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 24.dp),
        )
    }
}

@Preview(name = "Aviso do fim da primeira semana", widthDp = 411, heightDp = 891)
@Composable
private fun WeekNoticePreview() {
    ShoppingListTheme(darkTheme = false) { WeekNoticeSample() }
}
