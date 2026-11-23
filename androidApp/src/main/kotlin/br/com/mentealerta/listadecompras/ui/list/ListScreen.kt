package br.com.mentealerta.listadecompras.ui.list

import android.content.res.Resources
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import br.com.mentealerta.listadecompras.ui.components.UndoNoticeHost
import br.com.mentealerta.listadecompras.ui.components.UppercaseText
import br.com.mentealerta.listadecompras.ui.components.comicStyle
import br.com.mentealerta.listadecompras.ui.components.halftone
import br.com.mentealerta.listadecompras.ui.components.showUndoNotice
import br.com.mentealerta.listadecompras.ui.edit.EditScreen
import br.com.mentealerta.listadecompras.ui.history.HistoryScreen
import br.com.mentealerta.listadecompras.ui.theme.ComicTheme
import org.koin.compose.viewmodel.koinViewModel

/**
 * Tela principal: a lista de compras numa folha de papel, agrupada por seção (#50), com os riscados
 * em "Já peguei", no fim (#121). "Adicionar" abre a tela de adicionar (#53); "Mudar a lista" abre o
 * modo de mudar (#121), onde tocar num produto abre a edição (#55) e fica o "Jogar a lista fora".
 * Tudo por cima da lista, sem biblioteca de navegação; continua aberto numa rotação de tela. Ao sair
 * da edição, a pessoa volta para o modo de mudar.
 *
 * Produto tirado da lista na edição mostra o aviso "Feijão saiu da lista." com **Desfazer** e **Certo**; o
 * "Terminei" (#57), "Pronto! 8 produtos comprados. Ficaram 3 na lista para a próxima vez.". "Jogar a
 * lista fora" (#58) pergunta antes, volta para a lista e mostra "A lista foi jogada fora.".
 * Os avisos aparecem no meio da tela (2026-11-23), ficam 15 s (#59) e um novo tira o anterior; abrir
 * outra tela fecha o aviso. Riscar não mostra aviso (guia).
 * "O que já comprei" (#66) fica no fim da folha e na lista vazia, só quando há compras no histórico.
 * [editScreen] e [historyScreen] são as telas de edição e do histórico; os testes trocam por outras
 * com o ViewModel deles.
 */
@Composable
fun ListScreen(
    modifier: Modifier = Modifier,
    viewModel: ListViewModel = koinViewModel(),
    editScreen: @Composable (id: ListItemId, onClose: (removedName: String?) -> Unit) -> Unit = { id, onClose ->
        EditScreen(id = id, onClose = onClose)
    },
    historyScreen: @Composable (onClose: () -> Unit) -> Unit = { onClose -> HistoryScreen(onClose = onClose) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var adding by rememberSaveable { mutableStateOf(false) }
    var changing by rememberSaveable { mutableStateOf(false) }
    var viewingHistory by rememberSaveable { mutableStateOf(false) }
    val hasHistory by viewModel.hasHistory.collectAsStateWithLifecycle()
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var removedName by rememberSaveable { mutableStateOf<String?>(null) }
    var askingToClear by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val notice by viewModel.notice.collectAsStateWithLifecycle()
    ListNotices(
        state = snackbarHostState,
        removedName = removedName,
        notice = notice,
        onUndo = viewModel::onUndo,
        onRemovedNoticeEnd = { removedName = null },
        onNoticeEnd = viewModel::onNoticeShown,
    )

    // Sem produtos, não há o que mudar: volta para a lista vazia.
    LaunchedEffect(uiState) { if (uiState == ListUiState.Empty) changing = false }

    // Abrir outra tela fecha o aviso: ele não volta a aparecer quando a pessoa retorna.
    val dismissNotice = { snackbarHostState.currentSnackbarData?.dismiss() }
    val openEdit = { id: ListItemId ->
        dismissNotice()
        editingId = id.value
    }
    val editing = editingId
    Box(modifier) {
        when {
            adding -> AddScreen(onClose = { adding = false })
            viewingHistory -> historyScreen { viewingHistory = false }
            editing != null -> editScreen(ListItemId(editing)) { removed ->
                editingId = null
                if (removed != null) removedName = removed
            }
            changing -> {
                BackHandler { changing = false }
                ChangeListContent(
                    uiState = uiState,
                    onEdit = openEdit,
                    onClear = {
                        dismissNotice()
                        askingToClear = true
                    },
                    onClose = { changing = false },
                )
            }
            else -> ListContent(
                uiState = uiState,
                onToggleCheck = viewModel::onToggleCheck,
                onEdit = openEdit,
                onRemove = viewModel::onRemove,
                onAdd = {
                    dismissNotice()
                    adding = true
                },
                onFinish = viewModel::onFinish,
                onChange = {
                    dismissNotice()
                    changing = true
                },
                hasHistory = hasHistory,
                onHistory = {
                    dismissNotice()
                    viewingHistory = true
                },
            )
        }
        if (!adding && !viewingHistory && editing == null) UndoNoticeHost(snackbarHostState)
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
 * Mostra os avisos no [state]: o produto tirado na edição ([removedName]) e o [notice] do ViewModel
 * (tirar pelo TalkBack, "Terminei", jogar a lista fora). **Desfazer** chama [onUndo]; quando cada
 * aviso sai da tela, [onRemovedNoticeEnd] ou [onNoticeEnd].
 */
@Composable
private fun ListNotices(
    state: SnackbarHostState,
    removedName: String?,
    notice: ListNotice?,
    onUndo: () -> Unit,
    onRemovedNoticeEnd: () -> Unit,
    onNoticeEnd: () -> Unit,
) {
    val resources = LocalContext.current.resources
    val accessibilityManager = LocalAccessibilityManager.current
    val currentOnUndo by rememberUpdatedState(onUndo)
    val currentOnRemovedNoticeEnd by rememberUpdatedState(onRemovedNoticeEnd)
    val currentOnNoticeEnd by rememberUpdatedState(onNoticeEnd)
    LaunchedEffect(removedName) {
        val name = removedName ?: return@LaunchedEffect
        val undo = state.showUndoNotice(
            message = resources.getString(R.string.list_item_removed, name.replaceFirstChar { it.titlecase() }),
            accessibilityManager = accessibilityManager,
        )
        if (undo) currentOnUndo()
        currentOnRemovedNoticeEnd()
    }
    LaunchedEffect(notice) {
        val current = notice ?: return@LaunchedEffect
        val undo = state.showUndoNotice(
            message = noticeMessage(resources, current),
            accessibilityManager = accessibilityManager,
        )
        if (undo) currentOnUndo()
        currentOnNoticeEnd()
    }
}

/**
 * "Pronto! 8 produtos comprados. Ficaram 3 na lista para a próxima vez." (guia de linguagem); sem nada
 * na lista, só a primeira frase.
 */
private fun noticeMessage(resources: Resources, notice: ListNotice): String = when (notice) {
    ListNotice.Cleared -> resources.getString(R.string.list_cleared)
    is ListNotice.Removed -> resources.getString(
        R.string.list_item_removed,
        notice.name.replaceFirstChar {
            it.titlecase()
        },
    )
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
 * lista", no fim da folha. [onEdit] e [onRemove] são as ações "Mudar" e "Tirar da lista" do TalkBack
 * na linha (#71); [onRemove] recebe o produto e o nome dele, para o aviso. Com [hasHistory], o botão
 * "O que já comprei" ([onHistory], #66) aparece no fim da folha ou na lista vazia. Comprando, a tela
 * não apaga.
 */
@Composable
fun ListContent(
    uiState: ListUiState,
    modifier: Modifier = Modifier,
    onToggleCheck: (ListItemId, Boolean) -> Unit = { _, _ -> },
    onEdit: (ListItemId) -> Unit = {},
    onRemove: (ListItemId, String) -> Unit = { _, _ -> },
    onAdd: () -> Unit = {},
    onFinish: () -> Unit = {},
    onChange: () -> Unit = {},
    hasHistory: Boolean = false,
    onHistory: () -> Unit = {},
) {
    val history = onHistory.takeIf { hasHistory }
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
                    ListUiState.Empty -> EmptyList(onHistory = history)
                    is ListUiState.Content -> Sheet(
                        uiState.sections,
                        SheetActions(onToggleCheck, onEdit, onRemove, onChange, history),
                    )
                }
            }
        }
    }
}

/** O que a folha faz com os produtos, o "Mudar a lista" e o "O que já comprei" (`null`: sem histórico). */
private class SheetActions(
    val onToggleCheck: (ListItemId, Boolean) -> Unit,
    val onEdit: (ListItemId) -> Unit,
    val onRemove: (ListItemId, String) -> Unit,
    val onChange: () -> Unit,
    val onHistory: (() -> Unit)?,
)

/**
 * A folha: seções com o que falta e "Já peguei" no fim. Riscar move o produto de seção com
 * animação, para a pessoa ver para onde ele foi.
 */
@Composable
private fun Sheet(sections: List<SectionUi>, actions: SheetActions) {
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
                    onToggleCheck = { actions.onToggleCheck(row.id, row.checked) },
                    onEdit = { actions.onEdit(row.id) },
                    onRemove = { actions.onRemove(row.id, row.name) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
        // Longe do polegar, no fim da folha: mudar a lista é raro.
        item(key = "change", contentType = "change") {
            SecondaryButton(
                text = stringResource(R.string.list_change),
                onClick = actions.onChange,
                icon = painterResource(R.drawable.ic_edit),
                modifier = Modifier.animateItem().fillMaxWidth().padding(top = 36.dp, end = 4.dp, bottom = 4.dp),
            )
        }
        actions.onHistory?.let { onHistory ->
            item(key = "history", contentType = "history") {
                HistoryButton(
                    onClick = onHistory,
                    modifier = Modifier.animateItem().fillMaxWidth().padding(top = 16.dp, end = 4.dp, bottom = 4.dp),
                )
            }
        }
    }
}

/** "O que já comprei" (#66): abre o histórico. */
@Composable
private fun HistoryButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    SecondaryButton(
        text = stringResource(R.string.list_history),
        onClick = onClick,
        icon = painterResource(R.drawable.ic_history),
        modifier = modifier,
    )
}

/** Nome da seção; a dos riscados é "Já peguei". */
@Composable
internal fun SectionUi.title(): String = if (bought) stringResource(R.string.list_bought_section) else name

/**
 * Arte do ícone do app e "Mente Alerta" acima do título **Lista de Compras** (logo do plano §4.8),
 * e "Toque em **Adicionar** para começar sua lista." (guia de linguagem). O texto todo já é Lexend
 * negrito, então o trecho em `<b>` ganha a cor de destaque para se distinguir. Com histórico, embaixo,
 * "O que já comprei" ([onHistory], #66): depois do "Terminei", a lista costuma ficar vazia. Com letra
 * grande, a folha rola em vez de cortar o texto.
 */
@Composable
private fun EmptyList(onHistory: (() -> Unit)?) {
    val html = AnnotatedString.fromHtml(stringResource(R.string.list_empty))
    val accent = SpanStyle(color = MaterialTheme.colorScheme.primary)
    val text = buildAnnotatedString {
        append(html.text)
        html.spanStyles.forEach { addStyle(accent, it.start, it.end) }
    }
    val tokens = ComicTheme.tokens
    BoxWithConstraints(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .heightIn(min = maxHeight)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Decorativa: o nome logo abaixo já diz o que é.
            Image(
                painter = painterResource(R.drawable.logo_app),
                contentDescription = null,
                modifier = Modifier
                    .size(LOGO_SIZE)
                    .comicStyle(
                        shape = LogoShape,
                        background = MaterialTheme.colorScheme.surface,
                        outline = MaterialTheme.colorScheme.outline,
                        outlineWidth = tokens.thickOutline,
                        shadow = tokens.mediumShadow,
                    ),
            )
            // Nome da família pequeno, colado acima do título, como no logo.
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                UppercaseText(
                    text = stringResource(R.string.app_family_name),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                )
                UppercaseText(
                    text = stringResource(R.string.list_title),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth().semantics { heading() },
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            if (onHistory != null) {
                HistoryButton(onClick = onHistory, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            }
        }
    }
}

private val LOGO_SIZE = 136.dp
private val LogoShape = RoundedCornerShape(28.dp)
