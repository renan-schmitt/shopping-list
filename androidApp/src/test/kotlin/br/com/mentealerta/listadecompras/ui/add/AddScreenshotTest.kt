package br.com.mentealerta.listadecompras.ui.add

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
 * Screenshots da tela de adicionar (#53) com fonte em 100% e 200%, no modo claro e no escuro
 * (plano §4.6): produtos achados com quantidade e nome longo, e nada achado.
 *
 * Gravar: `./gradlew :androidApp:recordRoborazziDebug`
 * Conferir: `./gradlew :androidApp:verifyRoborazziDebug`
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = ThemeScreenshotTest.SCREEN)
class AddScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    @Config(fontScale = 1.0f)
    fun light_font100() = capture("light_font100")

    @Test
    @Config(fontScale = 2.0f)
    fun light_font200() = capture("light_font200")

    @Test
    @Config(qualifiers = "+night", fontScale = 1.0f)
    fun dark_font100() = capture("dark_font100")

    @Test
    @Config(qualifiers = "+night", fontScale = 2.0f)
    fun dark_font200() = capture("dark_font200")

    @Test
    @Config(fontScale = 1.0f)
    fun nothingFound_light_font100() = capture("nothing_found_light_font100", SampleAddNothingFoundState)

    private fun capture(name: String, state: AddUiState = SampleAddState) {
        compose.setContent { ShoppingListTheme { AddContent(state) } }
        compose.onRoot().captureRoboImage("src/test/screenshots/add_$name.png")
    }
}
