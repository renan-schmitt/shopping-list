package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.CatalogSection
import br.com.mentealerta.listadecompras.catalog.EmbeddedCatalog
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Aprender o termo que a pessoa usa para cada produto (#44, plano §1.3). */
class LearnPreferredTermUseCaseTest {
    private val repository = InMemoryPreferredTermRepository()
    private val learn = LearnPreferredTermUseCase(CatalogIndex(EmbeddedCatalog.catalog), repository)

    private fun item(name: String, productId: String?, sectionId: String = "produce") =
        ListItem.create(name = name, productId = productId, sectionId = sectionId)

    @Test
    fun learnsTheRegionalismWithTheCatalogSpelling() = runTest {
        learn(item("Macaxeiras", "cassava"))

        assertEquals(mapOf("cassava" to "macaxeira"), repository.terms)
    }

    @Test
    fun learnsTheCatalogNameToo() = runTest {
        learn(item("mandioca", "cassava"))

        assertEquals(mapOf("cassava" to "Mandioca"), repository.terms)
    }

    @Test
    fun theLastTermUsedWins() = runTest {
        learn(item("aipim", "cassava"))
        learn(item("macaxeira", "cassava"))

        assertEquals(mapOf("cassava" to "macaxeira"), repository.terms)
    }

    @Test
    fun freeNameTeachesNothing() = runTest {
        learn(item("cacetinho da padaria", "french_bread", sectionId = "bakery"))
        learn(item("Pão quentinho", "french_bread", sectionId = "bakery"))

        assertEquals(emptyMap(), repository.terms)
    }

    @Test
    fun unknownProductTeachesNothing() = runTest {
        learn(item("xyzzy", productId = null, sectionId = CatalogSection.OTHER_ID))
        learn(item("aipim", productId = "not_in_catalog"))

        assertEquals(emptyMap(), repository.terms)
    }

    @Test
    fun termOfAnotherProductTeachesNothing() = runTest {
        learn(item("aipim", "potato"))

        assertEquals(emptyMap(), repository.terms)
    }
}
