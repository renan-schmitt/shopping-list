package br.com.mentealerta.listadecompras.ui.list

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.domain.Purchase
import br.com.mentealerta.listadecompras.domain.PurchaseId
import br.com.mentealerta.listadecompras.domain.PurchasedItem
import br.com.mentealerta.listadecompras.ui.edit.EditFixture
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme
import br.com.mentealerta.listadecompras.ui.theme.ThemeScreenshotTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.time.Instant

/**
 * "Está faltando?" no topo da lista (#64): o produto mais atrasado, um por vez. **Sim, pôr na lista**
 * põe o produto na seção dele; **Não** guarda o "Não" e passa para o próximo.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = ThemeScreenshotTest.SCREEN)
class ListSuggestionFlowTest {

    @get:Rule
    val compose = createComposeRule()

    /** Hoje é 23/11/2026 ([EditFixture.now]). */
    private val fixture = EditFixture()
    private val kale = ListItem(ListItemId("1"), "Couve", null, "produce")

    private val beansQuestion = "Faz 14 dias que você comprou feijão. Está faltando?"
    private val bananaQuestion = "Faz 13 dias que você comprou banana. Está faltando?"

    private fun show(vararg items: ListItem) {
        // Feijão de 7 em 7 dias, último há 14 (2×); banana de 10 em 10, última há 13 (1,3×).
        buy("beans", "Feijão", "2026-10-26", "2026-11-02", "2026-11-09")
        buy("banana", "Banana", "2026-10-21", "2026-10-31", "2026-11-10")
        items.forEach { fixture.put(it) }
        val viewModel = fixture.listViewModel()
        compose.setContent {
            ShoppingListTheme(darkTheme = false) { ListScreen(viewModel = viewModel) }
        }
    }

    private fun buy(productId: String, name: String, vararg days: String) = days.forEach { day ->
        fixture.putPurchase(
            Purchase(
                PurchaseId.random(),
                Instant.parse("${day}T15:00:00Z"),
                listOf(PurchasedItem(productId, name, quantity = null)),
            ),
        )
    }

    private fun button(text: String) = compose.onNode(hasText(text) and hasClickAction())

    @Test
    fun showsOnlyTheMostOverdueProductAtTheTop() {
        show(kale)

        compose.onNodeWithText(beansQuestion).assertExists()
        compose.onAllNodesWithText(bananaQuestion).assertCountEquals(0)
    }

    @Test
    fun yesPutsTheProductInItsSectionAndShowsTheNextOne() {
        show(kale)

        button("Sim, pôr na lista").performClick()

        val beans = fixture.items.single { it.name == "Feijão" }
        assertEquals("beans", beans.productId)
        assertEquals("pantry", beans.sectionId)
        compose.onNodeWithText("Mercearia").assertExists()
        compose.onAllNodesWithText(beansQuestion).assertCountEquals(0)
        compose.onNodeWithText(bananaQuestion).assertExists()
    }

    @Test
    fun noKeepsTheAnswerAndShowsTheNextOneUntilNothingIsLeft() {
        show(kale)

        button("Não").performClick()

        assertEquals(setOf("beans"), fixture.dismissals.dismissals.keys)
        assertEquals(listOf(kale), fixture.items)
        compose.onNodeWithText(bananaQuestion).assertExists()

        button("Não").performClick()

        compose.onAllNodesWithText(bananaQuestion).assertCountEquals(0)
        compose.onAllNodesWithText("Sim, pôr na lista").assertCountEquals(0)
    }

    @Test
    fun emptyListAlsoAsks() {
        show()

        compose.onNodeWithText(beansQuestion).assertExists()
        button("Sim, pôr na lista").performClick()

        assertEquals(listOf("Feijão"), fixture.items.map { it.name })
    }
}
