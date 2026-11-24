package br.com.mentealerta.listadecompras.ui.list

import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.domain.QuantityUnit
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.Locale

/** Formato do texto do "Mandar a lista" (#67): o que falta, por seção, legível no WhatsApp. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ShareListTextTest {
    private val resources = RuntimeEnvironment.getApplication().resources
    private val ptBr = Locale.forLanguageTag("pt-BR")

    private fun row(name: String, quantity: Quantity? = null, detail: String? = null, checked: Boolean = false) =
        ListRowUi(ListItemId(name), name, detail, quantity, checked, R.drawable.ic_add)

    private fun text(vararg sections: SectionUi) = shareListText(sections.toList(), resources, ptBr)

    @Test
    fun groupsByTheSectionsInTheirOrderWithWhatsAppBold() {
        val text = text(
            SectionUi("produce", "Hortifrúti", listOf(row("Banana", Quantity(6.0)), row("Tomate"))),
            SectionUi(
                "dairy",
                "Frios e laticínios",
                listOf(row("Leite", Quantity(2.0, QuantityUnit.LITER), detail = "desnatado")),
            ),
        )
        assertEquals(
            """
            *Lista de compras*

            *Hortifrúti*
            - Banana (6)
            - Tomate

            *Frios e laticínios*
            - Leite desnatado (2 litros)
            """.trimIndent(),
            text,
        )
    }

    @Test
    fun leavesOutWhatWasAlreadyPicked() {
        val text = text(
            SectionUi("produce", "Hortifrúti", listOf(row("Banana"))),
            SectionUi("pantry", "Mercearia", listOf(row("Arroz", checked = true))),
            SectionUi.bought(listOf(row("Café", checked = true))),
        )
        assertEquals("*Lista de compras*\n\n*Hortifrúti*\n- Banana", text)
    }

    @Test
    fun quantitiesAppearAsInTheList() {
        val text = text(
            SectionUi(
                "butcher",
                "Açougue e peixaria",
                listOf(
                    row("carne moída", Quantity(0.5, QuantityUnit.KILOGRAM)),
                    row("Frango", Quantity(1.5, QuantityUnit.KILOGRAM)),
                    row("Ovo", Quantity(1.0, QuantityUnit.HALF_DOZEN)),
                ),
            ),
        )
        // O nome começa com maiúscula, como na lista, mesmo se a pessoa escreveu em minúscula.
        assertEquals(
            "*Lista de compras*\n\n*Açougue e peixaria*\n" +
                "- Carne moída (½ kg)\n- Frango (1,5 kg)\n- Ovo (meia dúzia)",
            text,
        )
    }
}
