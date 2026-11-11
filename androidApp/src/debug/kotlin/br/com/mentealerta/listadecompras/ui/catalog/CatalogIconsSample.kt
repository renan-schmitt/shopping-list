package br.com.mentealerta.listadecompras.ui.catalog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme

/**
 * Todos os ícones do catálogo convertidos dos SVG, para conferir o visual (#22). Fica no source
 * set `debug` para não entrar na versão publicada; os nomes são os da planilha, só para a curadoria.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CatalogIconsSample(modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CatalogIconResources.all.forEach { (key, res) ->
            Column(modifier = Modifier.width(79.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painter = painterResource(res), contentDescription = null, modifier = Modifier.size(48.dp))
                Text(
                    text = key.substringAfter(':'),
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

@Preview(name = "Ícones do catálogo", widthDp = 411)
@Composable
private fun CatalogIconsPreview() {
    ShoppingListTheme(darkTheme = false) { CatalogIconsSample() }
}
