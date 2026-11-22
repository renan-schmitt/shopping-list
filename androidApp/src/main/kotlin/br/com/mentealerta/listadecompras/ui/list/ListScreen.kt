package br.com.mentealerta.listadecompras.ui.list

import android.content.res.Resources
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
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
import br.com.mentealerta.listadecompras.ui.components.SecondaryButton
import br.com.mentealerta.listadecompras.ui.components.SectionTitle
import br.com.mentealerta.listadecompras.ui.components.UndoNotice
import br.com.mentealerta.listadecompras.ui.components.UppercaseText
import br.com.mentealerta.listadecompras.ui.components.halftone
import br.com.mentealerta.listadecompras.ui.components.showUndoNotice
import br.com.mentealerta.listadecompras.ui.edit.EditScreen
import br.com.mentealerta.listadecompras.ui.theme.ComicTheme
import org.koin.compose.viewmodel.koinViewModel

/**
 * Tela principal: a lista de compras numa folha de papel, agrupada por seção (#50), com os riscados
 * em "Já peguei", no fim (#121). "Adicionar" abre a tela de adicionar (#53); "Mudar a lista" abre o
 * modo de mudar (#121), onde tocar num produto abre a edição (#55) e fica o "Jogar a lista fora".
 * Tudo por cima da lista, sem biblioteca de navegação; continua aberto numa rotação de tela. Ao sair
 * da edição, a pessoa volta para o modo de mudar.
 *
 * Produto tirado da lista na edição mostra o aviso "Feijão saiu da lista." com **Ops, voltar**; o
 * "Terminei" (#57), "Pronto! 8 produtos comprados. Ficaram 3 na lista para a próxima vez.". "Jogar a
 * lista fora" (#58) pergunta antes, volta para a lista e mostra "A lista foi jogada fora." com
 * **Ops, voltar**.
 * Cada aviso fica 15 s na tela (#59) e um novo tira o anterior; riscar não mostra aviso (guia).
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
    val notice by viewModel.notice.collectAsStateWithLifecycle()
    var adding by rememberSaveable { mutableStateOf(false) }
    var changing by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var removedName by rememberSaveable { mutableStateOf<String?>(null) }
    var askingToClear by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalContext.current.resources

    val accessibilityManager = LocalAccessibilityManager.current

    LaunchedEffect(removedName) {
        val name = removedName ?: return@LaunchedEffect
        val undo = snackbarHostState.showUndoNotice(
            message = resources.getString(R.string.list_item_removed, name.replaceFirstChar { it.titlecase() }),
            actionLabel = resources.getString(R.string.undo),
            accessibilityManager = accessibilityManager,
        )
        if (undo) viewModel.onUndo()
        removedName = null
    }
    LaunchedEffect(notice) {
        val current = notice ?: return@LaunchedEffect
        val undo = snackbarHostState.showUndoNotice(
            message = noticeMessage(resources, current),
            actionLabel = resources.getString(R.string.undo),
            accessibilityManager = accessibilityManager,
        )
        if (undo) viewModel.onUndo()
        viewModel.onNoticeShown()
    }

    // Sem produtos, não há o que mudar: volta para a lista vazia.
    LaunchedEffect(uiState) { if (uiState == ListUiState.Empty) changing = false }

    val snackbarHost = @Composable {
        SnackbarHost(snackbarHostState) {
            UndoNotice(it, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }
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
        changing -> {
            BackHandler { changing = false }
            ChangeListContent(
                uiState = uiState,
                onEdit = { editingId = it.value },
                onClear = { askingToClear = true },
                onClose = { changing = false },
                snackbarHost = snackbarHost,
                modifier = modifier,
            )
        }
        else -> ListContent(
            uiState = uiState,
            onToggleCheck = viewModel::onToggleCheck,
            onAdd = { adding = true },
            onFinish = viewModel::onFinish,
            onChange = { changing = true },
            snackbarHost = snackbarHost,
            modifier = modifier,
        )
    }

    // Se a lista esvaziar com a pergunta aberta, ela some sozinha.
    val total = (uiState as? ListUiState.Content)?.total ?: 0
    if (askingToClear && total > 0) {
        ClearListDialog(
            total = total,
            onConfirm = {
                askingToClear = false
                changing = false
                viewModel.onClearList()
            },
            onDismiss = { askingToClear = false },
        )
    }
}

/**
 * "Pronto! 8 produtos comprados. Ficaram 3 na lista para a próxima vez." (guia de linguagem); sem nada
 * na lista, só a primeira frase.
 */
private fun noticeMessage(resources: Resources, notice: ListNotice): String = when (notice) {
    ListNotice.Cleared -> resources.getString(R.string.list_cleared)
    is ListNotice.Finished -> {
        val bought = resources.getQuantityString(R.plurals.list_finished_bought, notice.bought, notice.bought)
        if (notice.remaining == 0) {
            bought
        } else {
            val remaining = resources.getQuantityString(
                R.plurals.list_finished_remaining,
                notice.remaining,
                notice.remaining,
            )
            resources.getString(R.string.list_finished, bought, remaining)
        }
    }
}

/**
 * Fundo com retícula, a folha da lista (plano §4.8) e o rodapé que se adapta (#52). [onToggleCheck]
 * recebe o produto e o estado atual dele, para riscar ou tirar o risco. [onChange] é o "Mudar a
 * lista", no fim da folha. [snackbarHost] mostra os avisos com "Ops, voltar" acima do rodapé.
 * Comprando, a tela não apaga.
 */
@Composable
fun ListContent(
    uiState: ListUiState,
    modifier: Modifier = Modifier,
    onToggleCheck: (ListItemId, Boolean) -> Unit = { _, _ -> },
    onAdd: () -> Unit = {},
    onFinish: () -> Unit = {},
    onChange: () -> Unit = {},
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
                    is ListUiState.Content -> Sheet(uiState.sections, onToggleCheck, onChange)
                }
            }
        }
    }
}

/**
 * A folha: seções com o que falta e "Já peguei" no fim. Riscar move o produto de seção com
 * animação, para a pessoa ver para onde ele foi.
 */
@Composable
private fun Sheet(sections: List<SectionUi>, onToggleCheck: (ListItemId, Boolean) -> Unit, onChange: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 16.dp),
    ) {
        sections.forEach { section ->
            item(key = "section-${section.id}", contentType = "section") {
                SectionTitle(
                    text = section.title(),
                    modifier = Modifier.animateItem().padding(top = 16.dp, bottom = 6.dp),
                )
            }
            items(section.rows, key = { it.id.value }, contentType = { "row" }) { row ->
                ListItemRow(
                    row = row,
                    onToggleCheck = { onToggleCheck(row.id, row.checked) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
        // Longe do polegar, no fim da folha: mudar a lista é raro.
        item(key = "change", contentType = "change") {
            SecondaryButton(
                text = stringResource(R.string.list_change),
                onClick = onChange,
                icon = painterResource(R.drawable.ic_edit),
                modifier = Modifier.animateItem().fillMaxWidth().padding(top = 36.dp, end = 4.dp, bottom = 4.dp),
            )
        }
    }
}

/** Nome da seção; a dos riscados é "Já peguei". */
@Composable
internal fun SectionUi.title(): String = if (bought) stringResource(R.string.list_bought_section) else name

/**
 * Título **Lista de Compras** e "Toque em **Adicionar** para começar sua lista." (guia de
 * linguagem). O texto todo já é Lexend negrito, então o trecho em `<b>` ganha a cor de destaque para
 * se distinguir.
 */
@Composable
private fun EmptyList() {
    val html = AnnotatedString.fromHtml(stringResource(R.string.list_empty))
    val accent = SpanStyle(color = MaterialTheme.colorScheme.primary)
    val text = buildAnnotatedString {
        append(html.text)
        html.spanStyles.forEach { addStyle(accent, it.start, it.end) }
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        UppercaseText(
            text = stringResource(R.string.list_title),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth().semantics { heading() },
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
