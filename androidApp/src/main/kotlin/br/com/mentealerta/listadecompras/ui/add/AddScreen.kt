package br.com.mentealerta.listadecompras.ui.add

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.ui.components.SecondaryButton
import br.com.mentealerta.listadecompras.ui.components.UppercaseText
import br.com.mentealerta.listadecompras.ui.components.comicStyle
import br.com.mentealerta.listadecompras.ui.list.format
import br.com.mentealerta.listadecompras.ui.theme.ComicTheme
import org.koin.compose.viewmodel.koinViewModel

private val FieldMinHeight = 68.dp
private val PlusSize = 36.dp
private val OptionIconSize = 44.dp

/**
 * Tela de adicionar digitando (#53, plano §1.2): ocupa a tela toda, por cima da lista. [onClose]
 * volta para a lista, pelo botão "Voltar para a lista" ou pelo voltar do celular.
 */
@Composable
fun AddScreen(onClose: () -> Unit, modifier: Modifier = Modifier, viewModel: AddViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val close = {
        viewModel.onClose()
        onClose()
    }
    BackHandler(onBack = close)
    AddContent(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onAdd = viewModel::onAdd,
        onSubmit = viewModel::onSubmit,
        onClose = close,
        modifier = modifier,
    )
}

/**
 * Topo verde com "Voltar para a lista" e o campo "Procurar produto" (protótipo navegável); embaixo,
 * no papel, o aviso do que entrou e as opções da busca. O campo abre com o teclado.
 */
@Composable
fun AddContent(
    uiState: AddUiState,
    modifier: Modifier = Modifier,
    onQueryChange: (String) -> Unit = {},
    onAdd: (AddOptionUi) -> Unit = {},
    onSubmit: () -> Unit = {},
    onClose: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = modifier.fillMaxSize().background(colors.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.primary)
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SecondaryButton(
                text = stringResource(R.string.add_back_to_list),
                onClick = onClose,
                icon = painterResource(R.drawable.ic_arrow_back),
            )
            SearchField(query = uiState.query, onQueryChange = onQueryChange, onSubmit = onSubmit)
        }
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().imePadding(),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item(key = "added", contentType = "added") { AddedNotice(uiState.lastAdded) }
            if (uiState.found.isNotEmpty()) {
                item(key = "found-title", contentType = "title") { OptionsTitle(stringResource(R.string.add_found)) }
                items(uiState.found, contentType = { "option" }) {
                    OptionButton(option = it, unknown = false, onClick = { onAdd(it) })
                }
            }
            uiState.unknown?.let { unknown ->
                if (uiState.found.isEmpty()) {
                    item(key = "nothing-found", contentType = "nothing-found") { NothingFound() }
                }
                item(key = "unknown", contentType = "option") {
                    OptionButton(option = unknown, unknown = true, onClick = { onAdd(unknown) })
                }
            }
            item(key = "bottom", contentType = "bottom") { Box(Modifier.navigationBarsPadding()) }
        }
    }
}

/**
 * Campo grande com o rótulo em cima, em caixa alta. O TalkBack ouve o rótulo pelo próprio campo, em
 * caixa normal. Tecla "pronto" põe a primeira opção na lista.
 */
@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, onSubmit: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val tokens = ComicTheme.tokens
    val label = stringResource(R.string.add_search_label)
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label.toUpperCase(LocaleList.current),
            style = MaterialTheme.typography.labelLarge,
            color = colors.onPrimary,
            modifier = Modifier.clearAndSetSemantics {},
        )
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
            cursorBrush = SolidColor(colors.onSurface),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .semantics { contentDescription = label }
                .border(tokens.thickOutline, colors.outline, MaterialTheme.shapes.medium)
                .background(colors.surface, MaterialTheme.shapes.medium)
                .heightIn(min = FieldMinHeight)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            decorationBox = { inner -> Box(contentAlignment = Alignment.CenterStart) { inner() } },
        )
    }
}

/** "Leite entrou na lista.", lido pelo TalkBack assim que aparece. Some quando a pessoa volta a digitar. */
@Composable
private fun AddedNotice(name: String?) {
    val text = name?.let { stringResource(R.string.add_added, it.capitalized()) }.orEmpty()
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (name == null) 0.dp else 8.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
    )
}

@Composable
private fun OptionsTitle(text: String) {
    UppercaseText(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 8.dp).semantics { heading() },
    )
}

@Composable
private fun NothingFound() {
    Text(
        text = stringResource(R.string.add_nothing_found),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).semantics { heading() },
    )
}

/**
 * Opção da busca: "+" → nome (com detalhe e quantidade) → ícone do produto, num cartão de no mínimo
 * 72dp. Nome longo quebra a linha. O produto desconhecido aparece como frase, em caixa normal:
 * "Pôr “bergamota” na lista". Para o TalkBack é um botão só, lido em caixa normal.
 */
@Composable
private fun OptionButton(option: AddOptionUi, unknown: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val tokens = ComicTheme.tokens
    val named = option.detail?.let { stringResource(R.string.list_item_name_with_detail, option.name, it) }
        ?: option.name
    val name = if (unknown) stringResource(R.string.add_unknown, named) else named
    val quantity = option.quantity?.format(LocalContext.current.resources)
    val label = quantity?.let { stringResource(R.string.list_item_with_quantity, name, it.spoken) } ?: name
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                text = AnnotatedString(label)
                role = Role.Button
                onClick {
                    onClick()
                    true
                }
            }
            .comicStyle(
                shape = MaterialTheme.shapes.medium,
                background = colors.surface,
                outline = colors.outline,
                outlineWidth = tokens.thinOutline,
                shadow = tokens.smallShadow,
            )
            .heightIn(min = tokens.minRowHeight)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_add),
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(PlusSize),
        )
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (unknown) name else name.toUpperCase(LocaleList.current),
                style = if (unknown) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                color = colors.onSurface,
            )
            if (quantity != null) QuantityBadge(quantity.visible)
        }
        Image(
            painter = painterResource(option.icon),
            contentDescription = null,
            modifier = Modifier.size(OptionIconSize),
        )
    }
}

@Composable
private fun QuantityBadge(text: String) {
    val color = MaterialTheme.colorScheme.onSurface
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = Modifier
            .border(2.dp, color, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

/** "xyzzy" → "Xyzzy", para o aviso começar com maiúscula como frase. */
private fun String.capitalized(): String = replaceFirstChar { it.titlecase() }
