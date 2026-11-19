package br.com.mentealerta.listadecompras.catalog

import br.com.mentealerta.listadecompras.normalization.searchKey

/**
 * Índice em memória do [Catalog], montado uma vez (plano §4.3: o catálogo cabe em memória, sem FTS).
 *
 * [key] transforma nomes de produto, termos e o texto procurado na chave de busca. O padrão é a
 * [searchKey] (minúsculas, sem acento, singular); novas etapas entram nela sem mudar o índice.
 */
class CatalogIndex(val catalog: Catalog, private val key: (String) -> String = ::searchKey) {
    private val productsById = catalog.products.associateBy { it.id }
    private val sectionsById = catalog.sections.associateBy { it.id }
    private val productsBySection = catalog.products.groupBy { it.sectionId }
    private val termsByProduct = catalog.terms.groupBy { it.productId }

    /**
     * Chave → produtos com esse nome ou termo, um por produto; mais de um quando textos diferentes
     * caem na mesma chave. Quem bate pelo nome vem antes.
     */
    private val matchesByKey: Map<String, List<ProductMatch>> = buildMap<String, MutableList<ProductMatch>> {
        catalog.products.forEach { addTo(it.name, ProductMatch(it, term = null)) }
        catalog.terms.forEach { term -> productsById[term.productId]?.let { addTo(term.term, ProductMatch(it, term)) } }
    }

    /** Produto pelo `id`; um `id` antigo devolve o produto que o absorveu. */
    fun product(id: String): CatalogProduct? = productsById[catalog.replacements[id] ?: id]

    fun section(id: String): CatalogSection? = sectionsById[id]

    /** Produtos da seção, na ordem dentro dela. */
    fun productsIn(sectionId: String): List<CatalogProduct> = productsBySection[sectionId].orEmpty()

    /** Sinônimos e regionalismos do produto. */
    fun termsOf(productId: String): List<CatalogTerm> = termsByProduct[productId].orEmpty()

    /** Produtos cujo nome ou termo tem a mesma chave de [text], sem repetir; quem bate pelo nome vem antes. */
    fun findByKey(text: String): List<CatalogProduct> = matchesByKey[key(text)].orEmpty().map { it.product }

    /**
     * Produto canônico de [text] pelo nome ou por sinônimo/regionalismo ("Macaxeiras" → Mandioca, pelo
     * termo "macaxeira"); `null` quando o catálogo não conhece o texto. Se a chave cair em mais de um
     * produto, vale o primeiro de [findByKey].
     */
    fun resolve(text: String): ProductMatch? = matchesByKey[key(text)]?.firstOrNull()

    private val catalogSearch by lazy { CatalogSearch(catalog.products, catalog.terms, productsById, key) }

    /**
     * Busca enquanto a pessoa digita, tolerando texto incompleto e erro de digitação ("maca" → Maçã,
     * "fejao" → Feijão). Ordem: da batida exata à aproximada ([MatchKind]), depois menos erros e
     * depois os mais comprados, por [purchaseCounts] (`id` do produto → quantas vezes foi comprado).
     * Cada produto aparece uma vez; texto vazio não acha nada.
     */
    fun search(
        text: String,
        purchaseCounts: Map<String, Int> = emptyMap(),
        limit: Int = DEFAULT_SEARCH_LIMIT,
    ): List<SearchResult> = catalogSearch.search(text, purchaseCounts, limit)

    private fun MutableMap<String, MutableList<ProductMatch>>.addTo(text: String, match: ProductMatch) {
        val matches = getOrPut(key(text)) { mutableListOf() }
        if (matches.none { it.product == match.product }) matches += match
    }

    companion object {
        const val DEFAULT_SEARCH_LIMIT = 20
    }
}

/** Produto do catálogo achado por um texto e por onde ele bateu. */
data class ProductMatch(
    val product: CatalogProduct,
    /** Sinônimo ou regionalismo que bateu; `null` quando bateu pelo nome do produto. */
    val term: CatalogTerm?,
)
