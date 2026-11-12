package br.com.mentealerta.listadecompras.data.database

import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LearnedPreferenceDaoTest {
    private val database = createTestDatabase()
    private val dao = database.learnedPreferenceDao()

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun preferredTermIsNullUntilLearned() = runTest {
        assertNull(dao.getPreferredTerm("cassava"))
    }

    @Test
    fun keepsTheLastPreferredTerm() = runTest {
        dao.upsertPreferredTerm(PreferredTermEntity(productId = "cassava", term = "mandioca"))
        dao.upsertPreferredTerm(PreferredTermEntity(productId = "cassava", term = "aipim"))
        dao.upsertPreferredTerm(PreferredTermEntity(productId = "tangerine", term = "bergamota"))

        assertEquals("aipim", dao.getPreferredTerm("cassava"))
        assertEquals("bergamota", dao.getPreferredTerm("tangerine"))
    }

    @Test
    fun sectionChoiceIsNullUntilChosen() = runTest {
        assertNull(dao.getSectionChoice("coffee"))
    }

    @Test
    fun keepsTheLastSectionChoice() = runTest {
        dao.upsertSectionChoice(SectionChoiceEntity(productKey = "coffee", sectionId = "beverages"))
        dao.upsertSectionChoice(SectionChoiceEntity(productKey = "coffee", sectionId = "grocery"))
        dao.upsertSectionChoice(SectionChoiceEntity(productKey = "flocao da vo", sectionId = "grocery"))

        assertEquals("grocery", dao.getSectionChoice("coffee"))
        assertEquals("grocery", dao.getSectionChoice("flocao da vo"))
    }
}
