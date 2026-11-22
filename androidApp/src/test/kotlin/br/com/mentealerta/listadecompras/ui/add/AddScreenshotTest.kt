package br.com.mentealerta.listadecompras.ui.add

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import br.com.mentealerta.listadecompras.ui.list.ListRowUi
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
 * (plano §4.6): produtos achados com quantidade e nome longo, nada achado, a pergunta do produto
 * repetido (#54) e a voz (#60): explicação do microfone, escuta e recado.
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

    @Test
    @Config(fontScale = 1.0f)
    fun repeated_light_font100() = capture("repeated_light_font100", SampleAddRepeatedState)

    @Test
    @Config(fontScale = 2.0f)
    fun repeated_light_font200() = capture("repeated_light_font200", SampleAddRepeatedState)

    @Test
    @Config(qualifiers = "+night", fontScale = 1.0f)
    fun repeated_dark_font100() = capture("repeated_dark_font100", SampleAddRepeatedState)

    @Test
    @Config(fontScale = 1.0f)
    fun voiceNotice_light_font100() = capture("voice_notice_light_font100", SampleAddVoiceNoticeState)

    @Test
    @Config(fontScale = 2.0f)
    fun voiceNotice_light_font200() = capture("voice_notice_light_font200", SampleAddVoiceNoticeState)

    @Test
    @Config(qualifiers = "+night", fontScale = 1.0f)
    fun voiceNotice_dark_font100() = capture("voice_notice_dark_font100", SampleAddVoiceNoticeState)

    @Test
    @Config(fontScale = 1.0f)
    fun listening_light_font100() = capture("listening_light_font100", SampleAddListeningState)

    @Test
    @Config(fontScale = 2.0f)
    fun listening_light_font200() = capture("listening_light_font200", SampleAddListeningState)

    @Test
    @Config(qualifiers = "+night", fontScale = 1.0f)
    fun listening_dark_font100() = capture("listening_dark_font100", SampleAddListeningState)

    @Test
    @Config(fontScale = 1.0f)
    fun voiceMessage_light_font100() = capture("voice_message_light_font100", SampleAddVoiceMessageState)

    @Test
    @Config(fontScale = 2.0f)
    fun voiceMessage_light_font200() = capture("voice_message_light_font200", SampleAddVoiceMessageState)

    @Test
    @Config(fontScale = 1.0f)
    fun added_light_font100() = capture("added_light_font100", SampleAddAddedState, SampleAdded)

    @Test
    @Config(fontScale = 2.0f)
    fun added_light_font200() = capture("added_light_font200", SampleAddAddedState, SampleAdded)

    @Test
    @Config(qualifiers = "+night", fontScale = 1.0f)
    fun added_dark_font100() = capture("added_dark_font100", SampleAddAddedState, SampleAdded)

    private fun capture(name: String, state: AddUiState = SampleAddState, added: List<ListRowUi> = emptyList()) {
        compose.setContent { ShoppingListTheme { AddContent(state, added = added, canSpeak = true) } }
        compose.onRoot().captureRoboImage("src/test/screenshots/add_$name.png")
    }
}
