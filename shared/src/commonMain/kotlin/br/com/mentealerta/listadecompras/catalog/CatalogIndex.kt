package br.com.mentealerta.listadecompras.catalog

import br.com.mentealerta.listadecompras.normalization.normalize

/**
 * Índice em memória do [Catalog], montado uma vez (plano §4.3: o catálogo cabe em memória, sem FTS).
 *
 * [key] transforma nomes de produto, termos e o texto procurado na chave de busca. Por enquanto é a
 * [normalize] (minúsculas, sem acento); as próximas etapas (plural) entram nela sem mudar o índice.
 */
class CatalogIndex(val catalog: Catalog, private val key: (String) -> String = ::normalize) {
    private val productsById = catalog.products.associateBy { it.id }
    private val sectionsById = catalog.sections.associateBy { it.id }
    private val productsBySection = catalog.products.groupBy { it.sectionId }
    private val termsByProduct = catalog.terms.groupBy { it.productId }

    /** Chave → produtos com esse nome ou termo; mais de um quando textos diferentes caem na mesma chave. */
    private val productsByKey: Map<String, List<CatalogProduct>> = buildMap<String, MutableList<CatalogProduct>> {
        catalog.products.forEach { addTo(it.name, it) }
        catalog.terms.forEach { term -> productsById[term.productId]?.let { addTo(term.term, it) } }
    }

    /** Produto pelo `id`; um `id` antigo devolve o produto que o absorveu. */
    fun product(id: String): CatalogProduct? = productsById[catalog.replacements[id] ?: id]

    fun section(id: String): CatalogSection? = sectionsById[id]

    /** Produtos da seção, na ordem dentro dela. */
    fun productsIn(sectionId: String): List<CatalogProduct> = productsBySection[sectionId].orEmpty()

    /** Sinônimos e regionalismos do produto. */
    fun termsOf(productId: String): List<CatalogTerm> = termsByProduct[productId].orEmpty()

    /** Produtos cujo nome ou termo tem a mesma chave de [text], sem repetir; quem bate pelo nome vem antes. */
    fun findByKey(text: String): List<CatalogProduct> = productsByKey[key(text)].orEmpty()

    private fun MutableMap<String, MutableList<CatalogProduct>>.addTo(text: String, product: CatalogProduct) {
        val products = getOrPut(key(text)) { mutableListOf() }
        if (product !in products) products += product
    }
}
