package br.com.mentealerta.listadecompras.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Inclinação da faixa, como no protótipo. */
private const val InclinacaoDaFaixa = -1.5f

/**
 * Título de seção ("Hortifrúti", "Limpeza"): faixa preta levemente
 * inclinada, letra Bangers clara em caixa alta (plano §4.8).
 *
 * Marcado como título para o TalkBack poder pular de seção em seção. Nomes
 * longos quebram a linha.
 */
@Composable
fun TituloDeSecao(texto: String, modifier: Modifier = Modifier) {
    val cores = MaterialTheme.colorScheme
    TextoCaixaAlta(
        texto = texto,
        estilo = MaterialTheme.typography.titleLarge,
        cor = cores.inverseOnSurface,
        alinhamento = TextAlign.Start,
        modifier = modifier
            .semantics { heading() }
            .rotate(InclinacaoDaFaixa)
            .background(cores.inverseSurface)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    )
}
