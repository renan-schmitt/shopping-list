package br.com.mentealerta.listadecompras.di

import br.com.mentealerta.listadecompras.catalog.CatalogRepository
import br.com.mentealerta.listadecompras.data.EmbeddedCatalogRepository
import br.com.mentealerta.listadecompras.data.RoomShoppingListRepository
import br.com.mentealerta.listadecompras.data.database.AppDatabase
import br.com.mentealerta.listadecompras.domain.AddItemsUseCase
import br.com.mentealerta.listadecompras.domain.ClearListUseCase
import br.com.mentealerta.listadecompras.domain.EditItemUseCase
import br.com.mentealerta.listadecompras.domain.FindSameItemUseCase
import br.com.mentealerta.listadecompras.domain.FinishShoppingUseCase
import br.com.mentealerta.listadecompras.domain.GetSectionsUseCase
import br.com.mentealerta.listadecompras.domain.ObserveListUseCase
import br.com.mentealerta.listadecompras.domain.RemoveItemUseCase
import br.com.mentealerta.listadecompras.domain.RenameItemUseCase
import br.com.mentealerta.listadecompras.domain.SearchProductsUseCase
import br.com.mentealerta.listadecompras.domain.SetItemCheckedUseCase
import br.com.mentealerta.listadecompras.domain.ShoppingListRepository
import br.com.mentealerta.listadecompras.domain.UndoLastActionUseCase
import br.com.mentealerta.listadecompras.domain.undo.UndoStack
import br.com.mentealerta.listadecompras.normalization.ItemTextParser
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.time.Clock

/**
 * Dependências do código compartilhado (Android e iOS): repositórios como `single`,
 * casos de uso como `factory`. Banco e preferências vêm do [platformDataModule]; cada plataforma
 * soma o próprio módulo a este.
 */
val sharedModule = module {
    includes(platformDataModule)

    single<Clock> { Clock.System }
    singleOf(::EmbeddedCatalogRepository) bind CatalogRepository::class
    single { get<CatalogRepository>().index() }
    singleOf(::RoomShoppingListRepository) bind ShoppingListRepository::class
    single { get<AppDatabase>().listItemDao() }
    single { get<AppDatabase>().purchaseDao() }
    single { get<AppDatabase>().learnedPreferenceDao() }
    single { UndoStack() }
    singleOf(::ItemTextParser)
    factoryOf(::GetSectionsUseCase)
    factoryOf(::ObserveListUseCase)
    factoryOf(::AddItemsUseCase)
    factoryOf(::FindSameItemUseCase)
    factoryOf(::SearchProductsUseCase)
    factoryOf(::SetItemCheckedUseCase)
    factoryOf(::EditItemUseCase)
    factoryOf(::RenameItemUseCase)
    factoryOf(::RemoveItemUseCase)
    factoryOf(::ClearListUseCase)
    factoryOf(::FinishShoppingUseCase)
    factoryOf(::UndoLastActionUseCase)
}
