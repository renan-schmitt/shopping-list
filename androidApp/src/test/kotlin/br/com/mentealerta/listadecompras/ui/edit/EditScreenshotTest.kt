package br.com.mentealerta.listadecompras.ui.edit

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
 * Screenshots da edição do produto (#55) com fonte em 100% e 200%, no modo claro e no escuro
 * (plano §4.6), e o produto sem quantidade nem detalhe.
 *
 * Gravar: `./gradlew :androidApp:recordRoborazziDebug`
 * Conferir: `./gradlew :androidApp:verifyRoborazziDebug`
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = ThemeScreenshotTest.SCREEN)
class EditScreenshotTest {

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
    fun empty_light_font100() = capture("empty_light_font100", SampleEditEmptyState)

    private fun capture(name: String, state: EditUiState = SampleEditState) {
        compose.setContent { ShoppingListTheme { EditContent(state) } }
        compose.onRoot().captureRoboImage("src/test/screenshots/edit_$name.png")
    }
}
