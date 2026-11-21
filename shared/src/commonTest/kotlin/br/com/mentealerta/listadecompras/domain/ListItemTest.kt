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

    @Test
    fun addMoreWithoutQuantityMakesTwo() {
        assertEquals(Quantity(2.0), cassava.addMore(null).quantity)
    }

    @Test
    fun addMoreSumsTheSameUnit() {
        val packages = cassava.copy(quantity = Quantity(2.0, QuantityUnit.PACKAGE))
        assertEquals(
            Quantity(5.0, QuantityUnit.PACKAGE),
            packages.addMore(Quantity(3.0, QuantityUnit.PACKAGE)).quantity,
        )
        assertEquals(Quantity(4.0), cassava.addMore(Quantity(3.0)).quantity)
    }

    @Test
    fun addMoreInAnotherUnitAddsOneOfTheItemUnit() {
        val half = cassava.copy(quantity = Quantity(0.5, QuantityUnit.KILOGRAM))
        assertEquals(Quantity(1.5, QuantityUnit.KILOGRAM), half.addMore(Quantity(2.0, QuantityUnit.CAN)).quantity)
        assertEquals(Quantity(1.5, QuantityUnit.KILOGRAM), half.addMore(null).quantity)
        assertEquals(Quantity(2.0), cassava.addMore(Quantity(2.0, QuantityUnit.LITER)).quantity)
    }

    @Test
    fun addMoreKeepsEverythingElse() {
        val item = cassava.copy(detail = "descascado")
        val more = item.addMore(null)
        assertEquals(item.copy(quantity = Quantity(2.0)), more)
    }

    @Test
    fun plusTurnsNoQuantityIntoTwo() {
        assertEquals(Quantity(2.0), cassava.increaseQuantity().quantity)
    }

    @Test
    fun plusAddsOneInTheSameUnit() {
        val kilos = cassava.copy(quantity = Quantity(1.5, QuantityUnit.KILOGRAM))
        assertEquals(Quantity(2.5, QuantityUnit.KILOGRAM), kilos.increaseQuantity().quantity)
    }

    @Test
    fun plusTakesHalfToOne() {
        val half = cassava.copy(quantity = Quantity(0.5, QuantityUnit.KILOGRAM))
        assertEquals(Quantity(1.0, QuantityUnit.KILOGRAM), half.increaseQuantity().quantity)
    }

    @Test
    fun minusTakesOneAway() {
        val kilos = cassava.copy(quantity = Quantity(2.5, QuantityUnit.KILOGRAM))
        assertEquals(Quantity(1.5, QuantityUnit.KILOGRAM), kilos.decreaseQuantity().quantity)
        assertEquals(Quantity(0.5, QuantityUnit.KILOGRAM), kilos.decreaseQuantity().decreaseQuantity().quantity)
    }

    @Test
    fun minusBackToOneUnitClearsTheQuantity() {
        val two = cassava.copy(quantity = Quantity(2.0))
        assertNull(two.decreaseQuantity().quantity)
    }

    @Test
    fun minusBackToOneKeepsOtherUnits() {
        val two = cassava.copy(quantity = Quantity(2.0, QuantityUnit.KILOGRAM))
        assertEquals(Quantity(1.0, QuantityUnit.KILOGRAM), two.decreaseQuantity().quantity)
    }

    @Test
    fun minusDoesNothingAtOneOrLess() {
        assertFalse(cassava.canDecreaseQuantity)
        assertEquals(cassava, cassava.decreaseQuantity())
        val oneKilo = cassava.copy(quantity = Quantity(1.0, QuantityUnit.KILOGRAM))
        assertFalse(oneKilo.canDecreaseQuantity)
        assertEquals(oneKilo, oneKilo.decreaseQuantity())
        val half = cassava.copy(quantity = Quantity(0.5, QuantityUnit.KILOGRAM))
        assertEquals(half, half.decreaseQuantity())
        assertTrue(cassava.increaseQuantity().canDecreaseQuantity)
    }
}
