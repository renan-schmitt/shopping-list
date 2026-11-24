package br.com.mentealerta.listadecompras.ui.fullversion

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import br.com.mentealerta.listadecompras.domain.BuyPremiumUseCase
import br.com.mentealerta.listadecompras.domain.GetPremiumPriceUseCase
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.domain.ObservePremiumUseCase
import br.com.mentealerta.listadecompras.domain.RestorePremiumUseCase
import br.com.mentealerta.listadecompras.premium.RestoreResult
import br.com.mentealerta.listadecompras.ui.edit.EditFixture
import br.com.mentealerta.listadecompras.ui.list.ListScreen
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme
import br.com.mentealerta.listadecompras.ui.theme.ThemeScreenshotTest
import kotlinx.coroutines.CompletableDeferred
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * "Versão completa" (#88) pela tela da lista: o botão aparece só na versão grátis, no fim da folha e na
 * lista vazia; a tela mostra o preço e o que vem, compra ou restaura pela loja falsa e volta para a
 * lista pelo botão ou pelo voltar do celular.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = ThemeScreenshotTest.SCREEN)
class FullVersionFlowTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val fixture = EditFixture()
    private val premium = fixture.premium

    private fun show(vararg items: ListItem) {
        items.forEach { fixture.put(it) }
        val listViewModel = fixture.listViewModel()
        val fullVersionViewModel = FullVersionViewModel(
            ObservePremiumUseCase(premium),
            GetPremiumPriceUseCase(premium),
            BuyPremiumUseCase(premium),
            RestorePremiumUseCase(premium),
        )
        compose.setContent {
            ShoppingListTheme(darkTheme = false) {
                ListScreen(
                    viewModel = listViewModel,
                    fullVersionScreen = { onClose -> FullVersionScreen(onClose, viewModel = fullVersionViewModel) },
                )
            }
        }
    }

    private fun button(text: String) = compose.onNode(hasText(text) and hasClickAction())

    private val title get() = compose.onNode(hasText("Versão completa") and isHeading())

    @Test
    fun buyingFromTheEmptyListShowsTheThanksAndHidesTheButton() {
        show()

        button("Versão completa").performClick()
        title.assertExists()
        compose.onNodeWithText("Por R$ 19,90, uma vez só.").assertExists()
        compose.onNodeWithText("Sem anúncios").assertExists()
        compose.onNodeWithText("Fazer a lista e usar no mercado continua de graça.").performScrollTo()

        button("Comprar a versão completa").performScrollTo().performClick()
        compose.onNodeWithText("Você tem a versão completa. Obrigado!").performScrollTo()
        button("Comprar a versão completa").assertDoesNotExist()
        button("Já comprei").assertDoesNotExist()

        button("Voltar para a lista").performClick()
        title.assertDoesNotExist()
        button("Versão completa").assertDoesNotExist()
    }

    @Test
    fun restoringWithoutPurchaseSaysSoAndButtonsWaitForTheStore() {
        show()
        button("Versão completa").performClick()

        val gate = CompletableDeferred<Unit>().also { premium.gate = it }
        button("Já comprei").performScrollTo().performClick()
        button("Já comprei").assertIsNotEnabled()
        button("Comprar a versão completa").assertIsNotEnabled()

        gate.complete(Unit)
        compose.onNodeWithText("Não achei a compra nesta conta do Google.").performScrollTo()

        premium.gate = null
        premium.restoreResult = RestoreResult.Restored
        button("Já comprei").performScrollTo().performClick()
        compose.onNodeWithText("Você tem a versão completa. Obrigado!").performScrollTo()
        compose.onNodeWithText("Não achei a compra nesta conta do Google.").assertDoesNotExist()
    }

    @Test
    fun buttonIsAtTheEndOfTheSheetAndPhoneBackCloses() {
        show(ListItem(ListItemId("1"), "Feijão", "beans", "pantry"))

        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Versão completa"))
        button("Versão completa").performClick()
        title.assertExists()

        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        title.assertDoesNotExist()
        compose.onNodeWithText("Feijão").assertExists()
    }

    @Test
    fun withTheFullVersionThereIsNoButton() {
        premium.isPremium.value = true
        show(ListItem(ListItemId("1"), "Feijão", "beans", "pantry"))

        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Mudar a lista"))
        button("Versão completa").assertDoesNotExist()
    }
}
