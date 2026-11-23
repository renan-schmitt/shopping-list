package br.com.mentealerta.listadecompras.e2e

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import br.com.mentealerta.listadecompras.MainActivity
import br.com.mentealerta.listadecompras.ui.theme.ThemeScreenshotTest
import br.com.mentealerta.listadecompras.voice.VoiceEvent
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Ponta a ponta (#77): o app montado, da `MainActivity` ao banco, sem ViewModel nem repositório falso.
 * Cobre os fluxos principais do plano §4.6 (adicionar digitando e falando, riscar, "Terminei", jogar
 * a lista fora, voltar), sempre passando de uma tela para outra como a pessoa faz. Cada tela tem
 * também o seu teste de fluxo, com os detalhes; aqui vale o caminho inteiro.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = ThemeScreenshotTest.SCREEN)
class MainFlowsTest {

    private val koin = AppKoinRule()
    private val compose = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(koin).around(compose)

    private fun button(text: String) = compose.onNode(hasText(text) and hasClickAction())

    private val field get() = compose.onNode(hasSetTextAction())

    /** Banco, busca e voz respondem fora da thread principal: espera o texto aparecer. */
    private fun waitFor(text: String) = waitFor(hasText(text))

    private fun waitFor(matcher: SemanticsMatcher) {
        compose.waitUntil(TIMEOUT_MS) { compose.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun waitForGone(text: String) {
        compose.waitUntil(TIMEOUT_MS) { compose.onAllNodes(hasText(text)).fetchSemanticsNodes().isEmpty() }
    }

    private fun pressBack() {
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
    }

    /** Abre o "Adicionar", põe cada produto digitando e volta para a lista. */
    private fun addTyping(vararg products: Pair<String, String>) {
        button("Adicionar").performClick()
        products.forEach { (typed, option) ->
            field.performTextInput(typed)
            waitFor(hasText(option) and hasClickAction())
            button(option).performClick()
            waitFor("Adicionado: ${option.substringBefore(",")}.")
        }
        button("Voltar para a lista").performClick()
    }

    @Test
    fun startsEmptyAndTypedProductsGoToTheirSections() {
        waitFor("Lista de Compras")
        compose.onNodeWithText("Toque em Adicionar para começar sua lista.").assertExists()

        addTyping("2 banan" to "Banana, 2", "feijao" to "Feijão")

        waitFor("Banana, 2")
        compose.onNodeWithText("Feijão").assertExists()
        compose.onNodeWithText("Hortifrúti").assertExists()
        compose.onNodeWithText("Mercearia").assertExists()
    }

    @Test
    fun spokenProductsGoToTheList() {
        shadowOf(compose.activity.application).grantPermissions(android.Manifest.permission.RECORD_AUDIO)
        waitFor("Lista de Compras")
        button("Adicionar").performClick()

        button("Falar").performClick()
        waitFor(hasText("Usar o microfone") and hasClickAction())
        button("Usar o microfone").performClick()
        waitFor(hasText("Falar") and hasClickAction())
        button("Falar").performTouchInput { down(center) }
        waitFor("Estou ouvindo. Pode falar.")
        koin.voice.say(VoiceEvent.Heard(listOf("arroz e feijão")))
        waitFor("arroz e feijão")
        button("Terminei").performTouchInput {
            advanceEventTime(1_000)
            up()
        }
        waitFor("Adicionados: 2 produtos.")
        button("Voltar para a lista").performClick()

        // O nome fica como foi falado ("arroz"); aqui vale que os dois entraram na lista.
        waitFor(hasText("Arroz", ignoreCase = true))
        compose.onNodeWithText("Feijão", ignoreCase = true).assertExists()
    }

    @Test
    fun checkingAndFinishingSavesThePurchaseAndOpsVoltarBringsItBack() {
        waitFor("Lista de Compras")
        addTyping("banana" to "Banana", "feijao" to "Feijão")
        waitFor("Banana")

        compose.onNodeWithText("Banana").performClick()
        waitFor("Já peguei")
        button("Terminei").performClick()

        waitFor("Pronto! 1 produto comprado. Ficou 1 na lista para a próxima vez.")
        waitForGone("Banana")
        compose.onNodeWithText("Feijão").assertExists()

        button("Desfazer").performClick()
        waitFor("Banana")
        button("Terminei").assertExists()
    }

    @Test
    fun throwingTheListAwayAsksFirstAndOpsVoltarBringsItBack() {
        waitFor("Lista de Compras")
        addTyping("banana" to "Banana", "feijao" to "Feijão")
        waitFor("Banana")

        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Mudar a lista"))
        button("Mudar a lista").performClick()
        button("Jogar a lista fora").performClick()
        waitFor("Jogar a lista fora?")
        button("Sim, jogar fora").performClick()

        waitFor("A lista foi jogada fora.")
        waitForGone("Banana")
        compose.onNodeWithText("Toque em Adicionar para começar sua lista.").assertExists()

        button("Desfazer").performClick()
        waitFor("Banana")
        compose.onNodeWithText("Feijão").assertExists()
    }

    @Test
    fun phoneBackLeavesAddAndEditAndTheChangeIsKept() {
        waitFor("Lista de Compras")
        button("Adicionar").performClick()
        field.performTextInput("feijao")
        waitFor(hasText("Feijão") and hasClickAction())
        button("Feijão").performClick()
        waitFor("Adicionado: Feijão.")

        pressBack()
        waitFor("Feijão")
        button("Adicionar").assertExists()

        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Mudar a lista"))
        button("Mudar a lista").performClick()
        compose.onNodeWithText("Feijão").performClick()
        val name = hasSetTextAction() and hasContentDescription("Nome")
        waitFor(name)
        compose.onNode(name).performTextReplacement("Feijão preto")

        pressBack()
        waitFor("Toque no produto que quer mudar.")
        pressBack()
        waitForGone("Toque no produto que quer mudar.")
        waitFor("Feijão preto")
    }

    @Test
    fun listIsKeptWhenTheScreenIsRecreated() {
        waitFor("Lista de Compras")
        addTyping("banana" to "Banana", "feijao" to "Feijão")
        waitFor("Banana")
        compose.onNodeWithText("Banana").performClick()
        waitFor("Já peguei")

        compose.activityRule.scenario.recreate()

        waitFor("Já peguei")
        compose.onNodeWithText("Banana").assertExists()
        compose.onNodeWithText("Feijão").assertExists()
    }

    private companion object {
        const val TIMEOUT_MS = 5_000L
    }
}
