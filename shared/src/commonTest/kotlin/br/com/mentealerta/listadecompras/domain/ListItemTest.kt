package br.com.mentealerta.listadecompras.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ListItemTest {
    private val cassava = ListItem.create(name = "aipim", productId = "cassava", sectionId = "produce")

    @Test
    fun newItemIsNotChecked() {
        assertFalse(cassava.checked)
    }

    @Test
    fun createTrimsNameAndDetail() {
        val item = ListItem.create(
            name = "  leite ",
            productId = "milk",
            sectionId = "deli_dairy",
            detail = " desnatado ",
        )
        assertEquals("leite", item.name)
        assertEquals("desnatado", item.detail)
    }

    @Test
    fun createTurnsBlankDetailIntoNull() {
        assertNull(ListItem.create(name = "leite", productId = "milk", sectionId = "deli_dairy", detail = "   ").detail)
    }

    @Test
    fun createGivesEachItemItsOwnId() {
        val other = ListItem.create(name = "aipim", productId = "cassava", sectionId = "produce")
        assertNotEquals(cassava.id, other.id)
    }

    @Test
    fun acceptsUnknownProduct() {
        val item = ListItem.create(name = "coisa nova", productId = null, sectionId = "other")
        assertNull(item.productId)
    }

    @Test
    fun rejectsBlankName() {
        assertFailsWith<IllegalArgumentException> {
            ListItem.create(name = "  ", productId = null, sectionId = "other")
        }
    }

    @Test
    fun rejectsNameOrDetailWithSurroundingSpaces() {
        assertFailsWith<IllegalArgumentException> { cassava.copy(name = " aipim") }
        assertFailsWith<IllegalArgumentException> { cassava.copy(detail = "") }
        assertFailsWith<IllegalArgumentException> { cassava.copy(detail = "fresco ") }
    }

    @Test
    fun rejectsBlankSection() {
        assertFailsWith<IllegalArgumentException> { cassava.copy(sectionId = "") }
    }

    @Test
    fun checkAndUncheckKeepEverythingElse() {
        val checked = cassava.check()
        assertTrue(checked.checked)
        assertEquals(cassava, checked.uncheck())
    }

    @Test
    fun purchasedItemCopiesProductNameAndQuantity() {
        val item = ListItem.create(
            name = "carne moída",
            productId = "ground_beef",
            sectionId = "meat",
            quantity = Quantity(0.5, QuantityUnit.KILOGRAM),
            detail = "de primeira",
        )
        val expected = PurchasedItem(
            productId = "ground_beef",
            name = "carne moída",
            quantity = Quantity(0.5, QuantityUnit.KILOGRAM),
        )
        assertEquals(expected, item.toPurchasedItem())
    }
}
