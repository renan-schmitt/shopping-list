package br.com.mentealerta.listadecompras.ui.list

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.domain.ClearListUseCase
import br.com.mentealerta.listadecompras.domain.FinishShoppingUseCase
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.domain.ObserveListUseCase
import br.com.mentealerta.listadecompras.domain.ObservePurchaseHistoryUseCase
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.domain.RemoveItemUseCase
import br.com.mentealerta.listadecompras.domain.SetItemCheckedUseCase
import br.com.mentealerta.listadecompras.domain.UndoLastActionUseCase
import br.com.mentealerta.listadecompras.ui.catalog.iconRes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado da tela da lista. */
sealed interface ListUiState {
    /** O banco ainda não respondeu: a tela fica em branco, sem mostrar "lista vazia" por engano. */
    data object Loading : ListUiState

    data object Empty : ListUiState

    /**
     * Lista com produtos. A tela se adapta sozinha (#52), sem "modo No mercado" guardado: riscar o
     * primeiro produto já é "estou comprando" ([isShopping]), e o "Terminei" mostra quantos faltam
     * ([remaining]).
     */
    data class Content(val sections: List<SectionUi>) : ListUiState {
        val isShopping: Boolean = sections.any { section -> section.rows.any { it.checked } }
        val remaining: Int = sections.sumOf { section -> section.rows.count { !it.checked } }

        /** Há o que comprar: só então aparece o "Mandar a lista" (#67). */
        val canShare: Boolean get() = remaining > 0

        /** Todos os produtos, riscados ou não: os que saem ao jogar a lista fora (#58). */
        val total: Int = sections.sumOf { it.rows.size }
    }
}

/**
 * Seção com título e os produtos dela, na ordem em que entraram. A seção dos riscados ([bought]) vem
 * sempre por último e não tem nome aqui: a tela mostra "Já peguei" (#121), do `strings.xml`.
 */
data class SectionUi(val id: String, val name: String, val rows: List<ListRowUi>, val bought: Boolean = false) {
    companion object {
        const val BOUGHT_ID = "bought"

        /** "Já peguei": os riscados de todas as seções, na ordem das seções. */
        fun bought(rows: List<ListRowUi>) = SectionUi(BOUGHT_ID, name = "", rows = rows, bought = true)
    }
}

/** Linha do produto: marca de riscar → nome (com detalhe e quantidade) → ícone. */
data class ListRowUi(
    val id: ListItemId,
    val name: String,
    val detail: String? = null,
    val quantity: Quantity? = null,
    val checked: Boolean = false,
    @DrawableRes val icon: Int,
)

/** Aviso com "Desfazer" que a tela mostra depois de uma ação. */
sealed interface ListNotice {
    /** "Terminei" (#57): [bought] produtos comprados, [remaining] ficaram na lista. */
    data class Finished(val bought: Int, val remaining: Int) : ListNotice

    /** "Jogar a lista fora" (#58). */
    data object Cleared : ListNotice

    /** "Tirar da lista" pela ação do TalkBack na linha (#71): "Feijão saiu da lista.". */
    data class Removed(val name: String) : ListNotice
}

/**
 * Tela da lista (#50): a lista agrupada por seção, atualizada a cada mudança no banco. As seções
 * mostram só o que falta; os riscados descem para "Já peguei", no fim (#121, plano §1.1). Riscar e tirar
 * o risco (#51), o "Terminei" (#57), o "Jogar a lista fora" (#58) e o "Tirar da lista" do TalkBack
 * (#71) entram na pilha do "Desfazer"; [onUndo] desfaz a última ação. [hasHistory] diz se há compras
 * para mostrar em "O que já comprei" (#66).
 */
class ListViewModel(
    observeList: ObserveListUseCase,
    catalogIndex: CatalogIndex,
    private val setItemChecked: SetItemCheckedUseCase,
    private val finishShopping: FinishShoppingUseCase,
    private val clearList: ClearListUseCase,
    private val removeItem: RemoveItemUseCase,
    private val undoLastAction: UndoLastActionUseCase,
    observeHistory: ObservePurchaseHistoryUseCase,
) : ViewModel() {
    private val mutableNotice = MutableStateFlow<ListNotice?>(null)

    /** Aviso a mostrar; a tela chama [onNoticeShown] quando ele sai. Fica guardado numa rotação de tela. */
    val notice: StateFlow<ListNotice?> = mutableNotice.asStateFlow()

    val uiState: StateFlow<ListUiState> = observeList()
        .map { sections ->
            if (sections.isEmpty()) {
                ListUiState.Empty
            } else {
                val pending = sections.mapNotNull { (section, items) ->
                    items.filterNot { it.checked }
                        .takeIf { it.isNotEmpty() }
                        ?.let { rows -> SectionUi(section.id, section.name, rows.map { it.toRow(catalogIndex) }) }
                }
                val bought = sections.flatMap { it.items }.filter { it.checked }.map { it.toRow(catalogIndex) }
                ListUiState.Content(if (bought.isEmpty()) pending else pending + SectionUi.bought(bought))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ListUiState.Loading)

    /** Se há compras no histórico (com o limite da versão grátis): só então aparece "O que já comprei". */
    val hasHistory: StateFlow<Boolean> = observeHistory()
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), false)

    /** Risca o produto que não estava riscado, ou tira o risco de quem estava ([checked] é o estado atual). */
    fun onToggleCheck(id: ListItemId, checked: Boolean) {
        viewModelScope.launch { setItemChecked(id, !checked) }
    }

    /** "Terminei": grava a compra, tira os riscados e mostra quantos foram comprados e quantos ficaram. */
    fun onFinish() {
        viewModelScope.launch {
            finishShopping()?.let { mutableNotice.value = ListNotice.Finished(it.bought, it.remaining) }
        }
    }

    /** "Sim, jogar fora" da confirmação: tira todos os produtos, riscados ou não, e mostra o aviso. */
    fun onClearList() {
        viewModelScope.launch {
            if (clearList()) mutableNotice.value = ListNotice.Cleared
        }
    }

    /** "Tirar da lista", ação do TalkBack na linha (#71): tira o produto e mostra o aviso com [name]. */
    fun onRemove(id: ListItemId, name: String) {
        viewModelScope.launch {
            if (removeItem(id)) mutableNotice.value = ListNotice.Removed(name)
        }
    }

    /** O aviso saiu da tela (sumiu sozinho ou a pessoa tocou em "Desfazer"). */
    fun onNoticeShown() {
        mutableNotice.value = null
    }

    /** "Desfazer" do aviso (ex.: "Feijão saiu da lista."): desfaz a última ação. */
    fun onUndo() {
        viewModelScope.launch { undoLastAction() }
    }

    private companion object {
        /** Mantém a coleta numa rotação de tela, sem reabrir a consulta. */
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

/** Linha do item, também usada na lista "Adicionados" da tela de adicionar (#122). */
internal fun ListItem.toRow(index: CatalogIndex) = ListRowUi(
    id = id,
    name = name,
    detail = detail,
    quantity = quantity,
    checked = checked,
    icon = iconRes(index),
)
