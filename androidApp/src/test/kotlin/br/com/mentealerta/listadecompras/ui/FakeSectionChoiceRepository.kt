package br.com.mentealerta.listadecompras.ui

import br.com.mentealerta.listadecompras.domain.SectionChoiceRepository

/** Seções escolhidas em memória (#56), para os testes das telas. */
class FakeSectionChoiceRepository : SectionChoiceRepository {
    val sections = mutableMapOf<String, String>()

    override suspend fun learn(key: String, sectionId: String) {
        sections[key] = sectionId
    }

    override suspend fun choices(): Map<String, String> = sections.toMap()
}
