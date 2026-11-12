package br.com.mentealerta.listadecompras.data

import app.cash.turbine.test
import br.com.mentealerta.listadecompras.catalog.CatalogSection
import br.com.mentealerta.listadecompras.data.database.createTestDatabase
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.domain.ListSection
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.domain.QuantityUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RoomShoppingListRepositoryTest {
    private val database = createTestDatabase()
    private val catalogRepository = EmbeddedCatalogRepository()
    private val clock = StepClock()
    private val repository = RoomShoppingListRepository(database, catalogRepository, clock)

    private val milk = ListItem.create(name = "Leite", productId = "milk", sectionId = "deli_dairy")
    private val cheese = ListItem.create(name = "Queijo", productId = "cheese", sectionId = "deli_dairy")
    private val cassava = ListItem.create(name = "aipim", productId = "cassava", sectionId = "produce")
    private val soap = ListItem.create(name = "Sabão em pó", productId = null, sectionId = "cleaning")

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun startsEmpty() = runTest {
        repository.observeSections().test { assertEquals(emptyList(), awaitItem()) }
    }

    @Test
    fun groupsBySectionInTheMarketOrderAndKeepsTheOrderItemsWereAdded() = runTest {
        listOf(soap, milk, cassava, cheese).forEach { repository.add(it) }

        repository.observeSections().test {
            assertEquals(
                listOf("produce" to listOf(cassava), "deli_dairy" to listOf(milk, cheese), "cleaning" to listOf(soap)),
                awaitItem().simplified(),
            )
        }
    }

    @Test
    fun emitsAgainWhenTheListChanges() = runTest {
        repository.observeSections().test {
            assertEquals(emptyList(), awaitItem())

            repository.add(milk)
            assertEquals(listOf("deli_dairy" to listOf(milk)), awaitItem().simplified())

            repository.add(cassava)
            assertEquals(
                listOf("produce" to listOf(cassava), "deli_dairy" to listOf(milk)),
                awaitItem().simplified(),
            )
        }
    }

    @Test
    fun itemFromASectionTheCatalogDoesNotHaveGoesToOther() = runTest {
        val old = ListItem.create(name = "Fita", productId = null, sectionId = "old_section")
        val unknown = ListItem.create(name = "Flocão da vó", productId = null, sectionId = CatalogSection.OTHER_ID)
        repository.add(unknown)
        repository.add(old)

        repository.observeSections().test {
            val sections = awaitItem()
            assertEquals(listOf(CatalogSection.OTHER_ID to listOf(unknown, old)), sections.simplified())
            assertEquals(catalogRepository.catalog().sections.last(), sections.single().section)
        }
    }

    @Test
    fun checkingAndUncheckingKeepsTheItemInPlace() = runTest {
        listOf(milk, cheese).forEach { repository.add(it) }

        repository.observeSections().test {
            assertEquals(listOf(milk, cheese), awaitItem().single().items)

            assertEquals(milk, repository.setChecked(milk.id, checked = true))
            assertEquals(listOf(milk.check(), cheese), awaitItem().single().items)

            assertEquals(milk.check(), repository.setChecked(milk.id, checked = false))
            assertEquals(listOf(milk, cheese), awaitItem().single().items)
        }
    }

    @Test
    fun setCheckedReturnsNullWhenTheItemIsAlreadyThatWay() = runTest {
        repository.add(milk)

        assertNull(repository.setChecked(milk.id, checked = false))
        repository.setChecked(milk.id, checked = true)
        assertNull(repository.setChecked(milk.id, checked = true))
    }

    @Test
    fun updateChangesTheItemAndKeepsItInPlace() = runTest {
        listOf(milk, cheese).forEach { repository.add(it) }
        val edited = milk.copy(
            name = "Leite integral",
            quantity = Quantity(2.0, QuantityUnit.LITER),
            detail = "sem lactose",
        )

        assertEquals(milk, repository.update(edited))

        repository.observeSections().test { assertEquals(listOf(edited, cheese), awaitItem().single().items) }
    }

    @Test
    fun updateCanMoveTheItemToAnotherSection() = runTest {
        listOf(milk, cassava).forEach { repository.add(it) }
        val moved = milk.copy(sectionId = "pantry")

        assertEquals(milk, repository.update(moved))

        repository.observeSections().test {
            assertEquals(listOf("produce" to listOf(cassava), "pantry" to listOf(moved)), awaitItem().simplified())
        }
    }

    @Test
    fun updateAndSetCheckedReturnNullWhenTheItemIsNotOnTheList() = runTest {
        assertNull(repository.update(milk))
        assertNull(repository.setChecked(milk.id, checked = true))

        repository.observeSections().test { assertEquals(emptyList(), awaitItem()) }
    }

    @Test
    fun removeTakesTheItemOffAndReturnsIt() = runTest {
        listOf(milk, cheese).forEach { repository.add(it) }
        repository.setChecked(milk.id, checked = true)

        val removed = repository.remove(milk.id)

        assertEquals(listOf(milk.check()), removed.items)
        repository.observeSections().test { assertEquals(listOf(cheese), awaitItem().single().items) }
    }

    @Test
    fun removeOfAnItemNotOnTheListReturnsNothing() = runTest {
        assertTrue(repository.remove(ListItemId("nao-existe")).isEmpty())
    }

    @Test
    fun removedItemComesBackToTheSamePlace() = runTest {
        listOf(milk, cheese).forEach { repository.add(it) }
        val removed = repository.remove(milk.id)
        repository.add(ListItem.create(name = "Iogurte", productId = "yogurt", sectionId = "deli_dairy"))

        repository.restore(removed)

        repository.observeSections().test {
            assertEquals(listOf("Leite", "Queijo", "Iogurte"), awaitItem().single().items.map { it.name })
        }
    }

    @Test
    fun clearEmptiesTheListAndEverythingComesBackInPlace() = runTest {
        listOf(soap, milk, cassava, cheese).forEach { repository.add(it) }
        repository.setChecked(cassava.id, checked = true)
        val before = repository.observeSections().first()

        val removed = repository.clear()

        assertEquals(listOf(soap, milk, cassava.check(), cheese), removed.items)
        repository.observeSections().test {
            assertEquals(emptyList(), awaitItem())

            repository.restore(removed)
            assertEquals(before, awaitItem())
        }
    }

    @Test
    fun restoringNothingDoesNotChangeTheList() = runTest {
        repository.add(milk)

        repository.restore(repository.remove(ListItemId("nao-existe")))

        repository.observeSections().test { assertEquals(listOf(milk), awaitItem().single().items) }
    }

    private fun List<ListSection>.simplified() = map { it.section.id to it.items }
}
