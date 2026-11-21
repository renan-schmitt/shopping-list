package br.com.mentealerta.listadecompras.ui.list

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.domain.ObserveListUseCase
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.ui.catalog.iconRes
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Estado da tela da lista. */
sealed interface ListUiState {
    /** O banco ainda não respondeu: a tela fica em branco, sem mostrar "lista vazia" por engano. */
    data object Loading : ListUiState

    data object Empty : ListUiState

    data class Content(val sections: List<SectionUi>) : ListUiState
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

/** Tela da lista (#50): a lista agrupada por seção, atualizada a cada mudança no banco. */
class ListViewModel(observeList: ObserveListUseCase, catalogIndex: CatalogIndex) : ViewModel() {
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
