package br.com.mentealerta.listadecompras.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import br.com.mentealerta.listadecompras.ui.add.AddContent
import br.com.mentealerta.listadecompras.ui.add.AddUiState
import br.com.mentealerta.listadecompras.ui.add.SampleAddAddedState
import br.com.mentealerta.listadecompras.ui.add.SampleAddAlreadyOnListState
import br.com.mentealerta.listadecompras.ui.add.SampleAddListeningState
import br.com.mentealerta.listadecompras.ui.add.SampleAddNothingFoundState
import br.com.mentealerta.listadecompras.ui.add.SampleAddRepeatedState
import br.com.mentealerta.listadecompras.ui.add.SampleAddState
import br.com.mentealerta.listadecompras.ui.add.SampleAddVoiceMessageState
import br.com.mentealerta.listadecompras.ui.add.SampleAddVoiceNoticeState
import br.com.mentealerta.listadecompras.ui.add.SampleAdded
import br.com.mentealerta.listadecompras.ui.edit.EditContent
import br.com.mentealerta.listadecompras.ui.edit.SampleEditEmptyState
import br.com.mentealerta.listadecompras.ui.edit.SampleEditState
import br.com.mentealerta.listadecompras.ui.list.ChangeListContent
import br.com.mentealerta.listadecompras.ui.list.ClearListQuestion
import br.com.mentealerta.listadecompras.ui.list.ListContent
import br.com.mentealerta.listadecompras.ui.list.ListUiState
import br.com.mentealerta.listadecompras.ui.list.SampleListState
import br.com.mentealerta.listadecompras.ui.list.SampleListStateNotShopping
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme
import br.com.mentealerta.listadecompras.ui.theme.ThemeScreenshotTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Auditoria de acessibilidade (#71): as regras de [accessibilityProblems] em todas as telas e
 * estados, com a fonte do sistema em 100% e 200% (plano §1.7).
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = ThemeScreenshotTest.SCREEN)
class ScreensAccessibilityTest(private val screen: String, private val fontScale: Float) {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun screenFollowsTheAccessibilityRules() {
        RuntimeEnvironment.setFontScale(fontScale)
        val content = Screens.getValue(screen)
        compose.setContent { ShoppingListTheme(darkTheme = false) { content() } }
        val problems = compose.accessibilityProblems()
        assertTrue(
            "$screen com fonte em ${(fontScale * 100).toInt()}%:\n${problems.joinToString("\n")}",
            problems.isEmpty(),
        )
    }

    companion object {
        private fun add(state: AddUiState, withAdded: Boolean = false): @Composable () -> Unit = {
            AddContent(state, added = if (withAdded) SampleAdded else emptyList(), canSpeak = true)
        }

        private val Screens: Map<String, @Composable () -> Unit> = linkedMapOf(
            "lista comprando" to { ListContent(SampleListState) },
            "lista antes de comprar" to { ListContent(SampleListStateNotShopping) },
            "lista vazia" to { ListContent(ListUiState.Empty) },
            "mudar a lista" to { ChangeListContent(SampleListState) },
            "jogar a lista fora" to { ClearListQuestion(total = 12, onConfirm = {}, onDismiss = {}) },
            "edição" to { EditContent(SampleEditState) },
            "edição sem quantidade" to { EditContent(SampleEditEmptyState) },
            "adicionar buscando" to add(SampleAddState),
            "adicionar nada achado" to add(SampleAddNothingFoundState),
            "adicionar repetido" to add(SampleAddRepeatedState),
            "adicionar adicionados" to add(SampleAddAddedState, withAdded = true),
            "adicionar já na lista" to add(SampleAddAlreadyOnListState, withAdded = true),
            "adicionar aviso da voz" to add(SampleAddVoiceNoticeState),
            "adicionar ouvindo" to add(SampleAddListeningState),
            "adicionar recado da voz" to add(SampleAddVoiceMessageState),
        )

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}, fonte {1}")
        fun parameters(): List<Array<Any>> = Screens.keys.flatMap { screen ->
            listOf(1.0f, 2.0f).map { arrayOf(screen, it) }
        }
    }
}
