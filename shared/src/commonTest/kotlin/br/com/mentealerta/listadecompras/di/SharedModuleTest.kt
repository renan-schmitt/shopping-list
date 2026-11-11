package br.com.mentealerta.listadecompras.di

import br.com.mentealerta.listadecompras.catalog.CatalogRepository
import br.com.mentealerta.listadecompras.domain.GetSectionsUseCase
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SharedModuleTest : KoinTest {
    @BeforeTest
    fun setUp() {
        startKoin { modules(sharedModule) }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun getSectionsUseCaseReturnsTheCatalogSections() {
        assertTrue(get<GetSectionsUseCase>()().isNotEmpty())
    }

    @Test
    fun catalogRepositoryIsSingle() {
        assertSame(get<CatalogRepository>(), get<CatalogRepository>())
    }
}
