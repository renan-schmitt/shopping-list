package br.com.mentealerta.listadecompras.ui.history

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.ui.components.BackToListButton
import br.com.mentealerta.listadecompras.ui.components.SectionTitle
import br.com.mentealerta.listadecompras.ui.components.UppercaseText
import br.com.mentealerta.listadecompras.ui.list.ItemName
import br.com.mentealerta.listadecompras.ui.list.QuantityBadge
import br.com.mentealerta.listadecompras.ui.list.format
import org.koin.compose.viewmodel.koinViewModel

private val ProductIconSize = 48.dp
private val RowMinHeight = 72.dp

/**
 * "O que já comprei" (#66, plano §1.5): abre por cima da lista, como o modo de mudar. Topo verde com
 * "Voltar para a lista"; embaixo, o título e as compras da mais recente para a mais antiga, cada
 * uma com a data por extenso na faixa ("Ontem, 8 produtos") e os produtos. Só para ver: tocar não
 * faz nada.
 */
@Composable
fun HistoryScreen(onClose: () -> Unit, modifier: Modifier = Modifier, viewModel: HistoryViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BackHandler(onBack = onClose)
    HistoryContent(uiState = uiState, onClose = onClose, modifier = modifier)
}

@Composable
fun HistoryContent(uiState: HistoryUiState, modifier: Modifier = Modifier, onClose: () -> Unit = {}) {
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        ) {
            item(key = "title", contentType = "title") {
                UppercaseText(
                    text = stringResource(R.string.history_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onSurface,
                    modifier = Modifier.fillMaxWidth().semantics { heading() },
                )
            }
            when (uiState) {
                HistoryUiState.Loading -> Unit
                HistoryUiState.Empty -> item(key = "empty", contentType = "empty") { EmptyHistory() }
                is HistoryUiState.Content -> uiState.purchases.forEach { purchase ->
                    item(key = "purchase-${purchase.id}", contentType = "purchase") {
                        SectionTitle(
                            text = purchaseTitle(purchase),
                            modifier = Modifier.padding(top = 24.dp, bottom = 6.dp),
                        )
                    }
                    itemsIndexed(
                        purchase.rows,
                        key = { position, _ -> "${purchase.id}-$position" },
                        contentType = { _, _ -> "row" },
                    ) { _, row ->
                        PurchasedRow(row)
                    }
                }
            }
        }
    }
}

/** "Hoje, 3 produtos", "Ontem, 1 produto", "Há 12 dias, 8 produtos". */
@Composable
private fun purchaseTitle(purchase: PurchaseUi): String = stringResource(
    R.string.history_purchase_title,
    daysAgoText(purchase.daysAgo),
    pluralStringResource(R.plurals.history_product_count, purchase.rows.size, purchase.rows.size),
)

/** Data por extenso (guia de linguagem, princípio 7). */
@Composable
internal fun daysAgoText(daysAgo: Int): String = when (daysAgo) {
    0 -> stringResource(R.string.history_today)
    1 -> stringResource(R.string.history_yesterday)
    else -> pluralStringResource(R.plurals.history_days_ago, daysAgo, daysAgo)
}

/**
 * Produto comprado: nome (com a quantidade) → ícone. Para o TalkBack, um item só ("Leite, 2
 * litros"). Nome longo quebra a linha.
 */
@Composable
private fun PurchasedRow(row: PurchasedRowUi) {
    val quantity = row.quantity?.format(LocalContext.current.resources)
    val label = quantity?.let { stringResource(R.string.list_item_with_quantity, row.name, it.spoken) } ?: row.name
    // A linha e o traço embaixo: um item só para o TalkBack.
    Column(Modifier.fillMaxWidth().clearAndSetSemantics { text = AnnotatedString(label) }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = RowMinHeight)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FlowRow(
                modifier = Modifier.weight(1f).padding(start = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                ItemName(text = row.name, checked = false)
                if (quantity != null) QuantityBadge(text = quantity.visible, checked = false)
            }
            Image(
                painter = painterResource(row.icon),
                contentDescription = null,
                modifier = Modifier.size(ProductIconSize),
            )
        }
        HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/** "Aqui aparecem as compras de quando você toca em **Terminei**.", com o destaque na cor verde. */
@Composable
private fun EmptyHistory() {
    val html = AnnotatedString.fromHtml(stringResource(R.string.history_empty))
    val accent = SpanStyle(color = MaterialTheme.colorScheme.primary)
    val text = buildAnnotatedString {
        append(html.text)
        html.spanStyles.forEach { addStyle(accent, it.start, it.end) }
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
    )
}
