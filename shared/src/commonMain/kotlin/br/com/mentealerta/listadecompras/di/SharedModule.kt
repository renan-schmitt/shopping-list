package br.com.mentealerta.listadecompras.di

import br.com.mentealerta.listadecompras.catalog.CatalogRepository
import br.com.mentealerta.listadecompras.data.EmbeddedCatalogRepository
import br.com.mentealerta.listadecompras.data.RoomShoppingListRepository
import br.com.mentealerta.listadecompras.data.database.AppDatabase
import br.com.mentealerta.listadecompras.domain.GetSectionsUseCase
import br.com.mentealerta.listadecompras.domain.ShoppingListRepository
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
    singleOf(::RoomShoppingListRepository) bind ShoppingListRepository::class
    single { get<AppDatabase>().listItemDao() }
    single { get<AppDatabase>().purchaseDao() }
    single { get<AppDatabase>().learnedPreferenceDao() }
    factoryOf(::GetSectionsUseCase)
}
