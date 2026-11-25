package br.com.mentealerta.listadecompras.data

import br.com.mentealerta.listadecompras.data.database.LearnedPreferenceDao
import br.com.mentealerta.listadecompras.data.database.SuggestionDismissalEntity
import br.com.mentealerta.listadecompras.domain.SuggestionDismissalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Instant

/** O "Não" das sugestões no banco local (plano §4.3), na tabela `suggestion_dismissal`. */
internal class RoomSuggestionDismissalRepository(private val dao: LearnedPreferenceDao) :
    SuggestionDismissalRepository {
    override fun observeDismissals(): Flow<Map<String, Instant>> =
        dao.observeSuggestionDismissals().map { all -> all.associate { it.productKey to it.dismissedAt } }

    override suspend fun dismiss(key: String, at: Instant) {
        dao.upsertSuggestionDismissal(SuggestionDismissalEntity(productKey = key, dismissedAt = at))
    }
}
