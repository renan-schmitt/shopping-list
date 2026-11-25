package br.com.mentealerta.listadecompras.ui

import br.com.mentealerta.listadecompras.domain.SuggestionDismissalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Instant

/** O "Não" das sugestões em memória (#64), para os testes das telas. */
class FakeSuggestionDismissalRepository : SuggestionDismissalRepository {
    private val state = MutableStateFlow(emptyMap<String, Instant>())

    val dismissals: Map<String, Instant> get() = state.value

    override fun observeDismissals(): Flow<Map<String, Instant>> = state

    override suspend fun dismiss(key: String, at: Instant) {
        state.update { it + (key to at) }
    }
}
