package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.EmbeddedCatalog
import br.com.mentealerta.listadecompras.data.EmbeddedCatalogRepository
import br.com.mentealerta.listadecompras.data.RoomShoppingListRepository
import br.com.mentealerta.listadecompras.data.StepClock
import br.com.mentealerta.listadecompras.data.database.createTestDatabase
import br.com.mentealerta.listadecompras.domain.undo.UndoStack
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/** A edição ensina o termo preferido quando o nome muda (#44) e a seção escolhida (#56). */
class EditItemUseCaseTest {
    private val database = createTestDatabase()
    private val repository = RoomShoppingListRepository(database, EmbeddedCatalogRepository(), StepClock())
    private val index = CatalogIndex(EmbeddedCatalog.catalog)
    private val preferredTerms = InMemoryPreferredTermRepository()
    private val sectionChoices = InMemorySectionChoiceRepository()
    private val edit = EditItemUseCase(
        repository,
        UndoStack(),
        LearnPreferredTermUseCase(index, preferredTerms),
        RememberSectionUseCase(index, sectionChoices),
    )

    private val bread = ListItem.create(name = "Pão francês", productId = "french_bread", sectionId = "bakery")

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun renamingTeachesTheNewTerm() = runTest {
        repository.add(bread)

        edit(bread.copy(name = "cacetinho"))

        assertEquals(mapOf("french_bread" to "cacetinho"), preferredTerms.terms)
    }

    @Test
    fun editingWithoutRenamingTeachesNothing() = runTest {
        repository.add(bread)

        edit(bread.increaseQuantity())

        assertEquals(emptyMap(), preferredTerms.terms)
    }

    @Test
    fun itemNoLongerOnTheListTeachesNothing() = runTest {
        assertFalse(edit(bread.copy(name = "cacetinho")))
        assertEquals(emptyMap(), preferredTerms.terms)
    }

    @Test
    fun choosingASectionRemembersItForTheProduct() = runTest {
        repository.add(bread)

        edit(bread.copy(sectionId = "pantry"), sectionChosen = true)

        assertEquals(mapOf("french_bread" to "pantry"), sectionChoices.sections)
    }

    @Test
    fun choosingASectionForAnUnknownProductRemembersTheName() = runTest {
        val soap = ListItem.create(name = "Sabão de coco", productId = null, sectionId = "other")
        repository.add(soap)

        edit(soap.copy(sectionId = "cleaning"), sectionChosen = true)

        assertEquals(mapOf("sabao de coco" to "cleaning"), sectionChoices.sections)
    }

    @Test
    fun choosingTheSameSectionRemembersNothing() = runTest {
        repository.add(bread)

        edit(bread.increaseQuantity(), sectionChosen = true)

        assertEquals(emptyMap(), sectionChoices.sections)
    }

    @Test
    fun sectionChangedByRenamingToAnotherProductIsNotRemembered() = runTest {
        repository.add(bread)

        edit(bread.copy(name = "leite", productId = "milk", sectionId = "deli_dairy"))

        assertEquals(emptyMap(), sectionChoices.sections)
    }

    @Test
    fun itemNoLongerOnTheListRemembersNoSection() = runTest {
        assertFalse(edit(bread.copy(sectionId = "pantry"), sectionChosen = true))
        assertEquals(emptyMap(), sectionChoices.sections)
    }
}
