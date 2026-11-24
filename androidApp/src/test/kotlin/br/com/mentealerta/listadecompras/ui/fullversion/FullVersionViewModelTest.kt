package br.com.mentealerta.listadecompras.ui.fullversion

import br.com.mentealerta.listadecompras.domain.BuyPremiumUseCase
import br.com.mentealerta.listadecompras.domain.GetPremiumPriceUseCase
import br.com.mentealerta.listadecompras.domain.ObservePremiumUseCase
import br.com.mentealerta.listadecompras.domain.RestorePremiumUseCase
import br.com.mentealerta.listadecompras.premium.PurchaseResult
import br.com.mentealerta.listadecompras.premium.RestoreResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** "Versão completa" (#88): preço, comprar, "Já comprei", recados e o toque duplo. */
@OptIn(ExperimentalCoroutinesApi::class)
class FullVersionViewModelTest {

    private val premium = FakePremiumRepository()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = FullVersionViewModel(
        ObservePremiumUseCase(premium),
        GetPremiumPriceUseCase(premium),
        BuyPremiumUseCase(premium),
        RestorePremiumUseCase(premium),
    )

    /** O ViewModel com a tela aberta (coletando o estado). */
    private fun TestScope.opened() = viewModel().also {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { it.uiState.collect() }
    }

    @Test
    fun showsThePriceFromTheStore() = runTest {
        val state = opened().uiState.value

        assertFalse(state.isComplete)
        assertEquals("R$ 19,90", state.price)
        assertNull(state.message)
    }

    @Test
    fun withoutAnswerFromTheStoreThereIsNoPrice() = runTest {
        premium.price = null

        assertNull(opened().uiState.value.price)
    }

    @Test
    fun buyingUnlocksTheFullVersionWithoutMessage() = runTest {
        val viewModel = opened()

        viewModel.onBuy()

        val state = viewModel.uiState.value
        assertTrue(state.isComplete)
        assertFalse(state.busy)
        assertNull(state.message)
    }

    @Test
    fun cancellingShowsNoMessage() = runTest {
        premium.buyResult = PurchaseResult.Cancelled
        val viewModel = opened()

        viewModel.onBuy()

        assertFalse(viewModel.uiState.value.isComplete)
        assertNull(viewModel.uiState.value.message)
    }

    @Test
    fun eachStoreAnswerHasItsMessage() = runTest {
        val viewModel = opened()
        mapOf(
            PurchaseResult.Pending to FullVersionMessage.Pending,
            PurchaseResult.Unavailable to FullVersionMessage.Unavailable,
            PurchaseResult.Failed to FullVersionMessage.Failed,
        ).forEach { (result, message) ->
            premium.buyResult = result
            viewModel.onBuy()
            assertEquals(message, viewModel.uiState.value.message)
        }
        mapOf(
            RestoreResult.Pending to FullVersionMessage.Pending,
            RestoreResult.NothingToRestore to FullVersionMessage.NothingToRestore,
            RestoreResult.Unavailable to FullVersionMessage.Unavailable,
            RestoreResult.Failed to FullVersionMessage.Failed,
        ).forEach { (result, message) ->
            premium.restoreResult = result
            viewModel.onRestore()
            assertEquals(message, viewModel.uiState.value.message)
        }
    }

    @Test
    fun restoringUnlocksTheFullVersion() = runTest {
        premium.restoreResult = RestoreResult.Restored
        val viewModel = opened()

        viewModel.onRestore()

        assertTrue(viewModel.uiState.value.isComplete)
        assertNull(viewModel.uiState.value.message)
    }

    @Test
    fun tryingAgainClearsTheMessageAndADoubleTapOpensTheStoreOnce() = runTest {
        premium.buyResult = PurchaseResult.Unavailable
        val viewModel = opened()
        viewModel.onBuy()
        assertEquals(FullVersionMessage.Unavailable, viewModel.uiState.value.message)

        val gate = CompletableDeferred<Unit>().also { premium.gate = it }
        premium.buyResult = PurchaseResult.Purchased
        viewModel.onBuy()
        viewModel.onBuy()
        viewModel.onRestore()
        assertTrue(viewModel.uiState.value.busy)
        assertNull(viewModel.uiState.value.message)

        gate.complete(Unit)
        assertEquals(2, premium.buyCalls)
        assertTrue(viewModel.uiState.value.isComplete)
        assertFalse(viewModel.uiState.value.busy)
    }
}
