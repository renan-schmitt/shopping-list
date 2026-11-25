package br.com.mentealerta.listadecompras.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import br.com.mentealerta.listadecompras.ui.add.AddContent
import br.com.mentealerta.listadecompras.ui.add.AddUiState
import br.com.mentealerta.listadecompras.ui.add.SampleAddAddedState
import br.com.mentealerta.listadecompras.ui.add.SampleAddAlreadyOnListState
import br.com.mentealerta.listadecompras.ui.add.SampleAddListeningState
import br.com.mentealerta.listadecompras.ui.add.SampleAddNothingFoundState
import br.com.mentealerta.listadecompras.ui.add.SampleAddPastedNothingState
import br.com.mentealerta.listadecompras.ui.add.SampleAddRepeatedState
import br.com.mentealerta.listadecompras.ui.add.SampleAddReviewEditingState
import br.com.mentealerta.listadecompras.ui.add.SampleAddReviewState
import br.com.mentealerta.listadecompras.ui.add.SampleAddState
import br.com.mentealerta.listadecompras.ui.add.SampleAddVoiceMessageState
import br.com.mentealerta.listadecompras.ui.add.SampleAddVoiceNoticeState
import br.com.mentealerta.listadecompras.ui.add.SampleAdded
import br.com.mentealerta.listadecompras.ui.add.SampleUsual
import br.com.mentealerta.listadecompras.ui.add.UsualUi
import br.com.mentealerta.listadecompras.ui.edit.EditContent
import br.com.mentealerta.listadecompras.ui.edit.SampleEditChoosingSectionState
import br.com.mentealerta.listadecompras.ui.edit.SampleEditEmptyState
import br.com.mentealerta.listadecompras.ui.edit.SampleEditState
import br.com.mentealerta.listadecompras.ui.fullversion.FullVersionContent
import br.com.mentealerta.listadecompras.ui.fullversion.SampleFullVersionBusyState
import br.com.mentealerta.listadecompras.ui.fullversion.SampleFullVersionCompleteState
import br.com.mentealerta.listadecompras.ui.fullversion.SampleFullVersionMessageState
import br.com.mentealerta.listadecompras.ui.fullversion.SampleFullVersionState
import br.com.mentealerta.listadecompras.ui.history.HistoryContent
import br.com.mentealerta.listadecompras.ui.history.HistoryUiState
import br.com.mentealerta.listadecompras.ui.history.SampleHistoryState
import br.com.mentealerta.listadecompras.ui.list.ChangeListContent
import br.com.mentealerta.listadecompras.ui.list.ClearListQuestion
import br.com.mentealerta.listadecompras.ui.list.ListContent
import br.com.mentealerta.listadecompras.ui.list.ListUiState
import br.com.mentealerta.listadecompras.ui.list.SampleListState
import br.com.mentealerta.listadecompras.ui.list.SampleListStateNotShopping
import br.com.mentealerta.listadecompras.ui.list.SampleSuggestion
import br.com.mentealerta.listadecompras.ui.list.WeekNoticeSample
import br.com.mentealerta.listadecompras.ui.settings.SettingsContent
import br.com.mentealerta.listadecompras.ui.settings.SettingsUiState
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
        private fun add(
            state: AddUiState,
            withAdded: Boolean = false,
            canPaste: Boolean = false,
            usual: List<UsualUi> = emptyList(),
        ): @Composable () -> Unit = {
            AddContent(
                state,
                added = if (withAdded) SampleAdded else emptyList(),
                usual = usual,
                canSpeak = true,
                canPaste = canPaste,
            )
        }

        private val Screens: Map<String, @Composable () -> Unit> = linkedMapOf(
            "lista comprando" to { ListContent(SampleListState) },
            "lista antes de comprar" to { ListContent(SampleListStateNotShopping) },
            "lista vazia" to { ListContent(ListUiState.Empty) },
            "lista com histórico" to { ListContent(SampleListStateNotShopping, hasHistory = true) },
            "lista com está faltando" to { ListContent(SampleListStateNotShopping, suggestion = SampleSuggestion) },
            "lista vazia com está faltando" to { ListContent(ListUiState.Empty, suggestion = SampleSuggestion) },
            "lista vazia com histórico" to { ListContent(ListUiState.Empty, hasHistory = true) },
            "lista com versão completa" to {
                ListContent(SampleListStateNotShopping, hasHistory = true, canBuyFullVersion = true)
            },
            "lista vazia com versão completa" to {
                ListContent(ListUiState.Empty, hasHistory = true, canBuyFullVersion = true)
            },
            "lista vazia com ajustes" to {
                ListContent(ListUiState.Empty, hasHistory = true, canBuyFullVersion = true, hasSettings = true)
            },
            "ajustes" to { SettingsContent(SettingsUiState.Content(personalizedAds = true)) },
            "ajustes desligada" to { SettingsContent(SettingsUiState.Content(personalizedAds = false)) },
            "aviso do fim da primeira semana" to { WeekNoticeSample() },
            "versão completa" to { FullVersionContent(SampleFullVersionState) },
            "versão completa com recado" to { FullVersionContent(SampleFullVersionMessageState) },
            "versão completa esperando a loja" to { FullVersionContent(SampleFullVersionBusyState) },
            "versão completa comprada" to { FullVersionContent(SampleFullVersionCompleteState) },
            "o que já comprei" to { HistoryContent(SampleHistoryState) },
            "o que já comprei vazio" to { HistoryContent(HistoryUiState.Empty()) },
            "o que já comprei com compras antigas" to { HistoryContent(SampleHistoryState.copy(hasOlder = true)) },
            "o que já comprei só com compras antigas" to { HistoryContent(HistoryUiState.Empty(hasOlder = true)) },
            "mudar a lista" to { ChangeListContent(SampleListState) },
            "jogar a lista fora" to { ClearListQuestion(total = 12, onConfirm = {}, onDismiss = {}) },
            "edição" to { EditContent(SampleEditState) },
            "edição sem quantidade" to { EditContent(SampleEditEmptyState) },
            "edição escolhendo a seção" to { EditContent(SampleEditChoosingSectionState) },
            "adicionar buscando" to add(SampleAddState),
            "adicionar nada achado" to add(SampleAddNothingFoundState),
            "adicionar repetido" to add(SampleAddRepeatedState),
            "adicionar adicionados" to add(SampleAddAddedState, withAdded = true),
            "adicionar você costuma comprar" to add(SampleAddAddedState, withAdded = true, usual = SampleUsual),
            "adicionar já na lista" to add(SampleAddAlreadyOnListState, withAdded = true),
            "adicionar aviso da voz" to add(SampleAddVoiceNoticeState),
            "adicionar ouvindo" to add(SampleAddListeningState),
            "adicionar recado da voz" to add(SampleAddVoiceMessageState),
            "adicionar colar a lista" to add(SampleAddAddedState, withAdded = true, canPaste = true),
            "adicionar colado sem produto" to add(SampleAddPastedNothingState, canPaste = true),
            "adicionar conferência" to add(SampleAddReviewState),
            "adicionar conferência mudando" to add(SampleAddReviewEditingState),
        )

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}, fonte {1}")
        fun parameters(): List<Array<Any>> = Screens.keys.flatMap { screen ->
            listOf(1.0f, 2.0f).map { arrayOf(screen, it) }
        }
    }
}
