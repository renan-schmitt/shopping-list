package br.com.mentealerta.listadecompras.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class QuantityTest {
    @Test
    fun simpleCountUsesUnit() {
        assertEquals(QuantityUnit.UNIT, Quantity(2.0).unit)
    }

    @Test
    fun acceptsFractions() {
        assertEquals(0.5, Quantity(0.5, QuantityUnit.KILOGRAM).amount)
    }

    @Test
    fun rejectsZeroNegativeAndNonFiniteAmounts() {
        assertFailsWith<IllegalArgumentException> { Quantity(0.0) }
        assertFailsWith<IllegalArgumentException> { Quantity(-1.0) }
        assertFailsWith<IllegalArgumentException> { Quantity(Double.NaN) }
        assertFailsWith<IllegalArgumentException> { Quantity(Double.POSITIVE_INFINITY) }
    }

    @Test
    fun hasEveryUnitFromThePlan() {
        assertEquals(13, QuantityUnit.entries.size)
    }
}
