package br.com.mentealerta.listadecompras.ui.list

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.domain.ObserveListUseCase
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.domain.SetItemCheckedUseCase
import br.com.mentealerta.listadecompras.ui.catalog.iconRes
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

/**
 * Tela da lista (#50): a lista agrupada por seção, atualizada a cada mudança no banco. Riscar e tirar
 * o risco (#51) entram na pilha do "Ops, voltar".
 */
class ListViewModel(
    observeList: ObserveListUseCase,
    catalogIndex: CatalogIndex,
    private val setItemChecked: SetItemCheckedUseCase,
) : ViewModel() {
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
