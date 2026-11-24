package br.com.mentealerta.listadecompras.di

import br.com.mentealerta.listadecompras.ui.add.AddViewModel
import br.com.mentealerta.listadecompras.ui.edit.EditViewModel
import br.com.mentealerta.listadecompras.ui.history.HistoryViewModel
import br.com.mentealerta.listadecompras.ui.list.ListViewModel
import br.com.mentealerta.listadecompras.voice.AndroidVoiceRecognizer
import br.com.mentealerta.listadecompras.voice.VoiceRecognizer
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Dependências do Android: ViewModels e integrações de plataforma. Soma-se ao [sharedModule]. */
val appModule = module {
    viewModelOf(::ListViewModel)
    // Sem o despachante da busca: fica o padrão do construtor.
    viewModel { AddViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single<VoiceRecognizer> { AndroidVoiceRecognizer(androidContext()) }
    viewModelOf(::EditViewModel)
    viewModelOf(::HistoryViewModel)
}
