package br.com.mentealerta.listadecompras.ui.list

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.ui.edit.EditFixture
import br.com.mentealerta.listadecompras.ui.edit.EditScreen
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
 * Ações do TalkBack na linha da lista (#71): além de riscar (tocar duas vezes), "Mudar" abre a
 * edição direto e "Tirar da lista" tira o produto com o aviso e "Ops, voltar", sem passar pelo modo
 * "Mudar a lista".
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = ThemeScreenshotTest.SCREEN)
class ListTalkBackActionsTest {

    @get:Rule
    val compose = createComposeRule()

    private val fixture = EditFixture()
    private val beans = fixture.put(ListItem(ListItemId("1"), "feijão", "beans", "pantry"))

    private fun show() {
        val listViewModel = fixture.listViewModel()
        val editViewModel = fixture.editViewModel()
        compose.setContent {
            ShoppingListTheme(darkTheme = false) {
                ListScreen(
                    viewModel = listViewModel,
                    editScreen = { id, onClose -> EditScreen(id, onClose, viewModel = editViewModel) },
                )
            }
        }
    }

    private fun row() = compose.onNodeWithText("feijão")

    private fun SemanticsNodeInteraction.performCustomAction(label: String) {
        val actions = fetchSemanticsNode().config.getOrNull(SemanticsActions.CustomActions).orEmpty()
        val action = actions.single { it.label == label }
        compose.runOnIdle { action.action() }
    }

    @Test
    fun rowOffersCheckChangeAndRemove() {
        show()
        val node = row().fetchSemanticsNode()
        assertEquals("Marcar como comprado", node.config[SemanticsActions.OnClick].label)
        assertEquals(
            listOf("Mudar", "Tirar da lista"),
            node.config[SemanticsActions.CustomActions].map { it.label },
        )
    }

    @Test
    fun changeOpensTheEditAndGoingBackReturnsToTheList() {
        show()
        row().performCustomAction("Mudar")

        compose.onNode(hasText("Voltar para a lista") and hasClickAction()).performClick()

        // Volta para a lista, não para o modo de mudar.
        compose.onNodeWithText("Toque no produto que quer mudar.").assertDoesNotExist()
        compose.onNodeWithText("Mudar a lista").assertExists()
    }

    @Test
    fun removeShowsTheNoticeAndOpsVoltarBringsItBack() {
        show()
        row().performCustomAction("Tirar da lista")

        assertTrue(fixture.items.isEmpty())
        compose.onAllNodes(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion) and
                hasAnyDescendant(hasText("Feijão saiu da lista.")),
        ).onFirst().assertExists()

        compose.onNode(hasText("Ops, voltar") and hasClickAction()).performClick()

        row().assertExists()
        assertEquals(listOf(beans), fixture.items)
    }
}
