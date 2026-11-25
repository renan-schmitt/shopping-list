package br.com.mentealerta.listadecompras.di

import br.com.mentealerta.listadecompras.BuildConfig
import br.com.mentealerta.listadecompras.ads.AdDays
import br.com.mentealerta.listadecompras.ads.AdMobInterstitialAds
import br.com.mentealerta.listadecompras.ads.InterstitialAds
import br.com.mentealerta.listadecompras.ads.createAdDays
import br.com.mentealerta.listadecompras.billing.CurrentActivity
import br.com.mentealerta.listadecompras.billing.createBillingStore
import br.com.mentealerta.listadecompras.premium.BillingStore
import br.com.mentealerta.listadecompras.ui.add.AddViewModel
import br.com.mentealerta.listadecompras.ui.edit.EditViewModel
import br.com.mentealerta.listadecompras.ui.fullversion.FullVersionViewModel
import br.com.mentealerta.listadecompras.ui.history.HistoryViewModel
import br.com.mentealerta.listadecompras.ui.list.ListViewModel
import br.com.mentealerta.listadecompras.ui.settings.SettingsViewModel
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
    viewModel {
        AddViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get())
    }
    single<VoiceRecognizer> { AndroidVoiceRecognizer(androidContext()) }
    single { CurrentActivity() }
    // Release: Google Play Billing; debug: loja falsa (src/debug)
    single<BillingStore> { createBillingStore(androidContext(), get()) }
    viewModelOf(::EditViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::FullVersionViewModel)
    viewModelOf(::SettingsViewModel)
    // Anúncios da versão grátis (#89); no debug, um minuto vale um dia da primeira semana
    single<InterstitialAds> { AdMobInterstitialAds(androidContext(), get(), BuildConfig.ADMOB_INTERSTITIAL_ID) }
    single<AdDays> { createAdDays() }
}
