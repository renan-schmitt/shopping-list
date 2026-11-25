package br.com.mentealerta.listadecompras.ui.history

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.Purchase
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.ui.edit.EditFixture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {
    private val fixture = EditFixture()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun withoutPurchasesShowsEmpty() = runTest {
        fixture.historyViewModel().uiState.test {
            assertEquals(HistoryUiState.Empty(), awaitLoaded())
        }
    }

    @Test
    fun showsThePurchasesFromTheMostRecentWithDaysAndIcons() = runTest {
        val today = purchase(
            hoursAgo = 2,
            ListItem.create("bananas", "banana", "produce", Quantity(6.0)),
            ListItem.create("Flocão da vó", null, "pantry"),
        )
        val older = purchase(hoursAgo = 12 * 24, ListItem.create("feijão", "beans", "pantry"))
        fixture.putPurchase(older)
        fixture.putPurchase(today)

        fixture.historyViewModel().uiState.test {
            assertEquals(
                HistoryUiState.Content(
                    listOf(
                        PurchaseUi(
                            today.id.value,
                            daysAgo = 0,
                            rows = listOf(
                                PurchasedRowUi("bananas", Quantity(6.0), R.drawable.catalog_fluent_banana),
                                // Desconhecido: a compra não guarda a seção, fica o ícone de "Outros".
                                PurchasedRowUi("Flocão da vó", null, R.drawable.catalog_fluent_shopping_cart),
                            ),
                        ),
                        PurchaseUi(
                            older.id.value,
                            daysAgo = 12,
                            // Sem ícone próprio: o da seção do produto.
                            rows = listOf(PurchasedRowUi("feijão", null, R.drawable.catalog_fluent_hot_beverage)),
                        ),
                    ),
                ),
                awaitLoaded(),
            )
        }
    }

    @Test
    fun freeVersionLeavesOutPurchasesOlderThanThirtyDaysAndSaysSo() = runTest {
        val recent = purchase(hoursAgo = 2, ListItem.create("feijão", "beans", "pantry"))
        fixture.putPurchase(purchase(hoursAgo = 31 * 24, ListItem.create("feijão", "beans", "pantry")))
        fixture.putPurchase(recent)

        fixture.historyViewModel().uiState.test {
            val state = awaitLoaded() as HistoryUiState.Content
            assertEquals(listOf(recent.id.value), state.purchases.map { it.id })
            assertTrue(state.hasOlder)
        }
    }

    @Test
    fun freeVersionWithOnlyOldPurchasesIsEmptyAndSaysSo() = runTest {
        fixture.putPurchase(purchase(hoursAgo = 31 * 24, ListItem.create("feijão", "beans", "pantry")))

        fixture.historyViewModel().uiState.test {
            assertEquals(HistoryUiState.Empty(hasOlder = true), awaitLoaded())
        }
    }

    @Test
    fun completeVersionShowsEveryPurchaseRightAfterBuying() = runTest {
        val old = purchase(hoursAgo = 400 * 24, ListItem.create("feijão", "beans", "pantry"))
        fixture.putPurchase(old)

        fixture.historyViewModel().uiState.test {
            assertEquals(HistoryUiState.Empty(hasOlder = true), awaitLoaded())
            fixture.premium.isPremium.value = true
            val state = awaitItem() as HistoryUiState.Content
            assertEquals(listOf(PurchaseUi(old.id.value, 400, state.purchases.single().rows)), state.purchases)
            assertFalse(state.hasOlder)
        }
    }

    @Test
    fun historyButtonOnTheListFollowsTheVersion() = runTest {
        fixture.putPurchase(purchase(hoursAgo = 31 * 24, ListItem.create("feijão", "beans", "pantry")))

        fixture.listViewModel().hasHistory.test {
            // Grátis: só compras antigas, nada a mostrar.
            assertFalse(awaitItem())
            fixture.premium.isPremium.value = true
            assertTrue(awaitItem())
        }
    }

    private fun purchase(hoursAgo: Int, vararg items: ListItem) =
        Purchase.create(fixture.now - hoursAgo.hours, items.toList())

    /** Pula o "carregando", se ele chegar a aparecer. */
    private suspend fun ReceiveTurbine<HistoryUiState>.awaitLoaded(): HistoryUiState =
        awaitItem().takeUnless { it == HistoryUiState.Loading } ?: awaitItem()
}
