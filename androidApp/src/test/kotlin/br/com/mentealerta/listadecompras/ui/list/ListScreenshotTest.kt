package br.com.mentealerta.listadecompras.ui.list

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
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
 * Screenshots da tela da lista com a fonte do sistema em 100%, 150% e 200%, no modo claro e no
 * escuro (plano §4.6), comprando (com riscados, rodapé com "Terminei") e antes de riscar (só o
 * "Adicionar" grande, #52), da lista vazia, do "Está faltando?" no topo (#64) e do modo "Mudar a
 * lista" (#121).
 *
 * Gravar: `./gradlew :androidApp:recordRoborazziDebug`
 * Conferir: `./gradlew :androidApp:verifyRoborazziDebug`
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = ThemeScreenshotTest.SCREEN)
class ListScreenshotTest {

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
    fun notShopping_light_font100() = capture("not_shopping_light_font100", SampleListStateNotShopping)

    @Test
    @Config(fontScale = 2.0f)
    fun notShopping_light_font200() = capture("not_shopping_light_font200", SampleListStateNotShopping)

    @Test
    @Config(qualifiers = "+night", fontScale = 1.0f)
    fun notShopping_dark_font100() = capture("not_shopping_dark_font100", SampleListStateNotShopping)

    @Test
    @Config(qualifiers = "+night", fontScale = 2.0f)
    fun notShopping_dark_font200() = capture("not_shopping_dark_font200", SampleListStateNotShopping)

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi", fontScale = 1.0f)
    fun empty_light_font100() = capture("empty_light_font100", ListUiState.Empty)

    @Test
    @Config(fontScale = 1.0f)
    fun suggestion_light_font100() = capture("suggestion_light_font100", SampleListStateNotShopping, SampleSuggestion)

    @Test
    @Config(fontScale = 1.5f)
    fun suggestion_light_font150() = capture("suggestion_light_font150", SampleListStateNotShopping, SampleSuggestion)

    @Test
    @Config(fontScale = 2.0f)
    fun suggestion_light_font200() = capture("suggestion_light_font200", SampleListStateNotShopping, SampleSuggestion)

    @Test
    @Config(qualifiers = "+night", fontScale = 1.0f)
    fun suggestion_dark_font100() = capture("suggestion_dark_font100", SampleListStateNotShopping, SampleSuggestion)

    @Test
    @Config(qualifiers = "+night", fontScale = 2.0f)
    fun suggestion_dark_font200() = capture("suggestion_dark_font200", SampleListStateNotShopping, SampleSuggestion)

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi", fontScale = 1.0f)
    fun suggestionEmpty_light_font100() = capture("suggestion_empty_light_font100", ListUiState.Empty, SampleSuggestion)

    @Test
    @Config(fontScale = 1.0f)
    fun change_light_font100() = captureChange("light_font100")

    @Test
    @Config(fontScale = 2.0f)
    fun change_light_font200() = captureChange("light_font200")

    @Test
    @Config(qualifiers = "+night", fontScale = 1.0f)
    fun change_dark_font100() = captureChange("dark_font100")

    @Test
    @Config(qualifiers = "+night", fontScale = 2.0f)
    fun change_dark_font200() = captureChange("dark_font200")

    private fun capture(name: String, state: ListUiState = SampleListState, suggestion: SuggestionUi? = null) {
        compose.setContent { ShoppingListTheme { ListContent(state, suggestion = suggestion) } }
        compose.onRoot().captureRoboImage("src/test/screenshots/list_$name.png")
    }

    /** Modo "Mudar a lista" (#121). */
    private fun captureChange(name: String) {
        compose.setContent { ShoppingListTheme { ChangeListContent(SampleListState) } }
        compose.onRoot().captureRoboImage("src/test/screenshots/list_change_$name.png")
    }
}
