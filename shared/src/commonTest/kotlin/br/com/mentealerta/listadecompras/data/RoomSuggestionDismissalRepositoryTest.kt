package br.com.mentealerta.listadecompras.data

import app.cash.turbine.test
import br.com.mentealerta.listadecompras.data.database.createTestDatabase
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class RoomSuggestionDismissalRepositoryTest {
    private val database = createTestDatabase()
    private val repository = RoomSuggestionDismissalRepository(database.learnedPreferenceDao())

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun keepsTheLastNoOfEachProduct() = runTest {
        val first = Instant.parse("2026-11-10T12:00:00Z")
        val second = Instant.parse("2026-11-20T12:00:00Z")

        repository.observeDismissals().test {
            assertEquals(emptyMap(), awaitItem())

            repository.dismiss("coffee", first)
            assertEquals(mapOf("coffee" to first), awaitItem())

            repository.dismiss("coffee", second)
            assertEquals(mapOf("coffee" to second), awaitItem())

            repository.dismiss("sabao de coco", first)
            assertEquals(mapOf("coffee" to second, "sabao de coco" to first), awaitItem())
        }
    }
}
