package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.CatalogSection
import br.com.mentealerta.listadecompras.catalog.EmbeddedCatalog
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** A seção escolhida fica lembrada pelo produto do catálogo ou pelo nome do desconhecido (#56). */
class RememberSectionUseCaseTest {
    private val index = CatalogIndex(EmbeddedCatalog.catalog)
    private val repository = InMemorySectionChoiceRepository()
    private val rememberSection = RememberSectionUseCase(index, repository)

    private val coconutSoap = ListItem.create(name = "Sabão de coco", productId = null, sectionId = "cleaning")

    @Test
    fun catalogProductIsRememberedByItsIdWhateverTheName() = runTest {
        rememberSection.learn(ListItem.create(name = "aipim", productId = "cassava", sectionId = "pantry"))

        val macaxeira = ListItem.create(name = "macaxeira", productId = "cassava", sectionId = "produce")

        assertEquals(mapOf("cassava" to "pantry"), repository.sections)
        assertEquals("pantry", rememberSection.choices().applyTo(macaxeira).sectionId)
    }

    @Test
    fun unknownProductIsRememberedByTheNormalizedName() = runTest {
        rememberSection.learn(coconutSoap)

        val plural = ListItem.create(name = "sabões de coco", productId = null, sectionId = CatalogSection.OTHER_ID)

        assertEquals(mapOf("sabao de coco" to "cleaning"), repository.sections)
        assertEquals("cleaning", rememberSection.choices().applyTo(plural).sectionId)
    }

    @Test
    fun keepsTheLastChoice() = runTest {
        rememberSection.learn(coconutSoap)
        rememberSection.learn(coconutSoap.copy(sectionId = "pantry"))

        assertEquals(mapOf("sabao de coco" to "pantry"), repository.sections)
    }

    @Test
    fun itemWithoutChoiceStaysAsItCame() = runTest {
        rememberSection.learn(coconutSoap)
        val milk = ListItem.create(name = "Leite", productId = "milk", sectionId = "deli_dairy")

        assertEquals(milk, rememberSection.choices().applyTo(milk))
    }

    @Test
    fun choiceOfASectionTheCatalogNoLongerHasIsIgnored() = runTest {
        repository.learn("sabao de coco", "gone")
        val item = coconutSoap.copy(sectionId = CatalogSection.OTHER_ID)

        assertEquals(item, rememberSection.choices().applyTo(item))
    }
}
