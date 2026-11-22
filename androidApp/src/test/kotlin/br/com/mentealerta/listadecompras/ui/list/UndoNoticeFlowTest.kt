package br.com.mentealerta.listadecompras.ui.list

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.ui.edit.EditFixture
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme
import br.com.mentealerta.listadecompras.ui.theme.ThemeScreenshotTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Aviso com "Ops, voltar" (#59): anunciado pelo TalkBack, fica 15 s na tela (mais que o padrão, para
 * o público 60+), some ao tocar em "Ops, voltar" e um aviso novo tira o anterior na hora.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = ThemeScreenshotTest.SCREEN)
class UndoNoticeFlowTest {

    @get:Rule
    val compose = createComposeRule()

    private val fixture = EditFixture()
    private val banana = ListItem(ListItemId("1"), "Banana", "banana", "produce", checked = true)
    private val beans = ListItem(ListItemId("2"), "Feijão", "beans", "pantry")

    private val finished = "Pronto! 1 produto comprado. Ficou 1 na lista para a próxima vez."

    private fun show() {
        fixture.put(banana)
        fixture.put(beans)
        val viewModel = fixture.listViewModel()
        compose.setContent {
            ShoppingListTheme(darkTheme = false) { ListScreen(viewModel = viewModel) }
        }
    }

    private fun button(text: String) = compose.onNode(hasText(text) and hasClickAction())

    /** O aviso inteiro é uma live region: o TalkBack anuncia quando ele aparece. */
    private fun notice(message: String) = compose.onAllNodes(
        SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion) and hasAnyDescendant(hasText(message)),
    ).onFirst()

    @Test
    fun noticeIsAnnouncedAndStaysLongerThanTheDefault() {
        show()
        button("Terminei").performClick()
        notice(finished).assertExists()

        compose.mainClock.autoAdvance = false
        // O "Long" do Material some aos 10 s; o aviso continua aos 14 s e some aos 15 s.
        compose.mainClock.advanceTimeBy(14_000)
        notice(finished).assertExists()

        compose.mainClock.advanceTimeBy(2_000)
        compose.onNodeWithText(finished).assertDoesNotExist()
    }

    @Test
    fun opsVoltarClosesTheNotice() {
        show()
        button("Terminei").performClick()

        button("Ops, voltar").performClick()

        compose.onNodeWithText(finished).assertDoesNotExist()
        compose.onNodeWithText("Banana").assertExists()
    }

    @Test
    fun aNewNoticeReplacesThePreviousRightAway() {
        show()
        button("Terminei").performClick()
        notice(finished).assertExists()

        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Mudar a lista"))
        button("Mudar a lista").performClick()
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Jogar a lista fora"))
        button("Jogar a lista fora").performClick()
        button("Sim, jogar fora").performClick()

        notice("A lista foi jogada fora.").assertExists()
        compose.onNodeWithText(finished).assertDoesNotExist()
    }
}
