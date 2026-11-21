package br.com.mentealerta.listadecompras.ui.add

import br.com.mentealerta.listadecompras.catalog.Catalog
import br.com.mentealerta.listadecompras.catalog.CatalogIcon
import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.CatalogProduct
import br.com.mentealerta.listadecompras.catalog.CatalogSection
import br.com.mentealerta.listadecompras.catalog.IconFit
import br.com.mentealerta.listadecompras.domain.AddItemsUseCase
import br.com.mentealerta.listadecompras.domain.EditItemUseCase
import br.com.mentealerta.listadecompras.domain.FindSameItemUseCase
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.domain.ListSection
import br.com.mentealerta.listadecompras.domain.RemovedItems
import br.com.mentealerta.listadecompras.domain.SearchProductsUseCase
import br.com.mentealerta.listadecompras.domain.ShoppingListRepository
import br.com.mentealerta.listadecompras.domain.undo.UndoStack
import br.com.mentealerta.listadecompras.normalization.ItemTextParser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Catálogo pequeno e repositório falso para os testes da tela de adicionar: guarda o que entrou na
 * lista em [added] e a lista como está em [items]. O "Ops, voltar" fica na [undoStack].
 */
class AddFixture {
    private val produce = CatalogSection("produce", "Hortifrúti", 1, CatalogIcon.Fluent("Leafy green"))
    private val other = CatalogSection(CatalogSection.OTHER_ID, "Outros", 2, CatalogIcon.Fluent("Shopping cart"))

    val index = CatalogIndex(
        Catalog(
            sections = listOf(produce, other),
            products = listOf(
                CatalogProduct("banana", "Banana", "produce", 10, CatalogIcon.Fluent("Banana"), IconFit.EXACT),
                CatalogProduct("apple", "Maçã", "produce", 20, CatalogIcon.Fluent("Red apple"), IconFit.EXACT),
            ),
            terms = emptyList(),
            replacements = emptyMap(),
            retiredProductIds = emptySet(),
        ),
    )

    val added = mutableListOf<ListItem>()
    val items = mutableListOf<ListItem>()
    val undoStack = UndoStack()

    private val repository = object : ShoppingListRepository {
        override fun observeSections(): Flow<List<ListSection>> = flow {
            emit(listOf(ListSection(other, items.toList())))
        }

        override suspend fun add(item: ListItem) {
            added += item
            items += item
        }

        override suspend fun update(item: ListItem): ListItem? {
            val index = items.indexOfFirst { it.id == item.id }.takeIf { it >= 0 } ?: return null
            return items[index].also { items[index] = item }
        }

        override suspend fun setChecked(id: ListItemId, checked: Boolean): ListItem? =
            items.find { it.id == id }?.let { update(it.copy(checked = checked)) }

        override suspend fun remove(id: ListItemId) = RemovedItems.NONE

        override suspend fun clear() = RemovedItems.NONE

        override suspend fun restore(removed: RemovedItems) = Unit
    }

    fun viewModel(searchDispatcher: CoroutineDispatcher = Dispatchers.Unconfined) = AddViewModel(
        SearchProductsUseCase(index, ItemTextParser(index)),
        index,
        AddItemsUseCase(repository),
        FindSameItemUseCase(repository),
        EditItemUseCase(repository, undoStack),
        searchDispatcher,
    )
}
