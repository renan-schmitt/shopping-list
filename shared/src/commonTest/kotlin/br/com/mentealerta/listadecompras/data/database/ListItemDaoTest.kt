package br.com.mentealerta.listadecompras.data.database

import app.cash.turbine.test
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.domain.QuantityUnit
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class ListItemDaoTest {
    private val database = createTestDatabase()
    private val dao = database.listItemDao()

    private val milk = ListItem.create(
        name = "Leite",
        productId = "milk",
        sectionId = "dairy",
        quantity = Quantity(2.0, QuantityUnit.LITER),
        detail = "desnatado",
    )
    private val cassava = ListItem.create(name = "aipim", productId = "cassava", sectionId = "produce")
    private val unknown = ListItem.create(name = "Flocão da vó", productId = null, sectionId = "other")

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun startsEmpty() = runTest {
        dao.observeAll().test { assertEquals(emptyList(), awaitItem()) }
    }

    @Test
    fun keepsAllFieldsOfTheItem() = runTest {
        dao.upsert(milk.toEntity(at(1)))
        dao.upsert(unknown.toEntity(at(2)))

        assertEquals(milk, dao.getById(milk.id.value)?.toDomain())
        assertEquals(unknown, dao.getById(unknown.id.value)?.toDomain())
    }

    @Test
    fun listsItemsInTheOrderTheyWereAdded() = runTest {
        dao.upsert(cassava.toEntity(at(2)))
        dao.upsert(milk.toEntity(at(1)))
        dao.upsert(unknown.toEntity(at(3)))

        dao.observeAll().test {
            assertEquals(listOf(milk, cassava, unknown), awaitItem().map { it.toDomain() })
        }
    }

    @Test
    fun emitsAgainWhenTheListChanges() = runTest {
        dao.observeAll().test {
            assertEquals(emptyList(), awaitItem())

            dao.upsert(milk.toEntity(at(1)))
            assertEquals(listOf(milk), awaitItem().map { it.toDomain() })

            dao.upsert(milk.check().toEntity(at(1)))
            assertEquals(listOf(milk.check()), awaitItem().map { it.toDomain() })

            dao.deleteById(milk.id.value)
            assertEquals(emptyList(), awaitItem())
        }
    }

    @Test
    fun upsertUpdatesTheItemWithTheSameId() = runTest {
        dao.upsert(milk.toEntity(at(1)))
        val edited = milk.copy(name = "Leite integral", quantity = null, detail = null)

        dao.upsert(edited.toEntity(at(1)))

        assertEquals(edited, dao.getById(milk.id.value)?.toDomain())
    }

    @Test
    fun deleteCheckedKeepsItemsNotChecked() = runTest {
        dao.upsertAll(listOf(milk.check().toEntity(at(1)), cassava.toEntity(at(2)), unknown.check().toEntity(at(3))))

        dao.deleteChecked()

        dao.observeAll().test { assertEquals(listOf(cassava), awaitItem().map { it.toDomain() }) }
    }

    @Test
    fun itemsComeBackInTheSamePlaceAfterDeleteAll() = runTest {
        val entities = listOf(milk.toEntity(at(1)), cassava.check().toEntity(at(2)), unknown.toEntity(at(3)))
        dao.upsertAll(entities)

        dao.deleteAll()
        assertNull(dao.getById(milk.id.value))
        dao.upsertAll(entities.reversed())

        dao.observeAll().test { assertEquals(entities, awaitItem()) }
    }

    private fun at(epochSeconds: Long) = Instant.fromEpochSeconds(epochSeconds)
}
