package br.com.mentealerta.listadecompras.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.ui.components.PrimaryButton
import br.com.mentealerta.listadecompras.ui.components.SecondaryButton
import br.com.mentealerta.listadecompras.ui.components.UppercaseText

/**
 * Conferência da lista colada (#68), no papel: título **Lista recebida**, "Toque na lixeira para tirar
 * o que não quer.", um produto por linha com a lixeira ([TrashRow]) e, embaixo, quantos já estão na
 * lista ("Leite já está na lista."), que não entram. Os botões ficam fora da rolagem
 * ([ReviewButtons]), para a pessoa não precisar descer até o fim de uma lista comprida.
 */
internal fun LazyListScope.reviewItems(review: ReviewUi, onRemove: (AddOptionUi) -> Unit) {
    item(key = "review-title", contentType = "title") {
        Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            UppercaseText(
                text = stringResource(R.string.review_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(R.string.review_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
    items(review.items, contentType = { "review-row" }) { option ->
        TrashRow(
            name = option.name,
            detail = option.detail,
            quantity = option.quantity,
            icon = option.icon,
            onRemove = { onRemove(option) },
        )
    }
    val already = review.alreadyOnList
    if (already.isNotEmpty()) {
        item(key = "review-already", contentType = "review-already") {
            val text = if (already.size == 1) {
                stringResource(R.string.review_already_on_list, already.single().replaceFirstChar { it.titlecase() })
            } else {
                pluralStringResource(R.plurals.review_already_on_list_many, already.size, already.size)
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
        }
    }
}

/**
 * **Pôr 5 produtos na lista** e **Deixa pra lá**, um embaixo do outro, presos no pé da tela durante
 * a conferência. A contagem acompanha o que a pessoa tira.
 */
@Composable
internal fun ReviewButtons(count: Int, onConfirm: () -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val line = colors.outline
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface)
            .drawBehind { drawLine(line, Offset(0f, 0f), Offset(size.width, 0f), 2.dp.toPx()) }
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PrimaryButton(
            text = pluralStringResource(R.plurals.review_confirm, count, count),
            onClick = onConfirm,
            icon = painterResource(R.drawable.ic_add),
            modifier = Modifier.fillMaxWidth(),
        )
        SecondaryButton(
            text = stringResource(R.string.review_dismiss),
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
