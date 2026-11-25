package br.com.mentealerta.listadecompras.ui.settings

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import br.com.mentealerta.listadecompras.ui.list.ListContent
import br.com.mentealerta.listadecompras.ui.list.ListUiState
import br.com.mentealerta.listadecompras.ui.list.WeekNoticeSample
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
 * Screenshots dos "Ajustes" e do aviso do fim da primeira semana (#89) com a fonte do sistema em 100%,
 * 150% e 200%, no modo claro e no escuro (plano §4.6), e do botão "Ajustes" na lista vazia.
 *
 * Gravar: `./gradlew :androidApp:recordRoborazziDebug`
 * Conferir: `./gradlew :androidApp:verifyRoborazziDebug`
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = ThemeScreenshotTest.SCREEN)
class SettingsScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    @Config(fontScale = 1.0f)
    fun light_font100() = capture("settings_light_font100")

    @Test
    @Config(fontScale = 1.5f)
    fun light_font150() = capture("settings_light_font150")

    @Test
    @Config(fontScale = 2.0f)
    fun light_font200() = capture("settings_light_font200")

    @Test
    @Config(qualifiers = NIGHT, fontScale = 1.0f)
    fun dark_font100() = capture("settings_dark_font100")

    @Test
    @Config(qualifiers = NIGHT, fontScale = 2.0f)
    fun dark_font200() = capture("settings_dark_font200")

    @Test
    @Config(fontScale = 1.0f)
    fun off_light_font100() = capture("settings_off_light_font100", personalized = false)

    @Test
    @Config(qualifiers = NIGHT, fontScale = 1.0f)
    fun off_dark_font100() = capture("settings_off_dark_font100", personalized = false)

    @Test
    @Config(fontScale = 1.0f)
    fun weekNotice_light_font100() = captureWeekNotice("week_notice_light_font100")

    @Test
    @Config(fontScale = 1.5f)
    fun weekNotice_light_font150() = captureWeekNotice("week_notice_light_font150")

    @Test
    @Config(fontScale = 2.0f)
    fun weekNotice_light_font200() = captureWeekNotice("week_notice_light_font200")

    @Test
    @Config(qualifiers = NIGHT, fontScale = 1.0f)
    fun weekNotice_dark_font100() = captureWeekNotice("week_notice_dark_font100")

    @Test
    @Config(qualifiers = NIGHT, fontScale = 2.0f)
    fun weekNotice_dark_font200() = captureWeekNotice("week_notice_dark_font200")

    @Test
    @Config(fontScale = 1.0f)
    fun emptyListButton_light_font100() {
        compose.setContent {
            ShoppingListTheme {
                ListContent(ListUiState.Empty, hasHistory = true, canBuyFullVersion = true, hasSettings = true)
            }
        }
        compose.onRoot().captureRoboImage("src/test/screenshots/settings_empty_list_button_light_font100.png")
    }

    private fun capture(name: String, personalized: Boolean = true) {
        compose.setContent { ShoppingListTheme { SettingsContent(SettingsUiState.Content(personalized)) } }
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }

    private fun captureWeekNotice(name: String) {
        compose.setContent { ShoppingListTheme { WeekNoticeSample() } }
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }

    private companion object {
        const val NIGHT = "+night"
    }
}
