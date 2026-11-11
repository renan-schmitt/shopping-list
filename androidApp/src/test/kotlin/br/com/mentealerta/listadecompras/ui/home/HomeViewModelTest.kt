package br.com.mentealerta.listadecompras.ui.home

import app.cash.turbine.test
import br.com.mentealerta.listadecompras.catalog.Catalog
import br.com.mentealerta.listadecompras.catalog.CatalogIcon
import br.com.mentealerta.listadecompras.catalog.CatalogRepository
import br.com.mentealerta.listadecompras.catalog.CatalogSection
import br.com.mentealerta.listadecompras.domain.GetSectionsUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeViewModelTest {
    private val sections = listOf(
        CatalogSection(id = "fruits", name = "Frutas", order = 1, icon = CatalogIcon.Fluent("Red apple")),
        CatalogSection(id = "other", name = "Outros", order = 2, icon = CatalogIcon.Fluent("Shopping cart")),
    )

    private val fakeRepository = object : CatalogRepository {
        override fun catalog() = Catalog(
            sections = sections,
            products = emptyList(),
            terms = emptyList(),
            replacements = emptyMap(),
            retiredProductIds = emptySet(),
        )
    }

    @Test
    fun uiStateExposesTheCatalogSections() = runTest {
        val viewModel = HomeViewModel(GetSectionsUseCase(fakeRepository))

        viewModel.uiState.test {
            assertEquals(HomeUiState(sections = sections), awaitItem())
        }
    }
}
