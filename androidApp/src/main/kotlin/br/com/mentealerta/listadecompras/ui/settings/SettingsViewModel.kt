package br.com.mentealerta.listadecompras.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.mentealerta.listadecompras.domain.ObservePersonalizedAdsUseCase
import br.com.mentealerta.listadecompras.domain.SetPersonalizedAdsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado da tela de ajustes. */
sealed interface SettingsUiState {
    /** As preferências ainda não responderam: a chave não aparece, para não piscar no lugar errado. */
    data object Loading : SettingsUiState

    /** [personalizedAds]: chave "Anúncios pelo meu interesse". */
    data class Content(val personalizedAds: Boolean) : SettingsUiState
}

/**
 * Ajustes (#89; a tela completa vem na #70): por enquanto só a chave "Anúncios pelo meu interesse"
 * (plano §3.7), ligada por padrão. Desligada, o app pede só anúncios não personalizados.
 */
class SettingsViewModel(
    observePersonalizedAds: ObservePersonalizedAdsUseCase,
    private val setPersonalizedAds: SetPersonalizedAdsUseCase,
) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> = observePersonalizedAds()
        .map<Boolean, SettingsUiState> { SettingsUiState.Content(personalizedAds = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SettingsUiState.Loading)

    fun onPersonalizedAdsChange(personalized: Boolean) {
        viewModelScope.launch { setPersonalizedAds(personalized) }
    }

    private companion object {
        /** Mantém a coleta numa rotação de tela, sem reabrir a consulta. */
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
