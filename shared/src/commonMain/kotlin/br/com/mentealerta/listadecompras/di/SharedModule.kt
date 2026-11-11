package br.com.mentealerta.listadecompras.di

import br.com.mentealerta.listadecompras.catalog.CatalogRepository
import br.com.mentealerta.listadecompras.data.EmbeddedCatalogRepository
import br.com.mentealerta.listadecompras.domain.GetSectionsUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Dependências do código compartilhado (Android e iOS): repositórios como `single`,
 * casos de uso como `factory`. Cada plataforma soma o próprio módulo a este.
 */
val sharedModule = module {
    singleOf(::EmbeddedCatalogRepository) bind CatalogRepository::class
    factoryOf(::GetSectionsUseCase)
}
