package br.com.mentealerta.listadecompras.ui.list

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.ui.add.AddFixture
import br.com.mentealerta.listadecompras.ui.add.AddScreen
import br.com.mentealerta.listadecompras.ui.edit.EditFixture
import br.com.mentealerta.listadecompras.ui.edit.EditScreen
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme
import br.com.mentealerta.listadecompras.ui.theme.ThemeScreenshotTest
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Receber uma lista pelo "Compartilhar" do WhatsApp (#135): o texto que chega abre a tela de
 * adicionar na conferência **Lista recebida**, com a lista aberta ou com outra tela por cima dela.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = ThemeScreenshotTest.SCREEN)
class ListReceiveFlowTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val list = EditFixture()
    private val add = AddFixture()
    private var receivedText by mutableStateOf<String?>(null)

    private val whatsApp = "[18:23, 24/11/2026] Maria: *Lista de compras*\n- Banana (6)\n- Maçã"

    private fun show() {
        val listViewModel = list.listViewModel()
        val editViewModel = list.editViewModel()
        val addViewModel = add.viewModel(searchDispatcher = Dispatchers.Main)
        compose.setContent {
            ShoppingListTheme(darkTheme = false) {
                ListScreen(
                    receivedText = receivedText,
                    onReceivedTextConsume = { receivedText = null },
                    viewModel = listViewModel,
                    addScreen = { text, onHandled, onClose ->
                        AddScreen(
                            onClose,
                            receivedText = text,
                            onReceivedTextConsume = onHandled,
                            viewModel = addViewModel,
                        )
                    },
                    editScreen = { id, onClose -> EditScreen(id, onClose, viewModel = editViewModel) },
                )
            }
        }
    }

    private fun button(text: String) = compose.onNode(hasText(text) and hasClickAction())

    @Test
    fun sharedTextOpensTheReviewFromTheList() {
        show()

        receivedText = whatsApp

        compose.onNodeWithText("Lista recebida").assertExists()
        compose.onNodeWithText("Banana, 6").assertExists()
        assertNull(receivedText)
        assertTrue(add.added.isEmpty())

        button("Pôr 2 produtos na lista").performClick()
        assertEquals(listOf("Banana", "Maçã"), add.added.map { it.name })
    }

    @Test
    fun sharedTextClosesTheEditingAndKeepsTheChange() {
        list.put(ListItem(ListItemId("1"), "Feijão", "beans", "pantry"))
        show()
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Mudar a lista"))
        button("Mudar a lista").performClick()
        compose.onNodeWithText("Feijão").performClick()
        compose.onNode(hasSetTextAction() and hasContentDescription("Nome", substring = true))
            .performTextReplacement("Feijão preto")

        receivedText = whatsApp

        compose.onNodeWithText("Lista recebida").assertExists()
        compose.waitForIdle()
        assertEquals("Feijão preto", list.items.single().name)
    }

    @Test
    fun sharedTextWithoutProducts() {
        receivedText = "*Lista de compras*"
        show()

        compose.onNodeWithText("Não achei produtos na mensagem.").assertExists()
    }
}
