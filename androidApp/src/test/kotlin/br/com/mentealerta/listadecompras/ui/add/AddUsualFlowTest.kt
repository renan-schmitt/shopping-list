package br.com.mentealerta.listadecompras.ui.add

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
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
 * "Você costuma comprar" na tela de adicionar (#65): aparece com o campo vazio, um toque põe na lista
 * e o produto passa para "Adicionados"; digitando, sai da frente.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = ThemeScreenshotTest.SCREEN)
class AddUsualFlowTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val fixture = AddFixture()

    private fun show() {
        val viewModel = fixture.viewModel(searchDispatcher = Dispatchers.Main)
        compose.setContent {
            ShoppingListTheme(darkTheme = false) { AddScreen(onClose = {}, viewModel = viewModel) }
        }
    }

    private val field get() = compose.onNode(hasSetTextAction())

    @Test
    fun touchingAUsualProductPutsItOnTheList() {
        fixture.preferredTerms.terms["banana"] = "banana nanica"
        fixture.buyEvery10Days("banana", "Banana")
        show()

        compose.onNodeWithText("Você costuma comprar").assert(isHeading())
        // Um botão só, lido em caixa normal, com o termo da pessoa.
        compose.onNode(hasText("banana nanica") and hasClickAction()).assertHeightIsAtLeast(72.dp).performClick()

        assertEquals("banana nanica", fixture.added.single().name)
        compose.onNodeWithText("Adicionado: Banana nanica.").assertExists()
        compose.onNodeWithText("Você costuma comprar").assertDoesNotExist()
        compose.onNodeWithText("Adicionados").assertExists()
        compose.onNode(hasText("banana nanica") and hasClickAction()).assertDoesNotExist()
    }

    @Test
    fun typingHidesTheUsualProducts() {
        fixture.buyEvery10Days("apple", "Maçã")
        show()
        compose.onNodeWithText("Você costuma comprar").assertExists()

        field.performTextInput("bergamota")
        compose.onNodeWithText("Você costuma comprar").assertDoesNotExist()

        field.performTextReplacement("")
        compose.onNodeWithText("Você costuma comprar").assertExists()
    }

    @Test
    fun withoutHistoryTheTitleDoesNotAppear() {
        show()

        compose.onNodeWithText("Você costuma comprar").assertDoesNotExist()
    }
}
