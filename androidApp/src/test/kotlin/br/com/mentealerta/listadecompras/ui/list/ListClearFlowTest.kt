package br.com.mentealerta.listadecompras.ui.list

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
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
 * "Jogar a lista fora" (#58): a única ação que pergunta antes. "Não" deixa a lista como está; "Sim,
 * jogar fora" tira todos os produtos, riscados ou não, e o "Desfazer" devolve tudo no mesmo lugar.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = ThemeScreenshotTest.SCREEN)
class ListClearFlowTest {

    @get:Rule
    val compose = createComposeRule()

    private val fixture = EditFixture()
    private val banana = ListItem(ListItemId("1"), "Banana", "banana", "produce", checked = true)
    private val kale = ListItem(ListItemId("2"), "Couve", null, "produce")
    private val beans = ListItem(ListItemId("3"), "Feijão", "beans", "pantry")

    private fun show(vararg items: ListItem) {
        items.forEach { fixture.put(it) }
        val viewModel = fixture.listViewModel()
        compose.setContent {
            ShoppingListTheme(darkTheme = false) { ListScreen(viewModel = viewModel) }
        }
    }

    private fun button(text: String) = compose.onNode(hasText(text) and hasClickAction())

    /** "Jogar a lista fora" fica no fim do modo "Mudar a lista" (#121). */
    private fun tapClear() {
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Mudar a lista"))
        button("Mudar a lista").performClick()
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Jogar a lista fora"))
        button("Jogar a lista fora").performClick()
    }

    private fun assertNoticeShown(message: String) {
        compose.onAllNodes(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion) and hasAnyDescendant(hasText(message)),
        ).onFirst().assertExists()
    }

    @Test
    fun asksBeforeThrowingAwayCountingCheckedItemsToo() {
        show(banana, kale, beans)

        tapClear()

        compose.onNode(isDialog()).assertExists()
        compose.onNodeWithText("Jogar a lista fora?").assertExists()
        compose.onNodeWithText("Os 3 produtos serão tirados.").assertExists()
        assertEquals(listOf(banana, kale, beans), fixture.items)
    }

    @Test
    fun withASingleProductTheQuestionUsesTheSingular() {
        show(beans)

        tapClear()

        compose.onNodeWithText("O produto será tirado.").assertExists()
    }

    @Test
    fun noLeavesTheListAsItIs() {
        show(banana, kale)
        tapClear()

        button("Não").performClick()

        compose.onNode(isDialog()).assertDoesNotExist()
        assertEquals(listOf(banana, kale), fixture.items)
        // Continua no modo de mudar.
        compose.onNodeWithText("Toque no produto que quer mudar.").assertExists()
        compose.onNodeWithText("Couve").assertExists()
    }

    @Test
    fun yesThrowsEverythingAwayAndShowsTheNotice() {
        show(banana, kale, beans)
        tapClear()

        button("Sim, jogar fora").performClick()

        compose.onNode(isDialog()).assertDoesNotExist()
        assertTrue(fixture.items.isEmpty())
        assertNoticeShown("A lista foi jogada fora.")
        // Volta para a lista, vazia: sem o que mudar nem jogar fora.
        compose.onNodeWithText("Toque em Adicionar para começar sua lista.").assertExists()
        compose.onAllNodesWithText("Mudar a lista").assertCountEquals(0)
        compose.onAllNodesWithText("Jogar a lista fora").assertCountEquals(0)
    }

    @Test
    fun opsVoltarBringsEverythingBackInPlace() {
        show(banana, kale, beans)
        tapClear()
        button("Sim, jogar fora").performClick()

        button("Desfazer").performClick()

        assertEquals(listOf(banana, kale, beans), fixture.items)
        compose.onNodeWithText("Couve").assertExists()
        // O risco volta junto: o rodapé continua no modo comprando.
        button("Terminei").assertExists()
    }
}
