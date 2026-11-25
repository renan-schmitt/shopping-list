package br.com.mentealerta.listadecompras.data

import br.com.mentealerta.listadecompras.data.database.createTestDatabase
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RoomPreferredTermRepositoryTest {
    private val database = createTestDatabase()
    private val repository = RoomPreferredTermRepository(database.learnedPreferenceDao())

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun returnsOnlyTheAskedProductsThatHaveATerm() = runTest {
        repository.learn("cassava", "aipim")
        repository.learn("tangerine", "bergamota")

        assertEquals(mapOf("cassava" to "aipim"), repository.preferredTerms(listOf("cassava", "milk", "cassava")))
        assertEquals(emptyMap(), repository.preferredTerms(emptyList()))
    }

    @Test
    fun keepsTheLastTerm() = runTest {
        repository.learn("cassava", "mandioca")
        repository.learn("cassava", "macaxeira")

        assertEquals(mapOf("cassava" to "macaxeira"), repository.preferredTerms(setOf("cassava")))
    }
}
