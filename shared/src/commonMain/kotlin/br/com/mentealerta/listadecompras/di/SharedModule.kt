package br.com.mentealerta.listadecompras.di

import br.com.mentealerta.listadecompras.ads.AdPreferences
import br.com.mentealerta.listadecompras.catalog.CatalogRepository
import br.com.mentealerta.listadecompras.data.EmbeddedCatalogRepository
import br.com.mentealerta.listadecompras.data.RoomPurchaseHistoryRepository
import br.com.mentealerta.listadecompras.data.RoomShoppingListRepository
import br.com.mentealerta.listadecompras.data.StorePremiumRepository
import br.com.mentealerta.listadecompras.data.database.AppDatabase
import br.com.mentealerta.listadecompras.data.preferences.DataStoreAdPreferences
import br.com.mentealerta.listadecompras.data.preferences.DataStoreVoicePreferences
import br.com.mentealerta.listadecompras.domain.AddItemsUseCase
import br.com.mentealerta.listadecompras.domain.AdsUseCases
import br.com.mentealerta.listadecompras.domain.BuyPremiumUseCase
import br.com.mentealerta.listadecompras.domain.ClearListUseCase
import br.com.mentealerta.listadecompras.domain.EditItemUseCase
import br.com.mentealerta.listadecompras.domain.FindSameItemUseCase
import br.com.mentealerta.listadecompras.domain.FinishShoppingUseCase
import br.com.mentealerta.listadecompras.domain.GetPremiumPriceUseCase
import br.com.mentealerta.listadecompras.domain.GetSectionsUseCase
import br.com.mentealerta.listadecompras.domain.ObserveFeaturesUseCase
import br.com.mentealerta.listadecompras.domain.ObserveListUseCase
import br.com.mentealerta.listadecompras.domain.ObservePersonalizedAdsUseCase
import br.com.mentealerta.listadecompras.domain.ObservePremiumUseCase
import br.com.mentealerta.listadecompras.domain.ObservePurchaseHistoryUseCase
import br.com.mentealerta.listadecompras.domain.ParsePastedListUseCase
import br.com.mentealerta.listadecompras.domain.ParseSpeechUseCase
import br.com.mentealerta.listadecompras.domain.PurchaseHistoryRepository
import br.com.mentealerta.listadecompras.domain.RemoveItemUseCase
import br.com.mentealerta.listadecompras.domain.RenameItemUseCase
import br.com.mentealerta.listadecompras.domain.RestorePremiumUseCase
import br.com.mentealerta.listadecompras.domain.SearchProductsUseCase
import br.com.mentealerta.listadecompras.domain.SetItemCheckedUseCase
import br.com.mentealerta.listadecompras.domain.SetPersonalizedAdsUseCase
import br.com.mentealerta.listadecompras.domain.ShoppingListRepository
import br.com.mentealerta.listadecompras.domain.UndoLastActionUseCase
import br.com.mentealerta.listadecompras.domain.undo.UndoStack
import br.com.mentealerta.listadecompras.normalization.ItemTextParser
import br.com.mentealerta.listadecompras.premium.PremiumRepository
import br.com.mentealerta.listadecompras.voice.VoicePreferences
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.time.Clock

/**
 * Dependências do código compartilhado (Android e iOS): repositórios como `single`,
 * casos de uso como `factory`. Banco e preferências vêm do [platformDataModule]; cada plataforma
 * soma o próprio módulo a este, com a loja do celular (`BillingStore`), o anúncio (`InterstitialAds`)
 * e a contagem dos dias da primeira semana (`AdDays`).
 */
val sharedModule = module {
    includes(platformDataModule)

    single<Clock> { Clock.System }
    singleOf(::EmbeddedCatalogRepository) bind CatalogRepository::class
    single { get<CatalogRepository>().index() }
    singleOf(::RoomShoppingListRepository) bind ShoppingListRepository::class
    singleOf(::RoomPurchaseHistoryRepository) bind PurchaseHistoryRepository::class
    single { get<AppDatabase>().listItemDao() }
    single { get<AppDatabase>().purchaseDao() }
    single { get<AppDatabase>().learnedPreferenceDao() }
    single { UndoStack() }
    singleOf(::DataStoreVoicePreferences) bind VoicePreferences::class
    singleOf(::DataStoreAdPreferences) bind AdPreferences::class
    singleOf(::ItemTextParser)
    // Escopo próprio do construtor: a versão completa vale enquanto o app estiver aberto.
    single<PremiumRepository> { StorePremiumRepository(get(), get()) }
    factoryOf(::GetSectionsUseCase)
    factoryOf(::ObserveListUseCase)
    // Fuso do celular lido a cada tela aberta: o padrão do construtor.
    factory { ObservePurchaseHistoryUseCase(get(), get(), get()) }
    factoryOf(::AddItemsUseCase)
    factoryOf(::FindSameItemUseCase)
    factoryOf(::SearchProductsUseCase)
    factoryOf(::ParseSpeechUseCase)
    factoryOf(::ParsePastedListUseCase)
    factoryOf(::SetItemCheckedUseCase)
    factoryOf(::EditItemUseCase)
    factoryOf(::RenameItemUseCase)
    factoryOf(::RemoveItemUseCase)
    factoryOf(::ClearListUseCase)
    factoryOf(::FinishShoppingUseCase)
    factoryOf(::UndoLastActionUseCase)
    factoryOf(::ObservePremiumUseCase)
    factoryOf(::ObserveFeaturesUseCase)
    factoryOf(::GetPremiumPriceUseCase)
    factoryOf(::BuyPremiumUseCase)
    factoryOf(::RestorePremiumUseCase)
    factoryOf(::AdsUseCases)
    factoryOf(::ObservePersonalizedAdsUseCase)
    factoryOf(::SetPersonalizedAdsUseCase)
}
