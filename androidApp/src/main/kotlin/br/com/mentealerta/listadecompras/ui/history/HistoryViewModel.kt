package br.com.mentealerta.listadecompras.ui.history

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.CatalogSection
import br.com.mentealerta.listadecompras.domain.ObservePurchaseHistoryUseCase
import br.com.mentealerta.listadecompras.domain.PastPurchase
import br.com.mentealerta.listadecompras.domain.PurchasedItem
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.ui.catalog.drawableRes
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Estado da tela "O que já comprei". */
sealed interface HistoryUiState {
    /** O banco ainda não respondeu. */
    data object Loading : HistoryUiState

    /**
     * Nenhuma compra nos dias que a versão mostra. Com [hasOlder], há compras mais antigas que só a
     * versão completa mostra (#90).
     */
    data class Empty(val hasOlder: Boolean = false) : HistoryUiState

    /**
     * Compras da mais recente para a mais antiga. Com [hasOlder], há compras mais antigas que só a
     * versão completa mostra (#90).
     */
    data class Content(val purchases: List<PurchaseUi>, val hasOlder: Boolean = false) : HistoryUiState
}

/** Compra feita há [daysAgo] dias (0 é hoje), com os produtos na ordem em que estavam na lista. */
data class PurchaseUi(val id: String, val daysAgo: Int, val rows: List<PurchasedRowUi>)

/** Produto comprado: nome, quantidade (se a pessoa informou) e ícone. */
data class PurchasedRowUi(val name: String, val quantity: Quantity?, @DrawableRes val icon: Int)

/**
 * "O que já comprei" (#66, plano §1.5): o histórico gravado pelo "Terminei", só para ver. Na versão
 * grátis, os últimos 30 dias, com o recado da versão completa quando há compras mais antigas; na
 * versão completa, tudo (#90, [ObservePurchaseHistoryUseCase]).
 */
class HistoryViewModel(observeHistory: ObservePurchaseHistoryUseCase, private val catalogIndex: CatalogIndex) :
    ViewModel() {
    val uiState: StateFlow<HistoryUiState> = observeHistory()
        .map { (purchases, hasOlder) ->
            if (purchases.isEmpty()) {
                HistoryUiState.Empty(hasOlder)
            } else {
                HistoryUiState.Content(purchases.map { it.toUi() }, hasOlder)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), HistoryUiState.Loading)

    private fun PastPurchase.toUi() = PurchaseUi(
        id = purchase.id.value,
        daysAgo = daysAgo,
        rows = purchase.items.map { it.toRow() },
    )

    /** A compra não guarda a seção: produto desconhecido fica com o ícone de "Outros". */
    private fun PurchasedItem.toRow() = PurchasedRowUi(
        name = name,
        quantity = quantity,
        icon = catalogIndex.icon(productId, CatalogSection.OTHER_ID).let { icon ->
            checkNotNull(icon.drawableRes()) { "Ícone $icon sem recurso convertido" }
        },
    )

    private companion object {
        /** Mantém a coleta numa rotação de tela, sem reabrir a consulta. */
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
