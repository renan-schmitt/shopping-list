package br.com.mentealerta.listadecompras.ui.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.ui.components.BackToListButton
import br.com.mentealerta.listadecompras.ui.components.SecondaryButton
import br.com.mentealerta.listadecompras.ui.components.SectionTitle
import br.com.mentealerta.listadecompras.ui.components.comicStyle
import br.com.mentealerta.listadecompras.ui.theme.ComicTheme

private val PencilSize = 30.dp
private val ProductIconSize = 48.dp

/**
 * Modo "Mudar a lista" (#121, plano §1.1): por cima da lista, como as telas de adicionar e editar.
 * Topo verde com "Voltar para a lista"; embaixo, "Toque no produto que quer mudar.", os produtos por
 * seção (com "Já peguei" no fim), cada um numa caixa com lápis, e por último **Jogar a lista fora**
 * (#58). Tocar num produto abre a edição ([onEdit]); aqui tocar nunca risca, para não haver dois
 * comportamentos no mesmo toque.
 */
@Composable
fun ChangeListContent(
    uiState: ListUiState,
    modifier: Modifier = Modifier,
    onEdit: (ListItemId) -> Unit = {},
    onClear: () -> Unit = {},
    onClose: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    Scaffold(
        modifier = modifier,
        containerColor = colors.surface,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.primary)
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
            ) {
                BackToListButton(onClick = onClose)
            }
        },
    ) { padding ->
        val sections = (uiState as? ListUiState.Content)?.sections.orEmpty()
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "hint", contentType = "hint") {
                Text(
                    text = stringResource(R.string.list_change_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface,
                )
            }
            sections.forEach { section ->
                item(key = "section-${section.id}", contentType = "section") {
                    SectionTitle(text = section.title(), modifier = Modifier.padding(top = 12.dp))
                }
                items(section.rows, key = { it.id.value }, contentType = { "row" }) { row ->
                    ChangeRow(row = row, onClick = { onEdit(row.id) })
                }
            }
            item(key = "clear", contentType = "clear") {
                SecondaryButton(
                    text = stringResource(R.string.list_clear),
                    onClick = onClear,
                    // O mesmo cesto de lixo do produto "Lixeira" do catálogo, colorido.
                    icon = painterResource(R.drawable.catalog_fluent_wastebasket),
                    tintIcon = false,
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                )
            }
        }
    }
}

/**
 * Produto no modo de mudar: lápis → nome (com detalhe e quantidade) → ícone, numa caixa com
 * contorno, como as opções da busca. Riscado continua riscado. Para o TalkBack é um botão só, com a
 * ação "Mudar".
 */
@Composable
private fun ChangeRow(row: ListRowUi, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val tokens = ComicTheme.tokens
    val name = row.detail?.let { stringResource(R.string.list_item_name_with_detail, row.name, it) } ?: row.name
    val quantity = row.quantity?.format(LocalContext.current.resources)
    val label = quantity?.let { stringResource(R.string.list_item_with_quantity, name, it.spoken) } ?: name
    val checkedDescription = stringResource(R.string.list_item_checked)
    val actionLabel = stringResource(R.string.list_item_change)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                text = AnnotatedString(label)
                if (row.checked) stateDescription = checkedDescription
                role = Role.Button
                onClick(label = actionLabel) {
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
                shadowColor = ComicTheme.colors.shadow,
            )
            .heightIn(min = tokens.minRowHeight)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_edit),
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(PencilSize),
        )
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            ItemName(text = name, checked = row.checked)
            if (quantity != null) QuantityBadge(text = quantity.visible, checked = row.checked)
        }
        Image(
            painter = painterResource(row.icon),
            contentDescription = null,
            modifier = Modifier.size(ProductIconSize),
        )
    }
}
