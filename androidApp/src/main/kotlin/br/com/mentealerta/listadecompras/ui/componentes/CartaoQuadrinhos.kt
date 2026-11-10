package br.com.mentealerta.listadecompras.ui.componentes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import br.com.mentealerta.listadecompras.ui.tema.TemaQuadrinhos

/**
 * Cartão do estilo quadrinhos: papel creme, contorno grosso e sombra sólida
 * grande (plano §4.8). Base da folha da lista e de painéis.
 */
@Composable
fun CartaoQuadrinhos(
    modifier: Modifier = Modifier,
    conteudo: @Composable ColumnScope.() -> Unit,
) {
    val tokens = TemaQuadrinhos.tokens
    Column(
        modifier = modifier.estiloQuadrinhos(
            forma = MaterialTheme.shapes.large,
            fundo = MaterialTheme.colorScheme.surface,
            contorno = MaterialTheme.colorScheme.outline,
            larguraDoContorno = tokens.contornoGrosso,
            sombra = tokens.sombraGrande,
        ),
        content = conteudo,
    )
}
