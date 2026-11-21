package br.com.mentealerta.listadecompras.ui.edit

import br.com.mentealerta.listadecompras.catalog.Catalog
import br.com.mentealerta.listadecompras.catalog.CatalogIcon
import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.CatalogProduct
import br.com.mentealerta.listadecompras.catalog.CatalogSection
import br.com.mentealerta.listadecompras.catalog.IconFit
import br.com.mentealerta.listadecompras.domain.EditItemUseCase
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.domain.ListSection
import br.com.mentealerta.listadecompras.domain.ObserveListUseCase
import br.com.mentealerta.listadecompras.domain.RemoveItemUseCase
import br.com.mentealerta.listadecompras.domain.RemovedItems
import br.com.mentealerta.listadecompras.domain.RenameItemUseCase
import br.com.mentealerta.listadecompras.domain.SetItemCheckedUseCase
import br.com.mentealerta.listadecompras.domain.ShoppingListRepository
import br.com.mentealerta.listadecompras.domain.UndoLastActionUseCase
import br.com.mentealerta.listadecompras.domain.undo.UndoStack
import br.com.mentealerta.listadecompras.normalization.ItemTextParser
import br.com.mentealerta.listadecompras.ui.list.ListViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.time.Instant

/**
 * Catálogo pequeno e repositório falso, com estado, para os testes da edição (#55): a lista em
 * [items], na ordem em que os itens entraram; tirar e o "Ops, voltar" devolvem o item ao lugar.
 */
class EditFixture {
    val produce = CatalogSection("produce", "Hortifrúti", 1, CatalogIcon.Fluent("Leafy green"))
    val pantry = CatalogSection("pantry", "Mercearia", 2, CatalogIcon.Fluent("Hot beverage"))
    private val other = CatalogSection(CatalogSection.OTHER_ID, "Outros", 3, CatalogIcon.Fluent("Shopping cart"))

    val index = CatalogIndex(
        Catalog(
            sections = listOf(produce, pantry, other),
            products = listOf(
                CatalogProduct("banana", "Banana", "produce", 10, CatalogIcon.Fluent("Banana"), IconFit.EXACT),
                CatalogProduct("beans", "Feijão", "pantry", 10, null, null),
            ),
            terms = emptyList(),
            replacements = emptyMap(),
            retiredProductIds = emptySet(),
        ),
    )

    val undoStack = UndoStack()

    /** Ordem de entrada de cada item; fica guardada para o item voltar ao mesmo lugar. */
    private val order = mutableMapOf<ListItemId, Long>()
    private val state = MutableStateFlow<List<ListItem>>(emptyList())

    val items: List<ListItem> get() = state.value

    /** Põe o item direto na lista, sem passar pela pilha do "Ops, voltar". */
    fun put(item: ListItem): ListItem = item.also {
        order[it.id] = order.size.toLong()
        state.update { list -> list + it }
    }

    val repository = object : ShoppingListRepository {
        override fun observeSections(): Flow<List<ListSection>> = state.map { list ->
            index.catalog.sections.mapNotNull { section ->
                list.filter { it.sectionId == section.id }.takeIf { it.isNotEmpty() }?.let { ListSection(section, it) }
            }
        }

        override suspend fun add(item: ListItem) {
            put(item)
        }

        override suspend fun update(item: ListItem): ListItem? {
            val previous = state.value.find { it.id == item.id } ?: return null
            state.update { list -> list.map { if (it.id == item.id) item else it } }
            return previous
        }

        override suspend fun setChecked(id: ListItemId, checked: Boolean): ListItem? {
            val previous = state.value.find { it.id == id }?.takeIf { it.checked != checked } ?: return null
            update(previous.copy(checked = checked))
            return previous
        }

        override suspend fun remove(id: ListItemId): RemovedItems {
            val item = state.value.find { it.id == id } ?: return RemovedItems.NONE
            state.update { list -> list - item }
            return RemovedItems.of(listOf(item to Instant.fromEpochMilliseconds(order.getValue(id))))
        }

        override suspend fun clear(): RemovedItems = RemovedItems.NONE

        override suspend fun restore(removed: RemovedItems) {
            state.update { list -> (list + removed.items).sortedBy { order.getValue(it.id) } }
        }
    }

    fun editViewModel() = EditViewModel(
        ObserveListUseCase(repository),
        index,
        RenameItemUseCase(ItemTextParser(index)),
        EditItemUseCase(repository, undoStack),
        RemoveItemUseCase(repository, undoStack),
    )

    fun listViewModel() = ListViewModel(
        ObserveListUseCase(repository),
        index,
        SetItemCheckedUseCase(repository, undoStack),
        UndoLastActionUseCase(repository, undoStack),
    )
}
