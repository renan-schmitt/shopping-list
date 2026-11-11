package br.com.mentealerta.listadecompras.ui.catalog

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot de todos os ícones do catálogo convertidos (#22), para conferir a conversão
 * SVG → VectorDrawable. Um só, no modo claro: o ícone não muda com a fonte nem com o tema.
 *
 * Gravar: `./gradlew :androidApp:recordRoborazziDebug`
 * Conferir: `./gradlew :androidApp:verifyRoborazziDebug`
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w411dp-h3000dp-xhdpi")
class CatalogIconsScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun allIcons() {
        compose.setContent { ShoppingListTheme(darkTheme = false) { CatalogIconsSample() } }
        compose.onRoot().captureRoboImage("src/test/screenshots/catalog_icons.png")
    }
}
