package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.EmbeddedCatalog
import br.com.mentealerta.listadecompras.data.EmbeddedCatalogRepository
import br.com.mentealerta.listadecompras.data.RoomShoppingListRepository
import br.com.mentealerta.listadecompras.data.StepClock
import br.com.mentealerta.listadecompras.data.database.createTestDatabase
import br.com.mentealerta.listadecompras.normalization.ItemTextParser
import br.com.mentealerta.listadecompras.normalization.ParsedItem
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Produto repetido não duplica (#54): quando o produto que a pessoa quer pôr já está na lista. */
class FindSameItemUseCaseTest {
    private val database = createTestDatabase()
    private val repository = RoomShoppingListRepository(database, EmbeddedCatalogRepository(), StepClock())
    private val findSameItem = FindSameItemUseCase(repository)
    private val parser = ItemTextParser(CatalogIndex(EmbeddedCatalog.catalog))

    @AfterTest
    fun tearDown() {
        database.close()
    }

    private fun parsed(text: String): ParsedItem = checkNotNull(parser.parse(text))

    private suspend fun add(text: String): ListItem = parsed(text).toListItem().also { repository.add(it) }

    @Test
    fun emptyListHasNothingRepeated() = runTest {
        assertNull(findSameItem(parsed("leite")))
    }

    @Test
    fun sameCatalogProductIsFoundEvenWithAnotherNameOrQuantity() = runTest {
        add("arroz")
        val cassava = add("aipim")
        val milk = add("leite")

        assertEquals(cassava, findSameItem(parsed("mandioca")))
        assertEquals(milk, findSameItem(parsed("2 litros de leites")))
    }

    @Test
    fun otherProductIsNotRepeated() = runTest {
        add("leite")

        assertNull(findSameItem(parsed("café")))
    }

    @Test
    fun unknownProductComparesTheNormalizedName() = runTest {
        val item = add("Bergamotas")

        assertEquals(item, findSameItem(parsed("bérgamota")))
        assertNull(findSameItem(parsed("xyzzy")))
    }

    @Test
    fun differentDetailIsAnotherItem() = runTest {
        add("leite")
        val skimmed = add("leite desnatado")

        assertEquals(skimmed, findSameItem(parsed("Leite Desnatado")))
        assertNull(findSameItem(parsed("leite da marca X")))
    }

    @Test
    fun noDetailOnlyMatchesTheItemWithoutDetail() = runTest {
        add("leite desnatado")

        assertNull(findSameItem(parsed("leite")))
    }

    @Test
    fun checkedItemIsNotRepeated() = runTest {
        val milk = add("leite")
        repository.setChecked(milk.id, checked = true)

        assertNull(findSameItem(parsed("leite")))
    }

    @Test
    fun uncheckedItemIsFoundNextToACheckedOne() = runTest {
        val checked = add("leite")
        repository.setChecked(checked.id, checked = true)
        val unchecked = add("leite")

        assertEquals(unchecked, findSameItem(parsed("leite")))
    }
}
