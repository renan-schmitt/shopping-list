package br.com.mentealerta.listadecompras.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.time.Instant

class PurchaseTest {
    private val date = Instant.parse("2026-11-12T10:00:00Z")
    private val milk = ListItem.create(
        name = "leite",
        productId = "milk",
        sectionId = "deli_dairy",
        quantity = Quantity(2.0),
    )
    private val unknown = ListItem.create(name = "coisa nova", productId = null, sectionId = "other")

    @Test
    fun createCopiesItemsInOrder() {
        val purchase = Purchase.create(date, listOf(milk, unknown))
        assertEquals(date, purchase.date)
        assertEquals(
            listOf(
                PurchasedItem(productId = "milk", name = "leite", quantity = Quantity(2.0)),
                PurchasedItem(productId = null, name = "coisa nova", quantity = null),
            ),
            purchase.items,
        )
    }

    @Test
    fun createGivesEachPurchaseItsOwnId() {
        assertNotEquals(Purchase.create(date, listOf(milk)).id, Purchase.create(date, listOf(milk)).id)
    }

    @Test
    fun rejectsPurchaseWithoutItems() {
        assertFailsWith<IllegalArgumentException> { Purchase.create(date, emptyList()) }
    }

    @Test
    fun rejectsPurchasedItemWithoutName() {
        assertFailsWith<IllegalArgumentException> { PurchasedItem(productId = "milk", name = " ", quantity = null) }
    }
}
