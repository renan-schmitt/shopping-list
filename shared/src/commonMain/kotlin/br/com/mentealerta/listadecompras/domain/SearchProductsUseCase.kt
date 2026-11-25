package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.MatchKind
import br.com.mentealerta.listadecompras.catalog.ProductMatch
import br.com.mentealerta.listadecompras.normalization.ItemTextParser
import br.com.mentealerta.listadecompras.normalization.ParsedItem

/** Opções da tela de adicionar para o texto digitado, prontas para o [AddItemsUseCase]. */
data class ProductOptions(
    /** Produtos do catálogo, do mais provável ao menos provável. */
    val found: List<ParsedItem>,
    /** O texto como a pessoa escreveu, sem produto do catálogo (#49); `null` quando o texto já é um produto. */
    val unknown: ParsedItem?,
    /**
     * O [unknown] vem antes do [found] (2026-11-23): nenhum nome do catálogo começa com o que foi
     * digitado, só há batidas aproximadas ("nescau" → Nescafé); o mais provável é a pessoa querer o
     * texto como está.
     */
    val unknownFirst: Boolean = false,
) {
    fun isEmpty(): Boolean = found.isEmpty() && unknown == null

    /** A opção que entra na lista pela tecla "pronto" do teclado: a que aparece primeiro. */
    fun first(): ParsedItem? = if (unknownFirst) unknown else found.firstOrNull() ?: unknown

    companion object {
        val NONE = ProductOptions(found = emptyList(), unknown = null)
    }
}

/**
 * Busca enquanto a pessoa digita (#53, plano §1.2). O texto passa pelo [ItemTextParser], então
 * "2 litros de leite" já vem com a quantidade, que vale para todas as opções.
 *
 * Ordem das opções:
 * 1. o produto que o texto já nomeia, com o detalhe ("café da marca X");
 * 2. a busca aproximada no catálogo ("maca" → Maçã), sem repetir produto;
 * 3. o produto desconhecido, com o texto todo como nome, quando o texto não é exatamente um nome
 *    ou sinônimo do catálogo. Ele sobe para o topo quando só há batidas aproximadas
 *    ("nescau" → **Pôr “nescau” na lista** antes de Nescafé; "lei" → Leite continua primeiro).
 *
 * Produto do catálogo leva o nome ou o sinônimo do catálogo que bateu, com acento e no singular:
 * "maca" → Maçã, "macaxeiras" → macaxeira (regionalismo de Mandioca). Nas opções da busca
 * aproximada, o termo que a pessoa costuma usar ([PreferredTermRepository], plano §1.3) vem no
 * lugar: quem já pôs "cacetinho" na lista vê "cacetinho" ao digitar "pão". O produto que o texto
 * já nomeia fica com o termo digitado.
 */
class SearchProductsUseCase(
    private val index: CatalogIndex,
    private val parser: ItemTextParser,
    private val preferredTermRepository: PreferredTermRepository,
) {
    suspend operator fun invoke(text: String): ProductOptions {
        val parsed = parser.parse(text) ?: return ProductOptions.NONE
        // O texto depois da quantidade: o nome e o detalhe, ou o nome todo do desconhecido.
        val typed = listOfNotNull(parsed.name, parsed.detail).joinToString(" ")

        val results = index.search(typed, limit = MAX_RESULTS)
        val preferredTerms = preferredTermRepository.preferredTerms(results.map { it.product.id })
        val found = buildList {
            parsed.match?.let { add(parsed.copy(name = it.catalogName())) }
            results.forEach { result ->
                val match = ProductMatch(result.product, result.term)
                if (none { it.match?.product?.id == result.product.id }) {
                    val name = preferredTerms[result.product.id] ?: match.catalogName()
                    add(ParsedItem(name, match, parsed.quantity, detail = null))
                }
            }
        }.take(MAX_RESULTS)

        val isExactProduct = parsed.match != null && parsed.detail == null
        val unknown = if (isExactProduct) null else ParsedItem(typed, match = null, parsed.quantity, detail = null)
        // Com produto no texto ("café da marca X"), ele continua primeiro.
        val unknownFirst = unknown != null && parsed.match == null && results.all { it.kind == MatchKind.APPROX }
        return ProductOptions(found, unknown, unknownFirst)
    }

    private fun ProductMatch.catalogName(): String = term?.term ?: product.name

    private companion object {
        /** Botões grandes: mais que isso vira rolagem demais (a pessoa refina digitando). */
        const val MAX_RESULTS = 10
    }
}
