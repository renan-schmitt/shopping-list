package br.com.mentealerta.listadecompras.ui.fullversion

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
 * Screenshots da "Versão completa" (#88) com a fonte do sistema em 100%, 150% e 200%, no modo claro e
 * no escuro (plano §4.6), com recado, com a versão completa e do botão que abre a tela, na lista e na
 * lista vazia. A tela é tirada com a altura toda, para mostrar os botões do fim.
 *
 * Gravar: `./gradlew :androidApp:recordRoborazziDebug`
 * Conferir: `./gradlew :androidApp:verifyRoborazziDebug`
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = ThemeScreenshotTest.SCREEN)
class FullVersionScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    @Config(qualifiers = TALL, fontScale = 1.0f)
    fun light_font100() = capture("light_font100")

    @Test
    @Config(qualifiers = TALL, fontScale = 1.5f)
    fun light_font150() = capture("light_font150")

    @Test
    @Config(qualifiers = TALLER, fontScale = 2.0f)
    fun light_font200() = capture("light_font200")

    @Test
    @Config(qualifiers = TALL_NIGHT, fontScale = 1.0f)
    fun dark_font100() = capture("dark_font100")

    @Test
    @Config(qualifiers = TALL_NIGHT, fontScale = 1.5f)
    fun dark_font150() = capture("dark_font150")

    @Test
    @Config(qualifiers = TALLER_NIGHT, fontScale = 2.0f)
    fun dark_font200() = capture("dark_font200")

    @Test
    @Config(qualifiers = TALL, fontScale = 1.0f)
    fun message_light_font100() = capture("message_light_font100", SampleFullVersionMessageState)

    @Test
    @Config(qualifiers = TALL, fontScale = 1.0f)
    fun busy_light_font100() = capture("busy_light_font100", SampleFullVersionBusyState)

    @Test
    @Config(qualifiers = TALL, fontScale = 1.0f)
    fun complete_light_font100() = capture("complete_light_font100", SampleFullVersionCompleteState)

    @Test
    @Config(qualifiers = TALL_NIGHT, fontScale = 1.0f)
    fun complete_dark_font100() = capture("complete_dark_font100", SampleFullVersionCompleteState)

    /** O fim da folha, com "O que já comprei" e "Versão completa": a folha é tirada com a altura toda. */
    @Test
    @Config(qualifiers = "w411dp-h1700dp-xhdpi", fontScale = 1.0f)
    fun listButton_light_font100() = captureList("list_button_light_font100", SampleListStateNotShopping)

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi", fontScale = 1.0f)
    fun emptyListButton_light_font100() = captureList("empty_list_button_light_font100", ListUiState.Empty)

    @Test
    @Config(qualifiers = "w411dp-h891dp-night-xhdpi", fontScale = 1.0f)
    fun emptyListButton_dark_font100() = captureList("empty_list_button_dark_font100", ListUiState.Empty)

    private fun capture(name: String, state: FullVersionUiState = SampleFullVersionState) {
        compose.setContent { ShoppingListTheme { FullVersionContent(state) } }
        compose.onRoot().captureRoboImage("src/test/screenshots/full_version_$name.png")
    }

    private fun captureList(name: String, state: ListUiState) {
        compose.setContent { ShoppingListTheme { ListContent(state, hasHistory = true, canBuyFullVersion = true) } }
        compose.onRoot().captureRoboImage("src/test/screenshots/full_version_$name.png")
    }

    private companion object {
        const val TALL = "w411dp-h1400dp-xhdpi"
        const val TALL_NIGHT = "w411dp-h1400dp-night-xhdpi"
        const val TALLER = "w411dp-h2400dp-xhdpi"
        const val TALLER_NIGHT = "w411dp-h2400dp-night-xhdpi"
    }
}
