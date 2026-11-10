package br.com.mentealerta.listadecompras.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.mentealerta.listadecompras.ui.tema.TemaQuadrinhos

/**
 * Aviso curto no pé da tela, com o botão "Ops, voltar" (guia de linguagem).
 * Ex.: "Feijão saiu da lista." · **Ops, voltar**.
 *
 * Uso: `SnackbarHost(estado) { AvisoComVoltar(it) }`. Quanto tempo o aviso
 * fica na tela é decidido por quem mostra; para 60+, prefira
 * `SnackbarDuration.Long` ou mais.
 */
@Composable
fun AvisoComVoltar(dados: SnackbarData, modifier: Modifier = Modifier) {
    AvisoComVoltar(
        mensagem = dados.visuals.message,
        textoDoBotao = dados.visuals.actionLabel,
        aoTocarNoBotao = dados::performAction,
        modifier = modifier,
    )
}

/**
 * A mensagem fica em caixa normal (pode ter mais de uma linha, guia,
 * princípio 9) e o botão vai embaixo dela, para os dois caberem com a fonte
 * em 200%. O TalkBack anuncia o aviso quando ele aparece.
 */
@Composable
fun AvisoComVoltar(
    mensagem: String,
    textoDoBotao: String?,
    aoTocarNoBotao: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = TemaQuadrinhos.tokens
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite }
            .estiloQuadrinhos(
                forma = MaterialTheme.shapes.medium,
                fundo = MaterialTheme.colorScheme.surface,
                contorno = MaterialTheme.colorScheme.outline,
                larguraDoContorno = tokens.contornoGrosso,
                sombra = tokens.sombraMedia,
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = mensagem,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (textoDoBotao != null) {
            BotaoSecundario(
                texto = textoDoBotao,
                aoTocar = aoTocarNoBotao,
                modifier = Modifier.align(Alignment.End),
            )
        }
    }
}
