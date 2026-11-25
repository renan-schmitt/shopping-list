package br.com.mentealerta.listadecompras.ui.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import br.com.mentealerta.listadecompras.domain.ObservePersonalizedAdsUseCase
import br.com.mentealerta.listadecompras.domain.SetPersonalizedAdsUseCase
import br.com.mentealerta.listadecompras.ui.edit.EditFixture
import br.com.mentealerta.listadecompras.ui.list.ListScreen
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme
import br.com.mentealerta.listadecompras.ui.theme.ThemeScreenshotTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * "Ajustes" (#89) pela tela da lista: o botão aparece só na versão grátis; a chave "Anúncios pelo meu
 * interesse" vem ligada e desliga tocando na linha; o voltar do celular fecha a tela.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = ThemeScreenshotTest.SCREEN)
class SettingsFlowTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val fixture = EditFixture()
    private val preferences = fixture.ads.preferences

    private fun show() {
        val listViewModel = fixture.listViewModel()
        val settingsViewModel = SettingsViewModel(
            ObservePersonalizedAdsUseCase(preferences),
            SetPersonalizedAdsUseCase(preferences),
        )
        compose.setContent {
            ShoppingListTheme(darkTheme = false) {
                ListScreen(
                    viewModel = listViewModel,
                    settingsScreen = { onClose -> SettingsScreen(onClose, viewModel = settingsViewModel) },
                )
            }
        }
    }

    private fun button(text: String) = compose.onNode(hasText(text) and hasClickAction())

    private val title get() = compose.onNode(hasText("Ajustes") and isHeading())

    private val adsSwitch get() = compose.onNode(isToggleable() and hasText("Anúncios pelo meu interesse"))

    @Test
    fun turningOffPersonalizedAds() {
        show()

        button("Ajustes").performClick()
        title.assertExists()
        adsSwitch.assertIsOn()

        adsSwitch.performClick()

        adsSwitch.assertIsOff()
        assertFalse(preferences.current.personalized)
        adsSwitch.performClick()
        assertTrue(preferences.current.personalized)
    }

    @Test
    fun phoneBackCloses() {
        show()
        button("Ajustes").performClick()
        title.assertExists()

        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }

        title.assertDoesNotExist()
        button("Ajustes").assertExists()
    }

    @Test
    fun withTheFullVersionThereIsNoButton() {
        fixture.premium.isPremium.value = true
        show()

        button("Ajustes").assertDoesNotExist()
    }
}
