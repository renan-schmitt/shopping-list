package br.com.mentealerta.listadecompras.ui.add

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme
import br.com.mentealerta.listadecompras.ui.theme.ThemeScreenshotTest
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Fluxo de adicionar digitando (#53): digitar, ver os achados, tocar e ver o aviso; a tecla "pronto";
 * o produto desconhecido; voltar para a lista. E a acessibilidade da tela: rótulo do campo, botões
 * de 72dp lidos em caixa normal, títulos e o aviso lido pelo TalkBack.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = ThemeScreenshotTest.SCREEN)
class AddFlowTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val fixture = AddFixture()
    private var closes = 0

    private fun show() {
        val viewModel = fixture.viewModel(searchDispatcher = Dispatchers.Main)
        compose.setContent {
            ShoppingListTheme(darkTheme = false) { AddScreen(onClose = { closes++ }, viewModel = viewModel) }
        }
    }

    private val field get() = compose.onNode(hasSetTextAction())

    @Test
    fun typingShowsTheProductAndTouchingPutsItOnTheList() {
        show()
        field.assertIsFocused()

        field.performTextInput("2 banan")
        compose.onNodeWithText("Achei estes").assert(isHeading())
        compose.onNode(hasText("Banana, 2") and hasClickAction()).assertHeightIsAtLeast(72.dp).performClick()

        assertEquals("banana", fixture.added.single().productId)
        assertEquals(Quantity(2.0), fixture.added.single().quantity)
        compose.onNodeWithText("Banana entrou na lista.")
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion))
        // O campo fica vazio para o próximo produto, e a tela continua aberta.
        field.assert(hasText(""))
        compose.onAllNodesWithText("Achei estes").assertCountEquals(0)
        assertEquals(0, closes)
    }

    @Test
    fun searchToleratesMissingAccents() {
        show()
        field.performTextInput("maca")
        // Caixa alta só na tela; o TalkBack lê o nome como está no catálogo.
        compose.onNode(hasText("Maçã") and hasClickAction()).assertExists()
    }

    @Test
    fun doneKeyPutsTheFirstOption() {
        show()
        field.performTextInput("banana")
        field.performImeAction()

        assertEquals("banana", fixture.added.single().productId)
    }

    @Test
    fun unknownProductCanBePutAsTyped() {
        show()
        field.performTextInput("bergamota")
        compose.onNodeWithText("Não achei esse produto. Pode pôr assim mesmo:").assertExists()
        compose.onNode(hasText("Pôr “bergamota” na lista") and hasClickAction()).performClick()

        val item = fixture.added.single()
        assertEquals("bergamota", item.name)
        assertEquals(null, item.productId)
        compose.onNodeWithText("Bergamota entrou na lista.").assertExists()
    }

    @Test
    fun fieldIsReadWithItsLabel() {
        show()
        field.assert(SemanticsMatcher.expectValue(SemanticsProperties.ContentDescription, listOf("Procurar produto")))
    }

    @Test
    fun backButtonAndSystemBackCloseTheScreen() {
        show()
        compose.onNode(hasText("Voltar para a lista") and hasClickAction())
            .assertHeightIsAtLeast(64.dp)
            .performClick()
        assertEquals(1, closes)

        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        assertEquals(2, closes)
    }

    @Test
    @Config(fontScale = 2.0f)
    fun longNamesWrapWithLargeFont() {
        compose.setContent { ShoppingListTheme(darkTheme = false) { AddContent(SampleAddState) } }
        compose.onNode(hasText("Leite condensado de caixinha da marca preferida, 2 litros") and hasClickAction())
            .assertHeightIsAtLeast(72.dp)
    }
}
