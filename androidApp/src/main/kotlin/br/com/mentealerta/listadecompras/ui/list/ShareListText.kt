package br.com.mentealerta.listadecompras.ui.list

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import br.com.mentealerta.listadecompras.R
import java.util.Locale

/**
 * Texto do "Mandar a lista" (#67, plano §1.6): só o que falta comprar, agrupado por seção na ordem
 * do mercado, com o negrito do WhatsApp (`*`) no título e nas seções. Os riscados ("Já peguei") ficam
 * de fora. A quantidade vem entre parênteses, como aparece na lista:
 *
 * ```
 * *Lista de compras*
 *
 * *Frios e laticínios*
 * - Leite desnatado (2 litros)
 * - Queijo
 * ```
 */
fun shareListText(
    sections: List<SectionUi>,
    resources: Resources,
    locale: Locale = resources.configuration.locales[0],
): String = buildString {
    append(resources.getString(R.string.share_list_title))
    sections.filterNot { it.bought }.forEach { section ->
        val rows = section.rows.filterNot { it.checked }
        if (rows.isEmpty()) return@forEach
        append("\n\n")
        append(resources.getString(R.string.share_list_section, section.name))
        rows.forEach { row ->
            append('\n')
            append(row.shareLine(resources, locale))
        }
    }
}

private fun ListRowUi.shareLine(resources: Resources, locale: Locale): String {
    val withDetail = detail?.let { resources.getString(R.string.list_item_name_with_detail, name, it) } ?: name
    val fullName = withDetail.replaceFirstChar { it.titlecase(locale) }
    val quantity = quantity?.format(resources, locale)?.visible
        ?: return resources.getString(R.string.share_list_item, fullName)
    return resources.getString(R.string.share_list_item_with_quantity, fullName, quantity)
}

/**
 * Abre a tela de compartilhar do Android com o texto da lista ([shareListText]): a pessoa escolhe o
 * WhatsApp ou outro app. Sem produtos, não faz nada.
 */
fun Context.shareList(state: ListUiState) {
    val content = state as? ListUiState.Content ?: return
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareListText(content.sections, resources))
    }
    startActivity(Intent.createChooser(send, null))
}
