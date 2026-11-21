package br.com.mentealerta.listadecompras.ui.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.ui.components.ComicCard
import br.com.mentealerta.listadecompras.ui.components.SectionTitle
import br.com.mentealerta.listadecompras.ui.components.halftone
import br.com.mentealerta.listadecompras.ui.theme.ComicTheme
import org.koin.compose.viewmodel.koinViewModel

/** Tela principal: a lista de compras numa folha de papel, agrupada por seção (#50). */
@Composable
fun ListScreen(modifier: Modifier = Modifier, viewModel: ListViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ListContent(uiState = uiState, modifier = modifier)
}

/** Fundo com retícula e, por cima, a folha da lista (plano §4.8). */
@Composable
fun ListContent(uiState: ListUiState, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Scaffold(modifier = modifier, containerColor = colors.background) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .halftone(colors.outline, ComicTheme.tokens.halftoneSpacing)
                .padding(padding)
                .padding(horizontal = 8.dp, vertical = 12.dp),
        ) {
            ComicCard(modifier = Modifier.fillMaxSize()) {
                when (uiState) {
                    ListUiState.Loading -> Unit
                    ListUiState.Empty -> EmptyList()
                    is ListUiState.Content -> Sheet(uiState.sections)
                }
            }
        }
    }
}

@Composable
private fun Sheet(sections: List<SectionUi>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 16.dp),
    ) {
        sections.forEach { section ->
            item(key = "section-${section.id}", contentType = "section") {
                SectionTitle(text = section.name, modifier = Modifier.padding(top = 16.dp, bottom = 6.dp))
            }
            items(section.rows, key = { it.id.value }, contentType = { "row" }) { row ->
                ListItemRow(row = row)
            }
        }
    }
}

/**
 * "Sua lista está vazia. Toque em **Adicionar** para começar." (guia de linguagem). O texto todo já
 * é Lexend negrito, então o trecho em `<b>` ganha a cor de destaque para se distinguir.
 */
@Composable
private fun EmptyList() {
    val html = AnnotatedString.fromHtml(stringResource(R.string.list_empty))
    val accent = SpanStyle(color = MaterialTheme.colorScheme.primary)
    val text = buildAnnotatedString {
        append(html.text)
        html.spanStyles.forEach { addStyle(accent, it.start, it.end) }
    }
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
