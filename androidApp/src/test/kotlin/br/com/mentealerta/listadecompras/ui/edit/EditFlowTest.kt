package br.com.mentealerta.listadecompras.ui.edit

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.ui.list.ListScreen
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
 * Fluxo da edição pela tela da lista (#55): "Mudar a lista" (#121), tocar no produto, mudar nome,
 * quantidade e detalhe, voltar (tudo salvo sozinho); tirar da lista, ver o aviso e voltar com
 * "Desfazer". E a acessibilidade da tela: campos com rótulo, botões de 64dp, "Menos" desativado em 1 e
 * a quantidade lida pelo TalkBack.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = ThemeScreenshotTest.SCREEN)
class EditFlowTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val fixture = EditFixture()
    private val beans = fixture.put(ListItem(ListItemId("1"), "Feijão", "beans", "pantry"))

    private fun showAndOpenBeans() {
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
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Mudar a lista"))
        button("Mudar a lista").performClick()
        compose.onNodeWithText("Feijão").performClick()
    }

    /** Campo pelo rótulo; vazio, o TalkBack ouve também o exemplo depois dele. */
    private fun field(label: String) =
        compose.onNode(hasSetTextAction() and hasContentDescription(label, substring = true))

    private fun button(text: String) = compose.onNode(hasText(text) and hasClickAction())

    @Test
    fun editingIsSavedWhenGoingBackToTheList() {
        showAndOpenBeans()
        field("Nome").assert(hasText("Feijão")).performTextReplacement("Feijão preto")
        button("Mais").performClick()
        button("Mais").performClick()
        field("Detalhe").performTextReplacement("tipo 1")
        // Não existe "Salvar".
        compose.onAllNodesWithText("Salvar", substring = true, ignoreCase = true).assertCountEquals(0)

        button("Voltar para a lista").performClick()

        // Volta para o modo de mudar, já com o produto mudado.
        compose.onNodeWithText("Toque no produto que quer mudar.").assertExists()
        compose.onNodeWithText("Feijão preto tipo 1, 3").assertExists()
        assertEquals(
            beans.copy(name = "Feijão preto", quantity = Quantity(3.0), detail = "tipo 1"),
            fixture.items.single(),
        )
    }

    @Test
    fun phoneBackAlsoSaves() {
        showAndOpenBeans()
        button("Mais").performClick()
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("Feijão, 2").assertExists()
        // De novo, sai do modo de mudar e volta para a lista.
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("Toque no produto que quer mudar.").assertDoesNotExist()
        compose.onNodeWithText("Feijão, 2").assertExists()
    }

    @Test
    fun removingShowsTheNoticeAndUndoBringsItBack() {
        showAndOpenBeans()
        button("Tirar da lista").performScrollTo().performClick()

        assertTrue(fixture.items.isEmpty())
        // O aviso inteiro é anunciado pelo TalkBack quando aparece.
        compose.onAllNodes(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion) and
                hasAnyDescendant(hasText("Feijão saiu da lista.")),
        ).onFirst().assertExists()
        button("Desfazer").performClick()

        compose.onNodeWithText("Feijão").assertExists()
        assertEquals(listOf(beans), fixture.items)
        compose.onAllNodesWithText("Feijão saiu da lista.").assertCountEquals(0)
    }

    @Test
    fun openingAnotherProductClosesTheNoticeForGood() {
        fixture.put(ListItem(ListItemId("2"), "Arroz", "rice", "pantry"))
        showAndOpenBeans()
        button("Tirar da lista").performScrollTo().performClick()
        compose.onNodeWithText("Feijão saiu da lista.").assertExists()

        // O aviso não aparece de novo ao voltar da edição de outro produto.
        compose.onNodeWithText("Arroz").performClick()
        button("Voltar para a lista").performClick()

        compose.onNodeWithText("Toque no produto que quer mudar.").assertExists()
        compose.onAllNodesWithText("Feijão saiu da lista.").assertCountEquals(0)
    }

    @Test
    fun quantityButtonsAreLargeAndMinusStartsDisabled() {
        showAndOpenBeans()
        compose.onNodeWithText("Quantidade").assert(isHeading())
        button("Menos").assertIsNotEnabled().assertHeightIsAtLeast(64.dp)
        button("Mais").assertIsEnabled().assertHeightIsAtLeast(64.dp).performClick()
        button("Menos").assertIsEnabled()
        compose.onNodeWithText("2").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion))
    }

    @Test
    fun fieldsAreLabeledForTalkBack() {
        showAndOpenBeans()
        compose.onNode(hasSetTextAction() and hasContentDescription("Nome")).assertExists()
        // Vazio, o detalhe é lido com o exemplo que aparece dentro dele; preenchido, só o rótulo.
        compose.onNode(hasSetTextAction() and hasContentDescription("Detalhe, Ex.: desnatado, da marca X"))
            .performTextReplacement("tipo 1")
        compose.onNode(hasSetTextAction() and hasContentDescription("Detalhe")).assertExists()
        button("Tirar da lista").performScrollTo().assertHeightIsAtLeast(64.dp)
        // Rótulos em caixa alta só na tela; o TalkBack ouve "Nome", em caixa normal.
        compose.onAllNodesWithText("NOME").assertCountEquals(0)
    }

    @Test
    @Config(fontScale = 2.0f)
    fun everythingIsReachableWithLargeFont() {
        showAndOpenBeans()
        button("Mais").performScrollTo().performClick()
        button("Tirar da lista").performScrollTo().performClick()
        compose.onNodeWithText("Feijão saiu da lista.").assertExists()
    }

    @Test
    fun changingTheSectionMovesTheProductOnTheList() {
        showAndOpenBeans()
        button("Mudar de seção").performScrollTo().performClick()
        compose.onNode(hasText("Em qual seção fica?") and isHeading()).assertExists()
        // A seção atual é marcada com "Está aqui", não só pela cor.
        compose.onNode(hasText("Mercearia") and hasAnyDescendant(hasText("Está aqui")), useUnmergedTree = true)
            .assertDoesNotExist()
        compose.onNode(SemanticsMatcher.expectValue(SemanticsProperties.Selected, true))
            .assert(hasText("Mercearia"))
            .assert(hasText("Está aqui"))

        compose.onNode(SemanticsMatcher.expectValue(SemanticsProperties.Selected, false) and hasText("Hortifrúti"))
            .assertHeightIsAtLeast(64.dp)
            .performClick()

        // Volta para a edição, com a seção nova.
        button("Mudar de seção").performScrollTo()
        compose.onNodeWithText("Hortifrúti").assertExists()
        button("Voltar para a lista").performClick()

        assertEquals("produce", fixture.items.single().sectionId)
        assertEquals(mapOf("beans" to "produce"), fixture.sectionChoices.sections)
    }

    @Test
    fun phoneBackOnTheChoiceGoesBackToTheProduct() {
        showAndOpenBeans()
        button("Mudar de seção").performScrollTo().performClick()

        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }

        compose.onNodeWithText("Em qual seção fica?").assertDoesNotExist()
        button("Mudar de seção").assertExists()
        assertEquals("pantry", fixture.items.single().sectionId)
    }

    @Test
    @Config(fontScale = 2.0f)
    fun sectionChoiceIsReachableWithLargeFont() {
        showAndOpenBeans()
        button("Mudar de seção").performScrollTo().performClick()
        compose.onNode(hasText("Outros") and hasClickAction()).performScrollTo().performClick()
        button("Voltar para a lista").performClick()
        assertEquals("other", fixture.items.single().sectionId)
    }
}
