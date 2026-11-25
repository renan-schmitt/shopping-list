package br.com.mentealerta.listadecompras.domain

/** Termos preferidos em memória, para os testes que não precisam do banco. */
class InMemoryPreferredTermRepository(initial: Map<String, String> = emptyMap()) : PreferredTermRepository {
    val terms = initial.toMutableMap()

    override suspend fun learn(productId: String, term: String) {
        terms[productId] = term
    }

    override suspend fun preferredTerms(productIds: Collection<String>): Map<String, String> =
        terms.filterKeys { it in productIds }
}
