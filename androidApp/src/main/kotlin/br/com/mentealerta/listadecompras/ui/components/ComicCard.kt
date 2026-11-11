package br.com.mentealerta.listadecompras.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import br.com.mentealerta.listadecompras.ui.theme.ComicTheme

/**
 * Cartão do estilo quadrinhos: papel creme, contorno grosso e sombra sólida
 * grande (plano §4.8). Base da folha da lista e de painéis.
 */
@Composable
fun ComicCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val tokens = ComicTheme.tokens
    Column(
        modifier = modifier.comicStyle(
            shape = MaterialTheme.shapes.large,
            background = MaterialTheme.colorScheme.surface,
            outline = MaterialTheme.colorScheme.outline,
            outlineWidth = tokens.thickOutline,
            shadow = tokens.largeShadow,
        ),
        content = content,
    )
}
