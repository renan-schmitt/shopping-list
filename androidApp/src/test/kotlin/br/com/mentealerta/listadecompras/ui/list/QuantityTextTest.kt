package br.com.mentealerta.listadecompras.ui.list

import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.domain.QuantityUnit
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QuantityTextTest {
    private val resources = RuntimeEnvironment.getApplication().resources
    private val ptBr = Locale.forLanguageTag("pt-BR")

    private fun format(amount: Double, unit: QuantityUnit = QuantityUnit.UNIT) =
        Quantity(amount, unit).format(resources, ptBr)

    @Test
    fun simpleCountShowsOnlyTheNumber() {
        assertEquals(QuantityText("2", "2"), format(2.0))
    }

    @Test
    fun unitsFollowSingularAndPlural() {
        assertEquals("1 litro", format(1.0, QuantityUnit.LITER).visible)
        assertEquals("2 litros", format(2.0, QuantityUnit.LITER).visible)
        assertEquals("3 pacotes", format(3.0, QuantityUnit.PACKAGE).visible)
        assertEquals("1 dúzia", format(1.0, QuantityUnit.DOZEN).visible)
        assertEquals("2 maços", format(2.0, QuantityUnit.BUNCH).visible)
    }

    @Test
    fun symbolsDoNotChangeInThePlural() {
        assertEquals("1 kg", format(1.0, QuantityUnit.KILOGRAM).visible)
        assertEquals("500 g", format(500.0, QuantityUnit.GRAM).visible)
    }

    @Test
    fun decimalsFollowTheLocale() {
        assertEquals("1,5 kg", format(1.5, QuantityUnit.KILOGRAM).visible)
        assertEquals("1,5 litro", format(1.5, QuantityUnit.LITER).visible)
        assertEquals("2,25 kg", format(2.25, QuantityUnit.KILOGRAM).visible)
    }

    @Test
    fun halfShowsSymbolAndIsSpokenInWords() {
        assertEquals(QuantityText("½ kg", "meio quilo"), format(0.5, QuantityUnit.KILOGRAM))
        assertEquals(QuantityText("½ litro", "meio litro"), format(0.5, QuantityUnit.LITER))
        assertEquals(QuantityText("½ dúzia", "meia dúzia"), format(0.5, QuantityUnit.DOZEN))
        assertEquals(QuantityText("½", "meia unidade"), format(0.5))
    }

    @Test
    fun oneHalfDozenHasNoNumber() {
        assertEquals(QuantityText("meia dúzia", "meia dúzia"), format(1.0, QuantityUnit.HALF_DOZEN))
        assertEquals("2 meias dúzias", format(2.0, QuantityUnit.HALF_DOZEN).visible)
    }
}
