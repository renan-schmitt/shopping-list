package br.com.mentealerta.listadecompras.data

import br.com.mentealerta.listadecompras.data.database.LearnedPreferenceDao
import br.com.mentealerta.listadecompras.data.database.SectionChoiceEntity
import br.com.mentealerta.listadecompras.domain.SectionChoiceRepository

/** Seções escolhidas no banco local (plano §4.3), na tabela `section_choice`. */
internal class RoomSectionChoiceRepository(private val dao: LearnedPreferenceDao) : SectionChoiceRepository {
    override suspend fun learn(key: String, sectionId: String) {
        dao.upsertSectionChoice(SectionChoiceEntity(productKey = key, sectionId = sectionId))
    }

    override suspend fun choices(): Map<String, String> =
        dao.getSectionChoices().associate { it.productKey to it.sectionId }
}
