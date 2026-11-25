package br.com.mentealerta.listadecompras.ui

import br.com.mentealerta.listadecompras.domain.PreferredTermRepository

/** Termos preferidos em memória (#44), para os testes das telas. */
class FakePreferredTermRepository : PreferredTermRepository {
    val terms = mutableMapOf<String, String>()

    override suspend fun learn(productId: String, term: String) {
        terms[productId] = term
    }

    override suspend fun preferredTerms(productIds: Collection<String>): Map<String, String> =
        terms.filterKeys { it in productIds }
}
