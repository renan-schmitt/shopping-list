package br.com.mentealerta.listadecompras.ui.home

import androidx.lifecycle.ViewModel
import br.com.mentealerta.listadecompras.catalog.CatalogSection
import br.com.mentealerta.listadecompras.domain.GetSectionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(val sections: List<CatalogSection> = emptyList())

/** Exemplo mínimo da arquitetura (UI → ViewModel → caso de uso → repositório); a tela da lista substitui na fase 1. */
class HomeViewModel(getSections: GetSectionsUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState(sections = getSections()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}
