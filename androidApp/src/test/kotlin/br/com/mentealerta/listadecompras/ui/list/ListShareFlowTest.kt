package br.com.mentealerta.listadecompras.ui.list

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.ui.edit.EditFixture
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme
import br.com.mentealerta.listadecompras.ui.theme.ThemeScreenshotTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * "Mandar a lista" (#67): no fim da folha, só com produtos a comprar; o toque abre a tela de
 * compartilhar do Android com o texto da lista.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = ThemeScreenshotTest.SCREEN)
class ListShareFlowTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val fixture = EditFixture()

    private fun show() {
        val viewModel = fixture.listViewModel()
        compose.setContent { ShoppingListTheme(darkTheme = false) { ListScreen(viewModel = viewModel) } }
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Mudar a lista"))
    }

    private fun shareButton() = compose.onNode(hasText("Mandar a lista") and hasClickAction())

    @Test
    fun sendsWhatIsLeftThroughTheShareSheet() {
        fixture.put(ListItem(ListItemId("1"), "Banana", "banana", "produce"))
        fixture.put(ListItem(ListItemId("2"), "Feijão", "beans", "pantry", checked = true))
        show()
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Mandar a lista"))

        shareButton().performClick()

        val chooser = shadowOf(compose.activity).nextStartedActivity
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        @Suppress("DEPRECATION")
        val send = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
        assertEquals(Intent.ACTION_SEND, send.action)
        assertEquals("text/plain", send.type)
        assertEquals("*Lista de compras*\n\n*Hortifrúti*\n- Banana", send.getStringExtra(Intent.EXTRA_TEXT))
    }

    @Test
    fun withEverythingPickedThereIsNothingToSend() {
        fixture.put(ListItem(ListItemId("1"), "Banana", "banana", "produce", checked = true))
        show()
        shareButton().assertDoesNotExist()
    }
}
