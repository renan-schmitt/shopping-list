package br.com.mentealerta.listadecompras.ui.fullversion

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme

// Estados de exemplo da tela "Versão completa" (#88). Ficam no source set `debug` para não entrar na
// versão publicada.

/** Versão grátis com o preço da loja, para os screenshots e a auditoria de acessibilidade. */
val SampleFullVersionState = FullVersionUiState(isComplete = false, price = "R$ 19,90")

/** Depois de um "Já comprei" que não achou a compra. */
val SampleFullVersionMessageState = SampleFullVersionState.copy(message = FullVersionMessage.NothingToRestore)

/** Com a janela da loja aberta: botões desativados. */
val SampleFullVersionBusyState = SampleFullVersionState.copy(busy = true)

/** Com a versão completa: o agradecimento no lugar dos botões. */
val SampleFullVersionCompleteState = FullVersionUiState(isComplete = true, price = "R$ 19,90")

@Preview(name = "Versão completa — claro", widthDp = 411, heightDp = 1200)
@Composable
private fun FullVersionLightPreview() {
    ShoppingListTheme(darkTheme = false) { FullVersionContent(SampleFullVersionState) }
}

@Preview(name = "Versão completa — escuro", widthDp = 411, heightDp = 1200)
@Composable
private fun FullVersionDarkPreview() {
    ShoppingListTheme(darkTheme = true) { FullVersionContent(SampleFullVersionMessageState) }
}

@Preview(name = "Versão completa — comprada", widthDp = 411, heightDp = 1000)
@Composable
private fun FullVersionCompletePreview() {
    ShoppingListTheme(darkTheme = false) { FullVersionContent(SampleFullVersionCompleteState) }
}
