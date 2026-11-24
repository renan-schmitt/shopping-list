package br.com.mentealerta.listadecompras.ui.fullversion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.mentealerta.listadecompras.domain.BuyPremiumUseCase
import br.com.mentealerta.listadecompras.domain.GetPremiumPriceUseCase
import br.com.mentealerta.listadecompras.domain.ObservePremiumUseCase
import br.com.mentealerta.listadecompras.domain.RestorePremiumUseCase
import br.com.mentealerta.listadecompras.premium.PurchaseResult
import br.com.mentealerta.listadecompras.premium.RestoreResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado da tela "Versão completa". [price] é o preço formatado pela loja (`null` enquanto ela não
 * responde, ou se não respondeu). Com [busy], a janela da loja está aberta ou o "Já comprei" está
 * procurando: os botões ficam desativados, para um toque duplo não abrir a loja duas vezes.
 */
data class FullVersionUiState(
    val isComplete: Boolean,
    val price: String? = null,
    val busy: Boolean = false,
    val message: FullVersionMessage? = null,
)

/** Recado que fica na tela depois de comprar ou do "Já comprei", até a próxima tentativa. */
enum class FullVersionMessage {
    /** Pix ou boleto esperando: libera sozinha quando a loja confirmar. */
    Pending,

    /** Sem internet ou sem Google Play. */
    Unavailable,

    /** Outro erro da loja. */
    Failed,

    /** O "Já comprei" não achou a compra nesta conta. */
    NothingToRestore,
}

/**
 * "Versão completa" (#88, plano §5): explica o que vem, sem pressão, e compra ou restaura pela loja.
 * Comprar ou restaurar com sucesso troca a tela para o agradecimento, porque [FullVersionUiState.isComplete]
 * segue a compra liberada; desistir na janela da loja não mostra recado.
 */
class FullVersionViewModel(
    observePremium: ObservePremiumUseCase,
    private val getPrice: GetPremiumPriceUseCase,
    private val buyPremium: BuyPremiumUseCase,
    private val restorePremium: RestorePremiumUseCase,
) : ViewModel() {
    private val local = MutableStateFlow(LocalState())

    val uiState: StateFlow<FullVersionUiState> = combine(observePremium(), local) { isComplete, state ->
        FullVersionUiState(isComplete, state.price, state.busy, state.message.takeUnless { isComplete })
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        FullVersionUiState(isComplete = observePremium().value),
    )

    init {
        viewModelScope.launch {
            val price = getPrice()
            local.update { it.copy(price = price) }
        }
    }

    /** "Comprar a versão completa": abre a janela de pagamento da loja. */
    fun onBuy() = launchStoreAction {
        when (buyPremium()) {
            PurchaseResult.Purchased, PurchaseResult.Cancelled -> null
            PurchaseResult.Pending -> FullVersionMessage.Pending
            PurchaseResult.Unavailable -> FullVersionMessage.Unavailable
            PurchaseResult.Failed -> FullVersionMessage.Failed
        }
    }

    /** "Já comprei": procura a compra na conta da loja. */
    fun onRestore() = launchStoreAction {
        when (restorePremium()) {
            RestoreResult.Restored -> null
            RestoreResult.Pending -> FullVersionMessage.Pending
            RestoreResult.NothingToRestore -> FullVersionMessage.NothingToRestore
            RestoreResult.Unavailable -> FullVersionMessage.Unavailable
            RestoreResult.Failed -> FullVersionMessage.Failed
        }
    }

    /** Roda [action] uma vez por vez; o recado anterior sai quando a pessoa tenta de novo. */
    private fun launchStoreAction(action: suspend () -> FullVersionMessage?) {
        if (local.value.busy) return
        local.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            val message = action()
            local.update { it.copy(busy = false, message = message) }
        }
    }

    private data class LocalState(
        val price: String? = null,
        val busy: Boolean = false,
        val message: FullVersionMessage? = null,
    )

    private companion object {
        /** Mantém a coleta numa rotação de tela. */
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
