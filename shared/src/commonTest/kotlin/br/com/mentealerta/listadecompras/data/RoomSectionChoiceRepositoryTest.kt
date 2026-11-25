package br.com.mentealerta.listadecompras.data

import br.com.mentealerta.listadecompras.data.database.createTestDatabase
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RoomSectionChoiceRepositoryTest {
    private val database = createTestDatabase()
    private val repository = RoomSectionChoiceRepository(database.learnedPreferenceDao())

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun keepsTheLastChoiceOfEachProduct() = runTest {
        assertEquals(emptyMap(), repository.choices())

        repository.learn("coffee", "beverages")
        repository.learn("coffee", "pantry")
        repository.learn("sabao de coco", "cleaning")

        assertEquals(mapOf("coffee" to "pantry", "sabao de coco" to "cleaning"), repository.choices())
    }
}
