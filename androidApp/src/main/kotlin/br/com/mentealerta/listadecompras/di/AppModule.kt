package br.com.mentealerta.listadecompras.di

import br.com.mentealerta.listadecompras.ui.add.AddViewModel
import br.com.mentealerta.listadecompras.ui.edit.EditViewModel
import br.com.mentealerta.listadecompras.ui.list.ListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Dependências do Android: ViewModels e integrações de plataforma. Soma-se ao [sharedModule]. */
val appModule = module {
    viewModelOf(::ListViewModel)
    // Sem o despachante da busca: fica o padrão do construtor.
    viewModel { AddViewModel(get(), get(), get(), get(), get()) }
    viewModelOf(::EditViewModel)
}
