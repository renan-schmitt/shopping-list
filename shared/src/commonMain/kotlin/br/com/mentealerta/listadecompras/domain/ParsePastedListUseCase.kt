package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.normalization.ItemTextParser
import br.com.mentealerta.listadecompras.normalization.ParsedItem
import br.com.mentealerta.listadecompras.normalization.cleanPastedList
import br.com.mentealerta.listadecompras.normalization.searchKey

/**
 * Transforma uma lista recebida em texto (WhatsApp, #68, plano §1.6) em produtos, na ordem em que
 * vieram. O texto passa pelo [cleanPastedList] (marcadores, títulos, nomes de seção, quantidade
 * depois do produto) e cada trecho pelo [ItemTextParser.parseAll], o mesmo da voz.
 *
 * O mesmo produto escrito duas vezes entra uma vez só, com a primeira quantidade.
 */
class ParsePastedListUseCase(private val parser: ItemTextParser, index: CatalogIndex) {
    private val headings: Set<String> = (index.catalog.sections.map { it.name } + TITLES).map(::searchKey).toSet()

    operator fun invoke(text: String): List<ParsedItem> = cleanPastedList(text, headings)
        .flatMap(parser::parseAll)
        .distinctBy { item ->
            // Como o FindSameItemUseCase: produto do catálogo pelo id; desconhecido, pelo nome.
            val product = item.match?.product?.id ?: "?" + searchKey(item.name)
            product to item.detail?.let(::searchKey)
        }

    private companion object {
        /** Títulos comuns de lista, que não são produto. */
        val TITLES = listOf(
            "Lista",
            "Lista de compras",
            "Lista de compra",
            "Lista do mercado",
            "Lista de mercado",
            "Lista da feira",
            "Lista do supermercado",
            "Compras",
            "Compras do mês",
            "Mercado",
            "Supermercado",
            "Feira",
        )
    }
}
