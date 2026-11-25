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

/** A edição ensina o termo preferido quando o nome muda (#44). */
class EditItemUseCaseTest {
    private val database = createTestDatabase()
    private val repository = RoomShoppingListRepository(database, EmbeddedCatalogRepository(), StepClock())
    private val preferredTerms = InMemoryPreferredTermRepository()
    private val edit = EditItemUseCase(
        repository,
        UndoStack(),
        LearnPreferredTermUseCase(CatalogIndex(EmbeddedCatalog.catalog), preferredTerms),
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
}
