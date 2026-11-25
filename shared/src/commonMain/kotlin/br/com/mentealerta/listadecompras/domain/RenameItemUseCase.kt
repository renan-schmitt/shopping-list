package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.normalization.ItemTextParser
import br.com.mentealerta.listadecompras.normalization.ParsedItem

/**
 * Troca o nome do item na edição (#55), sem gravar: quem grava é o [EditItemUseCase].
 *
 * O nome fica como a pessoa escreveu. Se o começo dele é outro produto do catálogo ("leite" → "pão
 * francês"), o item passa a apontar para esse produto, com o ícone e a seção dele. Se é o mesmo
 * produto ou um que o app não conhece, produto e seção ficam como estavam (a seção pode ter sido
 * escolhida pela pessoa, #56). Nome em branco não muda nada.
 */
class RenameItemUseCase(private val parser: ItemTextParser) {
    operator fun invoke(item: ListItem, name: String): ListItem {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || trimmed == item.name) return item
        val product = parser.parse(trimmed)?.match?.product
        return if (product == null || product.id == item.productId) {
            item.copy(name = trimmed)
        } else {
            item.copy(name = trimmed, productId = product.id, sectionId = product.sectionId)
        }
    }

    /**
     * O mesmo, para um produto da conferência (#136), que ainda não está na lista: outro produto
     * conhecido troca o produto; o mesmo ou um desconhecido mantém o que o leitor tinha entendido.
     */
    operator fun invoke(item: ParsedItem, name: String): ParsedItem {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || trimmed == item.name) return item
        val match = parser.parse(trimmed)?.match
        return if (match == null || match.product.id == item.match?.product?.id) {
            item.copy(name = trimmed)
        } else {
            item.copy(name = trimmed, match = match)
        }
    }
}
