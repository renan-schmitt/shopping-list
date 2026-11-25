package br.com.mentealerta.listadecompras.ui.add

import android.content.ClipData
import android.content.ClipboardManager
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme
import br.com.mentealerta.listadecompras.ui.theme.ThemeScreenshotTest
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Colar uma lista recebida pelo WhatsApp (#68): o botão só aparece com texto copiado; tocar abre a
 * conferência, a lixeira tira um produto, tocar no produto abre a edição (#136) e **Pôr na lista**
 * põe o resto; o voltar do celular é o **Deixa pra lá**.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = ThemeScreenshotTest.SCREEN)
class AddPasteFlowTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val fixture = AddFixture()
    private var closes = 0

    private val clipboard get() = compose.activity.getSystemService(ClipboardManager::class.java)

    private fun copy(text: String) = clipboard.setPrimaryClip(ClipData.newPlainText("lista", text))

    private fun show() {
        val viewModel = fixture.viewModel(searchDispatcher = Dispatchers.Main)
        compose.setContent {
            ShoppingListTheme(darkTheme = false) { AddScreen(onClose = { closes++ }, viewModel = viewModel) }
        }
    }

    private val pasteButton = hasText("Colar a lista") and hasClickAction()

    @Test
    fun withoutCopiedTextThereIsNoPasteButton() {
        show()
        compose.onAllNodes(pasteButton).assertCountEquals(0)
    }

    @Test
    fun copyingWhileTheScreenIsOpenShowsTheButton() {
        show()
        copy("arroz")
        compose.onNode(pasteButton).assertExists()
    }

    @Test
    fun pasteReviewRemoveAndPut() {
        copy("*Lista de compras*\n\n*Hortifrúti*\n- Banana (6)\n- Maçã\n- Bergamota")
        show()

        compose.onNodeWithText("Copiou uma lista no WhatsApp?").assertExists()
        compose.onNode(pasteButton).performClick()

        compose.onNodeWithText("Lista recebida").assert(isHeading())
        compose.onNodeWithText("Banana, 6").assertExists()
        assertTrue(fixture.added.isEmpty())

        compose.onNode(hasContentDescription("Tirar Maçã da lista") and hasClickAction()).performClick()
        compose.onAllNodesWithText("Maçã").assertCountEquals(0)
        compose.onNode(hasText("Pôr 2 produtos na lista") and hasClickAction()).performClick()

        assertEquals(listOf("Banana", "Bergamota"), fixture.added.map { it.name })
        compose.onNodeWithText("Adicionados: 2 produtos.").assertExists()
        compose.onNodeWithText("Adicionados").performScrollTo().assert(isHeading())
        assertEquals(0, closes)
    }

    @Test
    fun backLeavesTheReviewWithoutPutting() {
        copy("banana\nmaçã")
        show()
        compose.onNode(pasteButton).performClick()
        compose.onNodeWithText("Lista recebida").assertExists()

        compose.activity.onBackPressedDispatcher.onBackPressed()
        compose.waitForIdle()

        compose.onAllNodesWithText("Lista recebida").assertCountEquals(0)
        assertTrue(fixture.added.isEmpty())
        assertEquals(0, closes)

        compose.onNode(pasteButton).performClick()
        compose.onNode(hasText("Deixa pra lá") and hasClickAction()).performClick()
        compose.onAllNodesWithText("Lista recebida").assertCountEquals(0)
        assertTrue(fixture.added.isEmpty())
    }

    @Test
    fun tapProductChangeItAndPut() {
        copy("zabumbinha\nbanana")
        show()
        compose.onNode(pasteButton).performClick()

        compose.onNode(hasText("zabumbinha") and hasClickAction()).performClick()
        compose.onNode(hasText("Voltar para a lista recebida") and hasClickAction()).assertExists()
        compose.onAllNodesWithText("Tirar da lista").assertCountEquals(0)

        compose.onNode(hasText("zabumbinha") and hasSetTextAction()).performTextReplacement("Goiabada")
        compose.onNode(hasText("Mais") and hasClickAction()).performClick()
        compose.onNode(hasText("Voltar para a lista recebida") and hasClickAction()).performClick()

        compose.onNodeWithText("Lista recebida").assertExists()
        compose.onNodeWithText("Goiabada, 2").assertExists()
        // Voltando da edição, o teclado não abre por cima da conferência.
        compose.onNode(hasSetTextAction()).assertIsNotFocused()
        compose.onNode(hasText("Pôr 2 produtos na lista") and hasClickAction()).performClick()

        assertEquals(listOf("Goiabada", "banana"), fixture.added.map { it.name })
        assertEquals(Quantity(2.0), fixture.added.first().quantity)
        assertEquals(0, closes)
    }

    @Test
    fun backWhileChangingReturnsToTheReview() {
        copy("banana\nmaçã")
        show()
        compose.onNode(pasteButton).performClick()
        compose.onNode(hasText("banana") and hasClickAction()).performClick()
        compose.onNode(hasText("Mais") and hasClickAction()).performClick()

        compose.activity.onBackPressedDispatcher.onBackPressed()
        compose.waitForIdle()

        compose.onNodeWithText("Lista recebida").assertExists()
        compose.onNodeWithText("banana, 2").assertExists()
        assertTrue(fixture.added.isEmpty())
        assertEquals(0, closes)
    }

    @Test
    fun textWithoutProducts() {
        copy("*Lista de compras*")
        show()
        compose.onNode(pasteButton).performClick()
        compose.onNodeWithText("Não achei produtos no texto copiado.").assertExists()
    }
}
