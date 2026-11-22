package br.com.mentealerta.listadecompras.ui.list

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme
import br.com.mentealerta.listadecompras.ui.theme.ThemeScreenshotTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Modo "Mudar a lista" (#121): tocar num produto abre a edição (nunca risca), "Já peguei" aparece no
 * fim, "Jogar a lista fora" fica por último e o TalkBack ouve a ação "Mudar".
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = ThemeScreenshotTest.SCREEN)
class ChangeListContentTest {

    @get:Rule
    val compose = createComposeRule()

    private val edits = mutableListOf<ListItemId>()
    private var clears = 0
    private var closes = 0

    private fun show() {
        compose.setContent {
            ShoppingListTheme(darkTheme = false) {
                ChangeListContent(
                    SampleListState,
                    onEdit = { edits += it },
                    onClear = { clears++ },
                    onClose = { closes++ },
                )
            }
        }
    }

    private fun button(text: String) = compose.onNode(hasText(text) and hasClickAction())

    @Test
    fun tappingAProductOpensTheEdit() {
        show()
        compose.onNodeWithText("Tomate").performClick()
        assertEquals(listOf(ListItemId("tomato")), edits)
    }

    @Test
    fun checkedProductsAreUnderJaPegueiAndStillOpenTheEdit() {
        show()
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Já peguei"))
        compose.onNodeWithText("Banana, 6")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Comprado"))
            .performScrollTo()
            .performClick()
        assertEquals(listOf(ListItemId("banana")), edits)
    }

    @Test
    fun talkBackHearsTheChangeAction() {
        show()
        compose.onNodeWithText("Tomate").assert(
            SemanticsMatcher("ação \"Mudar\"") { node ->
                node.config.getOrElseNullable(SemanticsActions.OnClick) { null }?.label == "Mudar"
            },
        )
    }

    @Test
    fun clearIsAtTheEndAndBackGoesToTheList() {
        show()
        button("Jogar a lista fora").performScrollTo().assertHeightIsAtLeast(64.dp).performClick()
        assertEquals(1, clears)
        button("Voltar para a lista").performClick()
        assertEquals(1, closes)
        assertEquals(emptyList<ListItemId>(), edits)
    }
}
