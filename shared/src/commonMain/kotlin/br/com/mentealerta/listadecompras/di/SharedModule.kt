package br.com.mentealerta.listadecompras.di

import br.com.mentealerta.listadecompras.catalog.CatalogRepository
import br.com.mentealerta.listadecompras.data.EmbeddedCatalogRepository
import br.com.mentealerta.listadecompras.data.database.AppDatabase
import br.com.mentealerta.listadecompras.domain.GetSectionsUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Dependências do código compartilhado (Android e iOS): repositórios como `single`,
 * casos de uso como `factory`. Banco e preferências vêm do [platformDataModule]; cada plataforma
 * soma o próprio módulo a este.
 */
val sharedModule = module {
    includes(platformDataModule)

    singleOf(::EmbeddedCatalogRepository) bind CatalogRepository::class
    single { get<AppDatabase>().listItemDao() }
    single { get<AppDatabase>().purchaseDao() }
    single { get<AppDatabase>().learnedPreferenceDao() }
    factoryOf(::GetSectionsUseCase)
}
