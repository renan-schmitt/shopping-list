package br.com.mentealerta.listadecompras.di

import br.com.mentealerta.listadecompras.ui.home.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Dependências do Android: ViewModels e integrações de plataforma. Soma-se ao [sharedModule]. */
val appModule = module {
    viewModelOf(::HomeViewModel)
}
