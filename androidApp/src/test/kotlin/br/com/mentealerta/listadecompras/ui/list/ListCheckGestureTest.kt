package br.com.mentealerta.listadecompras.ui.list

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.dp
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme
import br.com.mentealerta.listadecompras.ui.theme.ThemeScreenshotTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Riscar e tirar o risco (#51): tocando na marca, passando o dedo para qualquer lado ou pela ação
 * do TalkBack, sempre com vibração. Tocar no nome não risca (fica para a edição, #55) e um arrasto
 * curto volta sem riscar.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = ThemeScreenshotTest.SCREEN)
class ListCheckGestureTest {

    @get:Rule
    val compose = createComposeRule()

    private val toggles = mutableListOf<Pair<ListItemId, Boolean>>()
    private val edits = mutableListOf<ListItemId>()
    private val haptics = mutableListOf<HapticFeedbackType>()

    private val fakeHaptics = object : HapticFeedback {
        override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
            haptics += hapticFeedbackType
        }
    }

    private fun show() {
        compose.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides fakeHaptics) {
                ShoppingListTheme(darkTheme = false) {
                    ListContent(
                        SampleListState,
                        onToggleCheck = { id, checked -> toggles += id to checked },
                        onEdit = { edits += it },
                    )
                }
            }
        }
    }

    private fun idOf(name: String): ListItemId = SampleListState.sections
        .flatMap { it.rows }
        .first { it.name == name }
        .id

    @Test
    fun tappingTheMarkChecks() {
        show()
        // Na beirada da área de toque, fora do desenho da marca: a área é maior que a marca.
        compose.onNodeWithText("Tomate").performTouchInput { click(Offset(2.dp.toPx(), centerY)) }
        assertEquals(listOf(idOf("Tomate") to false), toggles)
        assertEquals(listOf(HapticFeedbackType.ToggleOn), haptics)
    }

    @Test
    fun tappingTheMarkOfACheckedItemUnchecks() {
        show()
        compose.onNodeWithText("Banana, 6").performTouchInput { click(Offset(2.dp.toPx(), centerY)) }
        assertEquals(listOf(idOf("Banana") to true), toggles)
        assertEquals(listOf(HapticFeedbackType.ToggleOff), haptics)
    }

    @Test
    fun tappingTheNameOpensTheEditInsteadOfChecking() {
        show()
        compose.onNodeWithText("Tomate").performTouchInput { click(center) }
        assertEquals(emptyList<Pair<ListItemId, Boolean>>(), toggles)
        assertEquals(listOf(idOf("Tomate")), edits)
    }

    @Test
    fun tappingTheMarkDoesNotOpenTheEdit() {
        show()
        compose.onNodeWithText("Tomate").performTouchInput { click(Offset(2.dp.toPx(), centerY)) }
        assertEquals(emptyList<ListItemId>(), edits)
    }

    @Test
    fun swipingDoesNotOpenTheEdit() {
        show()
        compose.onNodeWithText("Tomate").performTouchInput { swipeRight() }
        assertEquals(emptyList<ListItemId>(), edits)
    }

    @Test
    fun talkBackOffersEditAsAnExtraAction() {
        show()
        val action = compose.onNodeWithText("Tomate").fetchSemanticsNode()
            .config[SemanticsActions.CustomActions].single()
        assertEquals("Editar", action.label)
        action.action()
        assertEquals(listOf(idOf("Tomate")), edits)
        assertEquals(emptyList<Pair<ListItemId, Boolean>>(), toggles)
    }

    @Test
    fun swipingRightChecks() {
        show()
        compose.onNodeWithText("Tomate").performTouchInput { swipeRight() }
        assertEquals(listOf(idOf("Tomate") to false), toggles)
        assertEquals(listOf(HapticFeedbackType.ToggleOn), haptics)
    }

    @Test
    fun swipingLeftAlsoChecks() {
        show()
        compose.onNodeWithText("Tomate").performTouchInput { swipeLeft() }
        assertEquals(listOf(idOf("Tomate") to false), toggles)
    }

    @Test
    fun swipingACheckedItemUnchecks() {
        show()
        compose.onNodeWithText("Banana, 6").performTouchInput { swipeRight() }
        assertEquals(listOf(idOf("Banana") to true), toggles)
        assertEquals(listOf(HapticFeedbackType.ToggleOff), haptics)
    }

    @Test
    fun shortSwipeDoesNotCheck() {
        show()
        compose.onNodeWithText("Tomate").performTouchInput {
            swipeRight(startX = centerX, endX = centerX + 24.dp.toPx())
        }
        assertEquals(emptyList<Pair<ListItemId, Boolean>>(), toggles)
        assertEquals(emptyList<HapticFeedbackType>(), haptics)
    }

    @Test
    fun talkBackOffersMarkAsBought() {
        show()
        compose.onNodeWithText("Tomate")
            .assert(
                SemanticsMatcher("ação \"Marcar como comprado\"") { node ->
                    node.config.getOrElseNullable(SemanticsActions.OnClick) { null }?.label == "Marcar como comprado"
                },
            )
            .performSemanticsAction(SemanticsActions.OnClick)
        assertEquals(listOf(idOf("Tomate") to false), toggles)
        assertEquals(listOf(HapticFeedbackType.ToggleOn), haptics)
    }

    @Test
    fun talkBackOffersUnmarkOnCheckedItem() {
        show()
        compose.onNodeWithText("Banana, 6")
            .assert(
                SemanticsMatcher("ação \"Tirar o risco\"") { node ->
                    node.config.getOrElseNullable(SemanticsActions.OnClick) { null }?.label == "Tirar o risco"
                },
            )
            .performSemanticsAction(SemanticsActions.OnClick)
        assertEquals(listOf(idOf("Banana") to true), toggles)
    }
}
