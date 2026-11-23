package br.com.mentealerta.listadecompras.ui.history

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.domain.Purchase
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.domain.QuantityUnit
import br.com.mentealerta.listadecompras.ui.edit.EditFixture
import br.com.mentealerta.listadecompras.ui.list.ListScreen
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme
import br.com.mentealerta.listadecompras.ui.theme.ThemeScreenshotTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.time.Duration.Companion.days

/**
 * "O que já comprei" (#66) pela tela da lista: o botão só aparece com compras no histórico; abre a
 * tela com as compras da mais recente para a mais antiga, com a data por extenso, e volta para a
 * lista pelo botão ou pelo voltar do celular.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = ThemeScreenshotTest.SCREEN)
class HistoryFlowTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val fixture = EditFixture()

    private fun show(vararg items: ListItem) {
        items.forEach { fixture.put(it) }
        val listViewModel = fixture.listViewModel()
        val historyViewModel = fixture.historyViewModel()
        compose.setContent {
            ShoppingListTheme(darkTheme = false) {
                ListScreen(
                    viewModel = listViewModel,
                    historyScreen = { onClose -> HistoryScreen(onClose, viewModel = historyViewModel) },
                )
            }
        }
    }

    private fun button(text: String) = compose.onNode(hasText(text) and hasClickAction())

    @Test
    fun withoutPurchasesThereIsNoHistoryButton() {
        show(ListItem(ListItemId("1"), "Feijão", "beans", "pantry"))
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Mudar a lista"))

        button("O que já comprei").assertDoesNotExist()
    }

    @Test
    fun finishingShowsThePurchaseInTheHistory() {
        show(
            ListItem(ListItemId("1"), "Banana", "banana", "produce", quantity = Quantity(6.0), checked = true),
            ListItem(ListItemId("2"), "Feijão", "beans", "pantry", checked = true),
        )
        fixture.putPurchase(
            Purchase.create(
                fixture.now - 12.days,
                listOf(ListItem.create("Arroz", null, "pantry", Quantity(5.0, QuantityUnit.KILOGRAM))),
            ),
        )

        button("Terminei").performClick()
        // O aviso fica no meio da tela, por cima do botão.
        button("Certo").performClick()
        // A lista ficou vazia: o botão aparece embaixo do texto.
        button("O que já comprei").performClick()

        compose.onNode(hasText("O que já comprei") and isHeading()).assertExists()
        compose.onNode(hasText("Hoje, 2 produtos") and isHeading()).assertExists()
        compose.onNodeWithText("Banana, 6").assertExists()
        compose.onNodeWithText("Feijão").assertExists()
        compose.onNode(hasText("Há 12 dias, 1 produto") and isHeading()).assertExists()
        compose.onNodeWithText("Arroz, 5 kg").assertExists()

        button("Voltar para a lista").performClick()
        compose.onNode(hasText("O que já comprei") and isHeading()).assertDoesNotExist()
        button("O que já comprei").assertExists()
    }

    @Test
    fun historyButtonIsAtTheEndOfTheSheetAndPhoneBackCloses() {
        fixture.putPurchase(Purchase.create(fixture.now - 1.days, listOf(ListItem.create("Café", "coffee", "pantry"))))
        show(ListItem(ListItemId("1"), "Feijão", "beans", "pantry"))

        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("O que já comprei"))
        button("O que já comprei").performClick()
        compose.onNode(hasText("Ontem, 1 produto") and isHeading()).assertExists()

        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.onNode(hasText("Ontem, 1 produto")).assertDoesNotExist()
        compose.onNodeWithText("Feijão").assertExists()
    }
}
