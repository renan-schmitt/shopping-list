package br.com.mentealerta.listadecompras.ui.history

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import br.com.mentealerta.listadecompras.ui.list.ListContent
import br.com.mentealerta.listadecompras.ui.list.ListUiState
import br.com.mentealerta.listadecompras.ui.list.SampleListStateNotShopping
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme
import br.com.mentealerta.listadecompras.ui.theme.ThemeScreenshotTest
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshots do "O que já comprei" (#66) com a fonte do sistema em 100%, 150% e 200%, no modo claro
 * e no escuro (plano §4.6), do histórico vazio e do botão que abre o histórico, na lista e na lista
 * vazia.
 *
 * Gravar: `./gradlew :androidApp:recordRoborazziDebug`
 * Conferir: `./gradlew :androidApp:verifyRoborazziDebug`
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = ThemeScreenshotTest.SCREEN)
class HistoryScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    @Config(fontScale = 1.0f)
    fun light_font100() = capture("light_font100")

    @Test
    @Config(fontScale = 1.5f)
    fun light_font150() = capture("light_font150")

    @Test
    @Config(fontScale = 2.0f)
    fun light_font200() = capture("light_font200")

    @Test
    @Config(qualifiers = "+night", fontScale = 1.0f)
    fun dark_font100() = capture("dark_font100")

    @Test
    @Config(qualifiers = "+night", fontScale = 1.5f)
    fun dark_font150() = capture("dark_font150")

    @Test
    @Config(qualifiers = "+night", fontScale = 2.0f)
    fun dark_font200() = capture("dark_font200")

    @Test
    @Config(fontScale = 1.0f)
    fun empty_light_font100() = capture("empty_light_font100", HistoryUiState.Empty)

    @Test
    @Config(qualifiers = "+night", fontScale = 1.0f)
    fun empty_dark_font100() = capture("empty_dark_font100", HistoryUiState.Empty)

    /** O fim da folha, com "Mudar a lista" e "O que já comprei": a folha é tirada com a altura toda. */
    @Test
    @Config(qualifiers = "w411dp-h1600dp-xhdpi", fontScale = 1.0f)
    fun listButton_light_font100() = captureList("list_button_light_font100", SampleListStateNotShopping)

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi", fontScale = 1.0f)
    fun emptyListButton_light_font100() = captureList("empty_list_button_light_font100", ListUiState.Empty)

    @Test
    @Config(qualifiers = "w411dp-h891dp-night-xhdpi", fontScale = 1.0f)
    fun emptyListButton_dark_font100() = captureList("empty_list_button_dark_font100", ListUiState.Empty)

    private fun capture(name: String, state: HistoryUiState = SampleHistoryState) {
        compose.setContent { ShoppingListTheme { HistoryContent(state) } }
        compose.onRoot().captureRoboImage("src/test/screenshots/history_$name.png")
    }

    private fun captureList(name: String, state: ListUiState) {
        compose.setContent { ShoppingListTheme { ListContent(state, hasHistory = true) } }
        compose.onRoot().captureRoboImage("src/test/screenshots/history_$name.png")
    }
}
