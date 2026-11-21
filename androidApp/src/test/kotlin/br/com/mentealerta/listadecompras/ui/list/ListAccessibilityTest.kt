package br.com.mentealerta.listadecompras.ui.list

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToString
import androidx.compose.ui.unit.dp
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme
import br.com.mentealerta.listadecompras.ui.theme.ThemeScreenshotTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Acessibilidade da tela da lista (plano §1.7 e §4.6): altura da linha, títulos de seção, leitura do
 * TalkBack em caixa normal com detalhe e quantidade, estado riscado anunciado (nunca só por cor) e
 * nome longo sem cortar. Os checks do Accessibility Test Framework não rodam no Robolectric (ver
 * `ComponentsAccessibilityTest`), por isso as regras são conferidas uma a uma.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = ThemeScreenshotTest.SCREEN)
class ListAccessibilityTest {

    @get:Rule
    val compose = createComposeRule()

    private fun show(state: ListUiState = SampleListState) {
        compose.setContent { ShoppingListTheme(darkTheme = false) { ListContent(state) } }
    }

    @Test
    fun rowsAreAtLeast72dpTall() {
        show()
        listOf("Tomate", "Alface", "Café").forEach { compose.onNodeWithText(it).assertHeightIsAtLeast(72.dp) }
    }

    @Test
    @Config(fontScale = 2.0f)
    fun rowsStayAtLeast72dpTallWithLargeFont() {
        show()
        compose.onNodeWithText("Tomate").assertHeightIsAtLeast(72.dp)
    }

    @Test
    fun sectionTitlesAreHeadings() {
        show()
        listOf("Hortifrúti", "Padaria", "Açougue", "Mercearia").forEach {
            compose.onNodeWithText(it).assert(isHeading())
        }
    }

    @Test
    fun talkBackReadsNameDetailAndQuantityInNormalCase() {
        show()
        compose.onNodeWithText("Leite desnatado, 2 litros").assertExists()
        compose.onNodeWithText("Arroz, 5 kg").assertExists()
        compose.onAllNodesWithText("LEITE", substring = true).assertCountEquals(0)
    }

    @Test
    fun talkBackReadsHalfAsWords() {
        // Na tela: "½ kg"; no TalkBack: "meio quilo".
        show()
        compose.onNodeWithText("Carne moída de patinho, meio quilo").assertExists()
    }

    @Test
    fun checkedStateIsAnnouncedNotOnlyByColor() {
        show()
        compose.onNodeWithText("Banana, 6")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Comprado"))
        compose.onNodeWithText("Tomate")
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.StateDescription))
    }

    @Test
    fun iconsHaveNoLabelOfTheirOwn() {
        // Só o texto é lido: o ícone acompanha, não substitui (guia, princípio 5).
        show()
        val tree = compose.onRoot(useUnmergedTree = true).printToString(maxDepth = Int.MAX_VALUE)
        assertTrue("Algum nó tem contentDescription: $tree", "ContentDescription" !in tree)
    }

    @Test
    @Config(fontScale = 2.0f)
    fun longNameWrapsInsteadOfBeingCut() {
        show()
        // Cortado em uma linha, ficaria na altura mínima; inteiro, ocupa várias linhas de 28sp × 2.
        compose.onNodeWithText("Biscoito de polvilho salgado da padaria da esquina, 3 pacotes")
            .assertHeightIsAtLeast(200.dp)
    }

    @Test
    fun emptyListShowsTheGuideMessage() {
        show(ListUiState.Empty)
        compose.onNodeWithText("Sua lista está vazia. Toque em Adicionar para começar.").assertExists()
    }

    @Test
    fun loadingShowsNoEmptyMessage() {
        show(ListUiState.Loading)
        assertEquals(
            0,
            compose.onAllNodesWithText("vazia", substring = true).fetchSemanticsNodes().size,
        )
    }
}
