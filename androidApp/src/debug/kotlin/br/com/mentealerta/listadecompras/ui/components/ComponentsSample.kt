package br.com.mentealerta.listadecompras.ui.components

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
import br.com.mentealerta.listadecompras.ui.theme.ComicTheme
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme

/**
 * Amostra dos componentes base, para preview e screenshot (#12). Fica no
 * source set `debug` para não entrar na versão publicada.
 */
@Composable
fun ComponentsSample(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        CardSample()
        FooterSample()
        StandaloneButtonsSample()
        NoticeSample()
    }
}

@Composable
private fun CardSample() {
    ComicCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionTitle("Hortifrúti")
            Text("Banana", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            SectionTitle("Frios, laticínios e congelados da semana")
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
private fun FooterSample() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PrimaryButton(
            text = "Terminei",
            supportingText = "Faltam 5",
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        )
        SecondaryButton(
            text = "Adicionar",
            onClick = {},
            icon = painterResource(R.drawable.ic_add),
            minHeight = ComicTheme.tokens.primaryButtonHeight,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StandaloneButtonsSample() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PrimaryButton(
            text = "Adicionar",
            onClick = {},
            icon = painterResource(R.drawable.ic_add),
            modifier = Modifier.fillMaxWidth(),
        )
        SecondaryButton(text = "Jogar a lista fora", onClick = {})
    }
}

@Composable
private fun NoticeSample() {
    UndoNotice(message = "Feijão saiu da lista.", actionLabel = "Ops, voltar", onAction = {})
}

@Composable
private fun InTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    ShoppingListTheme(darkTheme = darkTheme) {
        Column(Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) { content() }
    }
}

@Preview(name = "Todos — claro", widthDp = 411)
@Composable
private fun AllLightPreview() {
    ShoppingListTheme(darkTheme = false) { ComponentsSample() }
}

@Preview(name = "Todos — escuro", widthDp = 411)
@Composable
private fun AllDarkPreview() {
    ShoppingListTheme(darkTheme = true) { ComponentsSample() }
}

@Preview(name = "Todos — fonte 200%", widthDp = 411, fontScale = 2f)
@Composable
private fun AllFont200Preview() {
    ShoppingListTheme(darkTheme = false) { ComponentsSample() }
}

@Preview(name = "Botões", widthDp = 411)
@Composable
private fun ButtonsPreview() = InTheme { FooterSample() }

@Preview(name = "Título de seção", widthDp = 411)
@Composable
private fun SectionTitlePreview() = InTheme { SectionTitle("Limpeza") }

@Preview(name = "Cartão", widthDp = 411)
@Composable
private fun CardPreview() = InTheme { CardSample() }

@Preview(name = "Aviso", widthDp = 411)
@Composable
private fun NoticePreview() = InTheme { NoticeSample() }
