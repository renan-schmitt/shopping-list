package br.com.mentealerta.listadecompras.di

import br.com.mentealerta.listadecompras.catalog.CatalogRepository
import br.com.mentealerta.listadecompras.domain.GetSectionsUseCase
import br.com.mentealerta.listadecompras.domain.InMemoryPreferredTermRepository
import br.com.mentealerta.listadecompras.domain.PreferredTermRepository
import br.com.mentealerta.listadecompras.domain.SearchProductsUseCase
import br.com.mentealerta.listadecompras.domain.undo.UndoStack
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
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
        // Sem o Context do Android aqui, o banco não sobe: os termos preferidos ficam em memória.
        val testModule = module { single<PreferredTermRepository> { InMemoryPreferredTermRepository() } }
        startKoin { modules(sharedModule, testModule) }
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

    @Test
    fun undoStackIsSingle() {
        assertSame(get<UndoStack>(), get<UndoStack>())
    }

    @Test
    fun searchProductsUseCaseFindsInTheCatalog() = runTest {
        assertTrue(get<SearchProductsUseCase>()("maca").found.isNotEmpty())
    }
}
