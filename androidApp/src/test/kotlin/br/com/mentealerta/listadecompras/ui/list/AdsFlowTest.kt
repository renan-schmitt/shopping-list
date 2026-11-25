package br.com.mentealerta.listadecompras.ui.list

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.ui.components.UNDO_NOTICE_MILLIS
import br.com.mentealerta.listadecompras.ui.edit.EditFixture
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
import kotlin.time.Duration.Companion.days

/**
 * Anúncios (#89) pela tela da lista: o aviso do fim da primeira semana ao abrir, com "Conhecer a
 * versão completa"; o anúncio depois de **Certo** no aviso do "Terminei", nunca quando o aviso some
 * sozinho; e a cada 3 voltas da tela de adicionar.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = ThemeScreenshotTest.SCREEN)
class AdsFlowTest {

    @get:Rule
    val compose = createComposeRule()

    private val fixture = EditFixture()
    private val ads = fixture.ads
    private var fullVersionOpened = false

    private fun show(vararg items: ListItem) {
        items.forEach { fixture.put(it) }
        val viewModel = fixture.listViewModel()
        compose.setContent {
            ShoppingListTheme(darkTheme = false) {
                ListScreen(
                    viewModel = viewModel,
                    fullVersionScreen = { fullVersionOpened = true },
                )
            }
        }
    }

    private fun button(text: String) = compose.onNode(hasText(text) and hasClickAction())

    private val weekNotice = "A partir de amanhã, o app continua de graça, com alguns anúncios. " +
        "Se preferir sem anúncios, tem a versão completa."

    @Test
    fun weekNoticeOpensTheFullVersion() {
        ads.firstOpenedAgo(6.days)
        show()

        compose.onNodeWithText(weekNotice).assertExists()
        button("Conhecer a versão completa").performClick()
        compose.waitForIdle()

        assertTrue(fullVersionOpened)
        assertEquals(ads.now, ads.preferences.current.weekNoticeShownAt)
    }

    @Test
    fun weekNoticeStaysUntilCerto() {
        ads.firstOpenedAgo(6.days)
        show()

        compose.mainClock.advanceTimeBy(UNDO_NOTICE_MILLIS * 2)
        compose.onNodeWithText(weekNotice).assertExists()
        button("Certo").performClick()

        compose.onNodeWithText(weekNotice).assertDoesNotExist()
    }

    @Test
    fun noWeekNoticeInTheFirstDays() {
        ads.firstOpenedAgo(2.days)
        show()

        compose.onNodeWithText(weekNotice).assertDoesNotExist()
    }

    @Test
    fun adAfterCertoOnTheFinishNotice() {
        ads.startAds()
        show(ListItem(ListItemId("1"), "Feijão", "beans", "pantry", checked = true))

        button("Terminei").performClick()
        button("Certo").performClick()
        compose.waitForIdle()

        assertEquals(1, ads.ads.shows.size)
    }

    @Test
    fun noAdWhenTheFinishNoticeClosesByItself() {
        ads.startAds()
        show(ListItem(ListItemId("1"), "Feijão", "beans", "pantry", checked = true))

        button("Terminei").performClick()
        compose.mainClock.advanceTimeBy(UNDO_NOTICE_MILLIS + 1_000)
        compose.waitForIdle()

        assertTrue(ads.ads.shows.isEmpty())
    }

    @Test
    fun noAdWhenAnotherScreenClosesTheFinishNotice() {
        ads.startAds()
        show(
            ListItem(ListItemId("1"), "Feijão", "beans", "pantry", checked = true),
            ListItem(ListItemId("2"), "Banana", "banana", "produce"),
        )

        button("Terminei").performClick()
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Mudar a lista"))
        button("Mudar a lista").performClick()
        compose.waitForIdle()

        assertTrue(ads.ads.shows.isEmpty())
    }
}
