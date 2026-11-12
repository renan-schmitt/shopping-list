package br.com.mentealerta.listadecompras.data.database

import app.cash.turbine.test
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.Purchase
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.domain.QuantityUnit
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant

class PurchaseDaoTest {
    private val database = createTestDatabase()
    private val dao = database.purchaseDao()

    private val firstPurchase = Purchase.create(
        date = Instant.parse("2026-11-01T10:15:30.123Z"),
        items = listOf(
            ListItem.create(name = "café", productId = "coffee", sectionId = "grocery"),
            ListItem.create(
                name = "Ovos",
                productId = "egg",
                sectionId = "dairy",
                quantity = Quantity(1.0, QuantityUnit.DOZEN),
            ),
            ListItem.create(name = "Flocão da vó", productId = null, sectionId = "other"),
        ),
    )
    private val secondPurchase = Purchase.create(
        date = Instant.parse("2026-11-08T09:00:00Z"),
        items = listOf(ListItem.create(name = "café", productId = "coffee", sectionId = "grocery")),
    )

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun keepsThePurchaseWithItsItemsInOrder() = runTest {
        insert(firstPurchase)

        dao.observeAll().test { assertEquals(listOf(firstPurchase), awaitItem().map { it.toDomain() }) }
    }

    @Test
    fun listsTheMostRecentPurchaseFirst() = runTest {
        insert(firstPurchase)
        insert(secondPurchase)

        dao.observeAll().test {
            assertEquals(listOf(secondPurchase, firstPurchase), awaitItem().map { it.toDomain() })
        }
    }

    @Test
    fun deletingThePurchaseDeletesItsItems() = runTest {
        insert(firstPurchase)
        insert(secondPurchase)

        dao.deleteById(firstPurchase.id.value)

        dao.observeAll().test { assertEquals(listOf(secondPurchase), awaitItem().map { it.toDomain() }) }
    }

    @Test
    fun savesNothingWhenTheItemsFail() = runTest {
        val (purchase, items) = firstPurchase.toEntities()
        // Mesmo id de linha duas vezes: o segundo item falha e a compra não pode ficar sem produtos
        val broken = items.take(2).map { it.copy(id = 1) }

        assertFailsWith<Exception> { dao.insert(purchase, broken) }

        dao.observeAll().test { assertEquals(emptyList(), awaitItem()) }
    }

    private suspend fun insert(purchase: Purchase) {
        val (entity, items) = purchase.toEntities()
        dao.insert(entity, items)
    }
}
