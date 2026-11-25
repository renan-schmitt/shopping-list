package br.com.mentealerta.listadecompras.domain

/** Seções escolhidas em memória, para os testes que não precisam do banco. */
class InMemorySectionChoiceRepository(initial: Map<String, String> = emptyMap()) : SectionChoiceRepository {
    val sections = initial.toMutableMap()

    override suspend fun learn(key: String, sectionId: String) {
        sections[key] = sectionId
    }

    override suspend fun choices(): Map<String, String> = sections.toMap()
}
