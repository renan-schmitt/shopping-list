package br.com.mentealerta.listadecompras.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.ui.tema.ListaDeComprasTheme
import br.com.mentealerta.listadecompras.ui.tema.TemaQuadrinhos

/**
 * Amostra dos componentes base, para preview e screenshot (#12). Fica no
 * source set `debug` para não entrar na versão publicada.
 */
@Composable
fun AmostraDosComponentes() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        AmostraDoCartao()
        AmostraDoRodape()
        AmostraDosBotoesSozinhos()
        AmostraDoAviso()
    }
}

@Composable
private fun AmostraDoCartao() {
    CartaoQuadrinhos(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TituloDeSecao("Hortifrúti")
            Text("Banana", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            TituloDeSecao("Frios, laticínios e congelados da semana")
            Text("Queijo", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

/**
 * "Terminei" e "Adicionar" um embaixo do outro. Lado a lado, como no
 * protótipo, "Adicionar" em Lexend 22sp não cabe num celular de 411dp sem
 * partir a palavra; o arranjo final fica com a tela da lista.
 */
@Composable
private fun AmostraDoRodape() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BotaoPrincipal(
            texto = "Terminei",
            textoDeApoio = "Faltam 5",
            aoTocar = {},
            modifier = Modifier.fillMaxWidth(),
        )
        BotaoSecundario(
            texto = "Adicionar",
            aoTocar = {},
            icone = painterResource(R.drawable.ic_adicionar),
            alturaMinima = TemaQuadrinhos.tokens.alturaBotaoPrincipal,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AmostraDosBotoesSozinhos() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BotaoPrincipal(
            texto = "Adicionar",
            aoTocar = {},
            icone = painterResource(R.drawable.ic_adicionar),
            modifier = Modifier.fillMaxWidth(),
        )
        BotaoSecundario(texto = "Jogar a lista fora", aoTocar = {})
    }
}

@Composable
private fun AmostraDoAviso() {
    AvisoComVoltar(mensagem = "Feijão saiu da lista.", textoDoBotao = "Ops, voltar", aoTocarNoBotao = {})
}

@Composable
private fun NoTema(escuro: Boolean = false, conteudo: @Composable () -> Unit) {
    ListaDeComprasTheme(escuro = escuro) {
        Column(Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) { conteudo() }
    }
}

@Preview(name = "Todos — claro", widthDp = 411)
@Composable
private fun TodosClaroPreview() {
    ListaDeComprasTheme(escuro = false) { AmostraDosComponentes() }
}

@Preview(name = "Todos — escuro", widthDp = 411)
@Composable
private fun TodosEscuroPreview() {
    ListaDeComprasTheme(escuro = true) { AmostraDosComponentes() }
}

@Preview(name = "Todos — fonte 200%", widthDp = 411, fontScale = 2f)
@Composable
private fun TodosFonte200Preview() {
    ListaDeComprasTheme(escuro = false) { AmostraDosComponentes() }
}

@Preview(name = "Botões", widthDp = 411)
@Composable
private fun BotoesPreview() = NoTema { AmostraDoRodape() }

@Preview(name = "Título de seção", widthDp = 411)
@Composable
private fun TituloDeSecaoPreview() = NoTema { TituloDeSecao("Limpeza") }

@Preview(name = "Cartão", widthDp = 411)
@Composable
private fun CartaoPreview() = NoTema { AmostraDoCartao() }

@Preview(name = "Aviso", widthDp = 411)
@Composable
private fun AvisoPreview() = NoTema { AmostraDoAviso() }
