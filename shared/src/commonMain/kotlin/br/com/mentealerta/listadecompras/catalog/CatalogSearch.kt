package br.com.mentealerta.listadecompras.catalog

import br.com.mentealerta.listadecompras.normalization.editDistance

/** Produto achado pela busca, por onde bateu e quão forte foi a batida. */
data class SearchResult(
    val product: CatalogProduct,
    /** Sinônimo ou regionalismo que bateu; `null` quando bateu pelo nome do produto. */
    val term: CatalogTerm?,
    val kind: MatchKind,
)

/** Como o texto procurado bateu, da batida mais forte para a mais fraca (é a ordem do resultado). */
enum class MatchKind {
    /** Mesma chave: "macas" → Maçã. */
    EXACT,

    /** A chave começa com o texto: "arr" → Arroz. */
    PREFIX,

    /** Outra palavra da chave começa com o texto: "moid" → Carne moída. */
    WORD_PREFIX,

    /** Com erro de digitação: "fejao" → Feijão, "macax" → Mandioca (pelo termo "macaxeira"). */
    APPROX,
}

/**
 * Busca aproximada no catálogo (plano §1.2 e §4.3), sobre as chaves de nomes e termos.
 *
 * Ordem do resultado: tipo da batida ([MatchKind]) → menos erros → mais comprados → nome antes de
 * termo → chave mais curta → ordem do catálogo. Cada produto aparece uma vez, pela melhor batida.
 */
internal class CatalogSearch(
    products: List<CatalogProduct>,
    terms: List<CatalogTerm>,
    productsById: Map<String, CatalogProduct>,
    private val key: (String) -> String,
) {
    private val entries: List<Entry> = buildList {
        products.forEach { add(Entry(it, term = null, key(it.name))) }
        terms.forEach { term -> productsById[term.productId]?.let { add(Entry(it, term, key(term.term))) } }
    }.filter { it.key.isNotEmpty() }

    private val catalogOrder: Map<String, Int> = products.withIndex().associate { (i, product) -> product.id to i }

    fun search(text: String, purchaseCounts: Map<String, Int>, limit: Int): List<SearchResult> {
        val typed = key(text)
        if (typed.isEmpty() || limit <= 0) return emptyList()
        val maxErrors = maxErrorsFor(typed)

        val best = HashMap<String, Hit>()
        entries.forEach { entry ->
            val hit = entry.match(typed, maxErrors) ?: return@forEach
            val current = best[entry.product.id]
            if (current == null || hit.rank() < current.rank()) best[entry.product.id] = hit
        }
        return best.values
            .sortedWith(
                compareBy<Hit> { it.kind }
                    .thenBy { it.errors }
                    .thenByDescending { purchaseCounts[it.entry.product.id] ?: 0 }
                    .thenBy { it.entry.term != null }
                    .thenBy { it.entry.key.length }
                    .thenBy { catalogOrder[it.entry.product.id] ?: Int.MAX_VALUE },
            )
            .take(limit)
            .map { SearchResult(it.entry.product, it.entry.term, it.kind) }
    }

    private class Entry(val product: CatalogProduct, val term: CatalogTerm?, val key: String) {
        /** Onde começa cada palavra da chave depois da primeira. */
        val laterWordStarts: List<Int> = key.indices.filter { it > 0 && key[it - 1] in WORD_SEPARATORS }

        fun match(typed: String, maxErrors: Int): Hit? = when {
            key == typed -> Hit(this, MatchKind.EXACT, errors = 0)
            key.startsWith(typed) -> Hit(this, MatchKind.PREFIX, errors = 0)
            laterWordStarts.any { key.startsWith(typed, it) } -> Hit(this, MatchKind.WORD_PREFIX, errors = 0)
            maxErrors == 0 -> null
            else -> approxErrors(typed, maxErrors).takeIf { it <= maxErrors }?.let { Hit(this, MatchKind.APPROX, it) }
        }

        private fun approxErrors(typed: String, maxErrors: Int): Int {
            var errors = editDistance(typed, key, maxErrors, prefix = true)
            for (start in laterWordStarts) {
                if (errors == 1) break
                errors = minOf(errors, editDistance(typed, key, maxErrors, prefix = true, targetStart = start))
            }
            return errors
        }
    }

    private class Hit(val entry: Entry, val kind: MatchKind, val errors: Int) {
        /** Para escolher a melhor batida de um produto: tipo, erros, nome antes de termo. */
        fun rank(): Int = (kind.ordinal * RANK_STEP + errors) * 2 + if (entry.term == null) 0 else 1
    }

    private companion object {
        val WORD_SEPARATORS = charArrayOf(' ', '-')
        const val RANK_STEP = 10

        /** A partir deste tamanho aceita 1 erro; a partir de [TWO_ERRORS_LENGTH], 2. */
        const val ONE_ERROR_LENGTH = 3
        const val TWO_ERRORS_LENGTH = 6

        /** Texto curto não aceita erro (senão "pa" acharia meio catálogo); depois 1 erro e, a partir de 6 letras, 2. */
        fun maxErrorsFor(typed: String): Int = when {
            typed.length < ONE_ERROR_LENGTH -> 0
            typed.length < TWO_ERRORS_LENGTH -> 1
            else -> 2
        }
    }
}
