package br.com.mentealerta.listadecompras.ui.list

import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme
import br.com.mentealerta.listadecompras.ui.theme.ThemeScreenshotTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Tela que se adapta (#52): antes de riscar, só o "Adicionar" grande; depois do primeiro risco, o
 * "Terminei" grande com quantos faltam, o "Adicionar" menor e a tela acesa. Sem riscados (depois do
 * "Terminei"), volta ao estado inicial.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = ThemeScreenshotTest.SCREEN)
class ListFooterTest {

    @get:Rule
    val compose = createComposeRule()

    private var adds = 0
    private var finishes = 0
    private lateinit var view: View

    private fun show(state: ListUiState) {
        compose.setContent {
            view = LocalView.current
            ShoppingListTheme(darkTheme = false) {
                ListContent(state, onAdd = { adds++ }, onFinish = { finishes++ })
            }
        }
    }

    private fun stateWith(vararg checked: Boolean) = ListUiState.Content(
        listOf(
            SectionUi(
                id = "produce",
                name = "Hortifrúti",
                rows = checked.mapIndexed { i, isChecked ->
                    ListRowUi(
                        id = ListItemId("$i"),
                        name = "Produto $i",
                        checked = isChecked,
                        icon = R.drawable.ic_add,
                    )
                },
            ),
        ),
    )

    private fun assertNoFinish() = compose.onAllNodesWithText("Terminei").assertCountEquals(0)

    @Test
    fun beforeCheckingShowsOnlyABigAddButton() {
        show(SampleListStateNotShopping)
        compose.onNodeWithText("Adicionar").assertHeightIsAtLeast(88.dp)
        assertNoFinish()
        assertFalse(view.keepScreenOn)
    }

    @Test
    fun emptyListShowsABigAddButton() {
        show(ListUiState.Empty)
        compose.onNodeWithText("Adicionar").assertHeightIsAtLeast(88.dp)
        assertNoFinish()
        assertFalse(view.keepScreenOn)
    }

    @Test
    fun loadingShowsNoButtons() {
        show(ListUiState.Loading)
        compose.onAllNodesWithText("Adicionar").assertCountEquals(0)
        assertNoFinish()
    }

    @Test
    fun afterCheckingShowsFinishWithWhatIsLeftAndASmallerAdd() {
        show(SampleListState)
        // Os textos do botão se juntam num nó só, lido em caixa normal pelo TalkBack.
        compose.onNode(hasText("Terminei") and hasText("Faltam 7") and hasClickAction())
            .assertHeightIsAtLeast(88.dp)
        compose.onNodeWithText("Adicionar").assertHeightIsAtLeast(64.dp)
        compose.onAllNodesWithText("TERMINEI").assertCountEquals(0)
        assertTrue(view.keepScreenOn)
    }

    @Test
    fun remainingUsesSingularAndNothingLeft() {
        var state by mutableStateOf(stateWith(true, false))
        compose.setContent { ShoppingListTheme(darkTheme = false) { ListContent(state) } }
        compose.onNodeWithText("Falta 1").assertExists()

        state = stateWith(true, true)
        compose.onNodeWithText("Não falta nada").assertExists()
    }

    @Test
    fun buttonsCallTheirActions() {
        show(SampleListState)
        compose.onNodeWithText("Terminei").performClick()
        compose.onNodeWithText("Adicionar").performClick()
        assertEquals(1, finishes)
        assertEquals(1, adds)
    }

    @Test
    fun screenStaysOnOnlyWhileShoppingAndGoesBackAfterFinishing() {
        var state: ListUiState by mutableStateOf(SampleListStateNotShopping)
        compose.setContent {
            view = LocalView.current
            ShoppingListTheme(darkTheme = false) { ListContent(state) }
        }
        assertFalse(view.keepScreenOn)

        state = SampleListState
        compose.waitForIdle()
        assertTrue(view.keepScreenOn)
        compose.onNodeWithText("Terminei").assertExists()

        // Depois do "Terminei" os riscados saem: volta ao estado inicial.
        state = stateWith(false, false)
        compose.waitForIdle()
        assertFalse(view.keepScreenOn)
        assertNoFinish()
        compose.onNodeWithText("Adicionar").assertHeightIsAtLeast(88.dp)
    }

    @Test
    @Config(fontScale = 2.0f)
    fun buttonsKeepTheirSizeWithLargeFont() {
        show(SampleListState)
        compose.onNodeWithText("Terminei").assertHeightIsAtLeast(88.dp)
        compose.onNodeWithText("Adicionar").assertHeightIsAtLeast(64.dp)
    }
}
