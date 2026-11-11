package br.com.mentealerta.listadecompras.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
fun ThemeSample(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val tokens = ComicTheme.tokens

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Mente Alerta", style = typography.bodySmall, color = colors.onBackground)
        Text("Lista de Compras", style = typography.displayMedium, color = colors.onBackground)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Swatch(colors.background, "Fundo")
            Swatch(colors.surface, "Papel")
            Swatch(colors.onSurface, "Tinta")
            Swatch(colors.onSurfaceVariant, "Riscado")
            Swatch(colors.primary, "Verde")
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(tokens.thickOutline, colors.outline, MaterialTheme.shapes.large)
                .background(colors.surface, MaterialTheme.shapes.large)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Hortifrúti", style = typography.titleLarge, color = colors.onSurface)
            ItemRow {
                Text("Arroz", style = typography.bodyLarge, color = colors.onSurface, modifier = Modifier.weight(1f))
                Text("2 pacotes", style = typography.bodyMedium, color = colors.onSurface)
            }
            ItemRow {
                Text(
                    "Biscoito de polvilho salgado da padaria da esquina",
                    style = typography.bodyLarge,
                    color = colors.onSurface,
                )
            }
            ItemRow {
                Text(
                    "Feijão",
                    style = typography.bodyLarge,
                    color = colors.onSurfaceVariant,
                    textDecoration = TextDecoration.LineThrough,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = tokens.primaryButtonHeight)
                .border(tokens.thickOutline, colors.outline, MaterialTheme.shapes.medium)
                .background(colors.primary, MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center,
        ) {
            Text("Terminei", style = typography.headlineMedium, color = colors.onPrimary)
        }
        Text("Pôr na lista", style = typography.labelLarge, color = colors.primary)
    }
}

@Composable
private fun ItemRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ComicTheme.tokens.minRowHeight),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun Swatch(color: Color, name: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .border(ComicTheme.tokens.thinOutline, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                .background(color, MaterialTheme.shapes.small),
        )
        Text(name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Preview(name = "Claro", widthDp = 411)
@Composable
private fun LightPreview() {
    ShoppingListTheme(darkTheme = false) { ThemeSample() }
}

@Preview(name = "Escuro", widthDp = 411)
@Composable
private fun DarkPreview() {
    ShoppingListTheme(darkTheme = true) { ThemeSample() }
}

@Preview(name = "Fonte 200%", widthDp = 411, fontScale = 2f)
@Composable
private fun Font200Preview() {
    ShoppingListTheme(darkTheme = false) { ThemeSample() }
}
