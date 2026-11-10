package br.com.mentealerta.listadecompras.ui.tema

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Amostra do tema, só para preview e screenshot (#11). Fica no source set
 * `debug` para não entrar na versão publicada.
 */
@Composable
fun AmostraDoTema() {
    val cores = MaterialTheme.colorScheme
    val texto = MaterialTheme.typography
    val tokens = TemaQuadrinhos.tokens

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(cores.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Mente Alerta", style = texto.bodySmall, color = cores.onBackground)
        Text("Lista de Compras", style = texto.displayMedium, color = cores.onBackground)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Amostra(cores.background, "Fundo")
            Amostra(cores.surface, "Papel")
            Amostra(cores.onSurface, "Tinta")
            Amostra(cores.onSurfaceVariant, "Riscado")
            Amostra(cores.primary, "Verde")
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(tokens.contornoGrosso, cores.outline, MaterialTheme.shapes.large)
                .background(cores.surface, MaterialTheme.shapes.large)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Hortifrúti", style = texto.titleLarge, color = cores.onSurface)
            Linha {
                Text("Arroz", style = texto.bodyLarge, color = cores.onSurface, modifier = Modifier.weight(1f))
                Text("2 pacotes", style = texto.bodyMedium, color = cores.onSurface)
            }
            Linha {
                Text(
                    "Biscoito de polvilho salgado da padaria da esquina",
                    style = texto.bodyLarge,
                    color = cores.onSurface,
                )
            }
            Linha {
                Text(
                    "Feijão",
                    style = texto.bodyLarge,
                    color = cores.onSurfaceVariant,
                    textDecoration = TextDecoration.LineThrough,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = tokens.alturaBotaoPrincipal)
                .border(tokens.contornoGrosso, cores.outline, MaterialTheme.shapes.medium)
                .background(cores.primary, MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center,
        ) {
            Text("Terminei", style = texto.headlineMedium, color = cores.onPrimary)
        }
        Text("Pôr na lista", style = texto.labelLarge, color = cores.primary)
    }
}

@Composable
private fun Linha(conteudo: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = TemaQuadrinhos.tokens.alturaMinimaDaLinha),
        verticalAlignment = Alignment.CenterVertically,
        content = conteudo,
    )
}

@Composable
private fun Amostra(cor: Color, nome: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .border(TemaQuadrinhos.tokens.contornoFino, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                .background(cor, MaterialTheme.shapes.small),
        )
        Text(nome, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Preview(name = "Claro", widthDp = 411)
@Composable
private fun AmostraClaroPreview() {
    ListaDeComprasTheme(escuro = false) { AmostraDoTema() }
}

@Preview(name = "Escuro", widthDp = 411)
@Composable
private fun AmostraEscuroPreview() {
    ListaDeComprasTheme(escuro = true) { AmostraDoTema() }
}

@Preview(name = "Fonte 200%", widthDp = 411, fontScale = 2f)
@Composable
private fun AmostraFonte200Preview() {
    ListaDeComprasTheme(escuro = false) { AmostraDoTema() }
}
