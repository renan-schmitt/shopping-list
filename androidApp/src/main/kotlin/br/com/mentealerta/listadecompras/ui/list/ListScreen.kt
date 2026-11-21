package br.com.mentealerta.listadecompras.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.ui.add.AddScreen
import br.com.mentealerta.listadecompras.ui.components.ComicCard
import br.com.mentealerta.listadecompras.ui.components.SectionTitle
import br.com.mentealerta.listadecompras.ui.components.UndoNotice
import br.com.mentealerta.listadecompras.ui.components.halftone
import br.com.mentealerta.listadecompras.ui.edit.EditScreen
import br.com.mentealerta.listadecompras.ui.theme.ComicTheme
import org.koin.compose.viewmodel.koinViewModel

/**
 * Tela principal: a lista de compras numa folha de papel, agrupada por seção (#50). "Adicionar" abre
 * a tela de adicionar (#53) e tocar num produto abre a edição (#55), as duas por cima da lista, sem
 * biblioteca de navegação; elas continuam abertas numa rotação de tela.
 *
 * Produto tirado da lista na edição mostra o aviso "Feijão saiu da lista." com **Ops, voltar**.
 * [editScreen] é a tela de edição; os testes trocam por uma com o ViewModel deles.
 */
@Composable
fun ListScreen(
    modifier: Modifier = Modifier,
    viewModel: ListViewModel = koinViewModel(),
    editScreen: @Composable (id: ListItemId, onClose: (removedName: String?) -> Unit) -> Unit = { id, onClose ->
        EditScreen(id = id, onClose = onClose)
    },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var adding by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var removedName by rememberSaveable { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalContext.current.resources

    // A duração definitiva do aviso fica para a #59.
    LaunchedEffect(removedName) {
        val name = removedName ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = resources.getString(R.string.list_item_removed, name.replaceFirstChar { it.titlecase() }),
            actionLabel = resources.getString(R.string.undo),
            duration = SnackbarDuration.Long,
        )
        if (result == SnackbarResult.ActionPerformed) viewModel.onUndo()
        removedName = null
    }

    val editing = editingId
    when {
        adding -> AddScreen(onClose = { adding = false }, modifier = modifier)
        editing != null -> Box(modifier) {
            editScreen(ListItemId(editing)) { removed ->
                editingId = null
                if (removed != null) removedName = removed
            }
        }
        // Terminei (#57) ganha ação na tarefa dele.
        else -> ListContent(
            uiState = uiState,
            onToggleCheck = viewModel::onToggleCheck,
            onEdit = { editingId = it.value },
            onAdd = { adding = true },
            snackbarHost = {
                SnackbarHost(snackbarHostState) {
                    UndoNotice(it, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
            },
            modifier = modifier,
        )
    }
}

/**
 * Fundo com retícula, a folha da lista (plano §4.8) e o rodapé que se adapta (#52). [onToggleCheck]
 * recebe o produto e o estado atual dele, para riscar ou tirar o risco; [onEdit], o produto tocado.
 * [snackbarHost] mostra os avisos com "Ops, voltar" acima do rodapé. Comprando, a tela não apaga.
 */
@Composable
fun ListContent(
    uiState: ListUiState,
    modifier: Modifier = Modifier,
    onToggleCheck: (ListItemId, Boolean) -> Unit = { _, _ -> },
    onEdit: (ListItemId) -> Unit = {},
    onAdd: () -> Unit = {},
    onFinish: () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val content = uiState as? ListUiState.Content
    val isShopping = content?.isShopping == true
    if (isShopping) KeepScreenOn()
    Scaffold(
        // A retícula cobre a tela toda, por trás da folha e do rodapé.
        modifier = modifier
            .background(colors.background)
            .halftone(colors.outline, ComicTheme.tokens.halftoneSpacing),
        containerColor = Color.Transparent,
        snackbarHost = snackbarHost,
        bottomBar = {
            if (uiState != ListUiState.Loading) {
                ListFooter(
                    isShopping = isShopping,
                    remaining = content?.remaining ?: 0,
                    onAdd = onAdd,
                    onFinish = onFinish,
                )
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 8.dp, vertical = 12.dp),
        ) {
            ComicCard(modifier = Modifier.fillMaxSize()) {
                when (uiState) {
                    ListUiState.Loading -> Unit
                    ListUiState.Empty -> EmptyList()
                    is ListUiState.Content -> Sheet(uiState.sections, onToggleCheck, onEdit)
                }
            }
        }
    }
}

@Composable
private fun Sheet(
    sections: List<SectionUi>,
    onToggleCheck: (ListItemId, Boolean) -> Unit,
    onEdit: (ListItemId) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 16.dp),
    ) {
        sections.forEach { section ->
            item(key = "section-${section.id}", contentType = "section") {
                SectionTitle(text = section.name, modifier = Modifier.padding(top = 16.dp, bottom = 6.dp))
            }
            items(section.rows, key = { it.id.value }, contentType = { "row" }) { row ->
                ListItemRow(
                    row = row,
                    onToggleCheck = { onToggleCheck(row.id, row.checked) },
                    onEdit = { onEdit(row.id) },
                )
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
