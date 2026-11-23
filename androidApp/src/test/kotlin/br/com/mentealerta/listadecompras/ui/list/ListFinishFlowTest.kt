package br.com.mentealerta.listadecompras.ui.list

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.domain.PurchasedItem
import br.com.mentealerta.listadecompras.ui.edit.EditFixture
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme
import br.com.mentealerta.listadecompras.ui.theme.ThemeScreenshotTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * "Terminei" (#57): grava a compra com os riscados, tira esses da lista, mostra quantos foram comprados
 * e quantos ficaram; o "Desfazer" apaga a compra e devolve os riscados.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = ThemeScreenshotTest.SCREEN)
class ListFinishFlowTest {

    @get:Rule
    val compose = createComposeRule()

    private val fixture = EditFixture()
    private val banana = ListItem(ListItemId("1"), "Banana", "banana", "produce", checked = true)
    private val kale = ListItem(ListItemId("2"), "Couve", null, "produce", checked = true)
    private val beans = ListItem(ListItemId("3"), "Feijão", "beans", "pantry")

    private fun show(vararg items: ListItem) {
        items.forEach { fixture.put(it) }
        val viewModel = fixture.listViewModel()
        compose.setContent {
            ShoppingListTheme(darkTheme = false) { ListScreen(viewModel = viewModel) }
        }
    }

    private fun button(text: String) = compose.onNode(hasText(text) and hasClickAction())

    /** O aviso inteiro é anunciado pelo TalkBack quando aparece. */
    private fun assertNoticeShown(message: String) {
        compose.onAllNodes(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion) and hasAnyDescendant(hasText(message)),
        ).onFirst().assertExists()
    }

    @Test
    fun finishingSavesTheCheckedAndKeepsTheRestForNextTime() {
        show(banana, kale, beans)

        button("Terminei").performClick()

        assertEquals(listOf(beans), fixture.items)
        assertEquals(
            listOf(PurchasedItem("banana", "Banana", null), PurchasedItem(null, "Couve", null)),
            fixture.purchases.single().items,
        )
        assertNoticeShown("Pronto! 2 produtos comprados. Ficou 1 na lista para a próxima vez.")
        compose.onAllNodesWithText("Banana").assertCountEquals(0)
        // Sem riscados, a tela volta ao começo: só o "Adicionar".
        compose.onAllNodesWithText("Terminei").assertCountEquals(0)
    }

    @Test
    fun withEverythingCheckedOnlyTheFirstSentenceIsShown() {
        show(banana)

        button("Terminei").performClick()

        assertTrue(fixture.items.isEmpty())
        assertNoticeShown("Pronto! 1 produto comprado.")
    }

    @Test
    fun opsVoltarDeletesThePurchaseAndBringsTheCheckedBack() {
        show(banana, kale, beans)
        button("Terminei").performClick()

        button("Desfazer").performClick()

        assertEquals(listOf(banana, kale, beans), fixture.items)
        assertTrue(fixture.purchases.isEmpty())
        compose.onNodeWithText("Banana").assertExists()
        button("Terminei").assertExists()
    }
}
