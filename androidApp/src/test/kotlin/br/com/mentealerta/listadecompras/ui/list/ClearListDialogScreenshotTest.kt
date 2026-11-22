package br.com.mentealerta.listadecompras.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshots da confirmação do "Jogar a lista fora" (#58) com a fonte do sistema em 100%, 150% e
 * 200%, no modo claro e no escuro (plano §4.6). Só a caixa, sem a janela do diálogo, que o
 * Roborazzi não captura junto com a tela.
 *
 * Gravar: `./gradlew :androidApp:recordRoborazziDebug`
 * Conferir: `./gradlew :androidApp:verifyRoborazziDebug`
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w411dp-h891dp-xhdpi")
class ClearListDialogScreenshotTest {

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

    private fun capture(name: String) {
        compose.setContent {
            ShoppingListTheme {
                // Fundo escurecido, como o diálogo aparece por cima da lista.
                Box(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center,
                ) {
                    ClearListQuestion(total = 12, onConfirm = {}, onDismiss = {}, modifier = Modifier.padding(16.dp))
                }
            }
        }
        compose.onRoot().captureRoboImage("src/test/screenshots/clear_list_$name.png")
    }
}
