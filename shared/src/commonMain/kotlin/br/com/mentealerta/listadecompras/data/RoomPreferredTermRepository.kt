package br.com.mentealerta.listadecompras.data

import br.com.mentealerta.listadecompras.data.database.LearnedPreferenceDao
import br.com.mentealerta.listadecompras.data.database.PreferredTermEntity
import br.com.mentealerta.listadecompras.domain.PreferredTermRepository

/** Termos preferidos no banco local (plano §4.3), na tabela `preferred_term`. */
internal class RoomPreferredTermRepository(private val dao: LearnedPreferenceDao) : PreferredTermRepository {
    override suspend fun learn(productId: String, term: String) {
        dao.upsertPreferredTerm(PreferredTermEntity(productId = productId, term = term))
    }

    override suspend fun preferredTerms(productIds: Collection<String>): Map<String, String> {
        if (productIds.isEmpty()) return emptyMap()
        return dao.getPreferredTerms(productIds.distinct()).associate { it.productId to it.term }
    }
}
