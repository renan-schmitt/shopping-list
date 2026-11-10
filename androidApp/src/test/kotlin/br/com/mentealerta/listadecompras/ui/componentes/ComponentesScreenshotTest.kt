package br.com.mentealerta.listadecompras.ui.componentes

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import br.com.mentealerta.listadecompras.ui.tema.ListaDeComprasTheme
import br.com.mentealerta.listadecompras.ui.tema.TemaScreenshotTest
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshots dos componentes base com a fonte do sistema em 100%, 150% e
 * 200%, no modo claro e no escuro (plano §4.6).
 *
 * Gravar: `./gradlew :androidApp:recordRoborazziDebug`
 * Conferir: `./gradlew :androidApp:verifyRoborazziDebug`
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = TemaScreenshotTest.TELA)
class ComponentesScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    @Config(fontScale = 1.0f)
    fun claro_fonte100() = capturar("claro_fonte100")

    @Test
    @Config(fontScale = 1.5f)
    fun claro_fonte150() = capturar("claro_fonte150")

    @Test
    @Config(fontScale = 2.0f)
    fun claro_fonte200() = capturar("claro_fonte200")

    @Test
    @Config(qualifiers = "+night", fontScale = 1.0f)
    fun escuro_fonte100() = capturar("escuro_fonte100")

    @Test
    @Config(qualifiers = "+night", fontScale = 1.5f)
    fun escuro_fonte150() = capturar("escuro_fonte150")

    @Test
    @Config(qualifiers = "+night", fontScale = 2.0f)
    fun escuro_fonte200() = capturar("escuro_fonte200")

    private fun capturar(nome: String) {
        compose.setContent { ListaDeComprasTheme { AmostraDosComponentes() } }
        compose.onRoot().captureRoboImage("src/test/screenshots/componentes_$nome.png")
    }
}
