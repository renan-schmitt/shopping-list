package br.com.mentealerta.listadecompras.ui.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.ui.components.comicStyle
import br.com.mentealerta.listadecompras.ui.theme.ComicTheme

/** Altura mínima da linha (plano §1.7); cresce se o nome quebrar a linha. */
private val RowMinHeight = 72.dp
private val MarkSize = 44.dp
private val IconSize = 48.dp
private val StrikeWidth = 4.dp
private val DividerWidth = 2.dp
private val DividerDash = 8.dp

/** Ícone de produto riscado fica mais apagado, como no protótipo. */
private const val CHECKED_ICON_ALPHA = 0.5f

/**
 * Linha do produto (plano §1.1 e §4.8): marca de riscar → nome com detalhe e quantidade → ícone.
 *
 * Riscado = marca verde com ✓ + traço grosso por cima do nome + texto cinza; o estado nunca fica só
 * na cor. Para o TalkBack a linha é um item só, lido em caixa normal ("Leite desnatado, 2 litros"),
 * com o estado "Comprado" quando riscada.
 */
@Composable
fun ListItemRow(row: ListRowUi, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val name = row.detail?.let { stringResource(R.string.list_item_name_with_detail, row.name, it) } ?: row.name
    val quantity = row.quantity?.format(LocalContext.current.resources)
    val label = quantity?.let { stringResource(R.string.list_item_with_quantity, name, it.spoken) } ?: name
    val checkedDescription = stringResource(R.string.list_item_checked)
    val dividerColor = colors.outlineVariant

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = RowMinHeight)
            .drawBehind {
                val y = size.height - DividerWidth.toPx() / 2
                val dash = DividerDash.toPx()
                drawLine(
                    color = dividerColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = DividerWidth.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash / 2)),
                )
            }
            .clearAndSetSemantics {
                text = AnnotatedString(label)
                if (row.checked) stateDescription = checkedDescription
            }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CheckMark(checked = row.checked)
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
            modifier = Modifier
                .size(IconSize)
                .alpha(if (row.checked) CHECKED_ICON_ALPHA else 1f),
        )
    }
}

/** Marca redonda: vazia antes de riscar; verde com ✓ e sombra depois. */
@Composable
private fun CheckMark(checked: Boolean) {
    val colors = MaterialTheme.colorScheme
    val tokens = ComicTheme.tokens
    val shadow = if (checked) tokens.smallShadow else 0.dp
    Box(
        // A sombra ocupa espaço no layout; a marca sem sombra reserva o mesmo, para não pular.
        modifier = Modifier
            .padding(end = tokens.smallShadow - shadow, bottom = tokens.smallShadow - shadow)
            .comicStyle(
                shape = CircleShape,
                background = if (checked) colors.primary else colors.surface,
                outline = colors.outline,
                outlineWidth = tokens.thinOutline,
                shadow = shadow,
            )
            .size(MarkSize),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(painter = painterResource(R.drawable.ic_check), contentDescription = null, tint = colors.onPrimary)
        }
    }
}

/**
 * Nome em caixa alta (plano §4.8; o TalkBack recebe o texto original pela linha). Nome longo quebra
 * a linha, sem reticências. Riscado: traço grosso na cor da tinta por cima de cada linha do texto.
 */
@Composable
private fun ItemName(text: String, checked: Boolean) {
    val colors = MaterialTheme.colorScheme
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val strikeColor = colors.outline
    Text(
        text = text.toUpperCase(LocaleList.current),
        style = MaterialTheme.typography.bodyLarge,
        color = if (checked) colors.onSurfaceVariant else colors.onSurface,
        onTextLayout = { layout = it },
        modifier = if (checked) Modifier.strikeThrough({ layout }, strikeColor) else Modifier,
    )
}

/** Lê o layout na hora de desenhar, sem recompor a cada medida do texto. */
private fun Modifier.strikeThrough(layout: () -> TextLayoutResult?, color: Color): Modifier = drawWithContent {
    drawContent()
    val result = layout() ?: return@drawWithContent
    for (line in 0 until result.lineCount) {
        val y = (result.getLineTop(line) + result.getLineBottom(line)) / 2
        drawLine(
            color = color,
            start = Offset(result.getLineLeft(line), y),
            end = Offset(result.getLineRight(line), y),
            strokeWidth = StrikeWidth.toPx(),
        )
    }
}

/** Quantidade em letra menor, num selo com contorno, só quando a pessoa informou. */
@Composable
private fun QuantityBadge(text: String, checked: Boolean) {
    val colors = MaterialTheme.colorScheme
    val color = if (checked) colors.onSurfaceVariant else colors.onSurface
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = Modifier
            .border(2.dp, color, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}
