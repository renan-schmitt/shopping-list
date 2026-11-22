package br.com.mentealerta.listadecompras.ui.list

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.domain.FinishShoppingUseCase
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.domain.ObserveListUseCase
import br.com.mentealerta.listadecompras.domain.Quantity
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
    }
}

/** Seção com título e os produtos dela, na ordem em que entraram. */
data class SectionUi(val id: String, val name: String, val rows: List<ListRowUi>)

/** Linha do produto: marca de riscar → nome (com detalhe e quantidade) → ícone. */
data class ListRowUi(
    val id: ListItemId,
    val name: String,
    val detail: String? = null,
    val quantity: Quantity? = null,
    val checked: Boolean = false,
    @DrawableRes val icon: Int,
)

/** Aviso com "Ops, voltar" que a tela mostra depois de uma ação. */
sealed interface ListNotice {
    /** "Terminei" (#57): [bought] produtos comprados, [remaining] ficaram na lista. */
    data class Finished(val bought: Int, val remaining: Int) : ListNotice
}

/**
 * Tela da lista (#50): a lista agrupada por seção, atualizada a cada mudança no banco. Riscar e tirar
 * o risco (#51) e o "Terminei" (#57) entram na pilha do "Ops, voltar"; [onUndo] desfaz a última ação.
 */
class ListViewModel(
    observeList: ObserveListUseCase,
    catalogIndex: CatalogIndex,
    private val setItemChecked: SetItemCheckedUseCase,
    private val finishShopping: FinishShoppingUseCase,
    private val undoLastAction: UndoLastActionUseCase,
) : ViewModel() {
    private val mutableNotice = MutableStateFlow<ListNotice?>(null)

    /** Aviso a mostrar; a tela chama [onNoticeShown] quando ele sai. Fica guardado numa rotação de tela. */
    val notice: StateFlow<ListNotice?> = mutableNotice.asStateFlow()

    val uiState: StateFlow<ListUiState> = observeList()
        .map { sections ->
            if (sections.isEmpty()) {
                ListUiState.Empty
            } else {
                ListUiState.Content(
                    sections.map { (section, items) ->
                        SectionUi(section.id, section.name, items.map { it.toRow(catalogIndex) })
                    },
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ListUiState.Loading)

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

    /** O aviso saiu da tela (sumiu sozinho ou a pessoa tocou em "Ops, voltar"). */
    fun onNoticeShown() {
        mutableNotice.value = null
    }

    /** "Ops, voltar" do aviso (ex.: "Feijão saiu da lista."): desfaz a última ação. */
    fun onUndo() {
        viewModelScope.launch { undoLastAction() }
    }

    private companion object {
        /** Mantém a coleta numa rotação de tela, sem reabrir a consulta. */
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

private fun ListItem.toRow(index: CatalogIndex) = ListRowUi(
    id = id,
    name = name,
    detail = detail,
    quantity = quantity,
    checked = checked,
    icon = iconRes(index),
)
