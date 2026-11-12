package br.com.mentealerta.listadecompras.data

import androidx.room3.withWriteTransaction
import br.com.mentealerta.listadecompras.catalog.CatalogRepository
import br.com.mentealerta.listadecompras.catalog.CatalogSection
import br.com.mentealerta.listadecompras.data.database.AppDatabase
import br.com.mentealerta.listadecompras.data.database.ListItemEntity
import br.com.mentealerta.listadecompras.data.database.toDomain
import br.com.mentealerta.listadecompras.data.database.toEntity
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.domain.ListSection
import br.com.mentealerta.listadecompras.domain.RemovedItems
import br.com.mentealerta.listadecompras.domain.ShoppingListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

/**
 * Lista de compras no banco local (plano §4.3). A ordem dentro da seção vem de quando o item entrou
 * na lista ([ListItemEntity.addedAt]), que só muda ao tirar e pôr de novo.
 */
internal class RoomShoppingListRepository(
    private val database: AppDatabase,
    private val catalogRepository: CatalogRepository,
    private val clock: Clock,
) : ShoppingListRepository {
    private val dao = database.listItemDao()

    override fun observeSections(): Flow<List<ListSection>> =
        dao.observeAll().map { entities -> groupBySection(entities.map { it.toDomain() }) }

    override suspend fun add(item: ListItem) {
        dao.upsert(item.toEntity(addedAt = clock.now()))
    }

    override suspend fun update(item: ListItem): Boolean = dao.update(
        id = item.id.value,
        name = item.name,
        productId = item.productId,
        sectionId = item.sectionId,
        quantityAmount = item.quantity?.amount,
        quantityUnit = item.quantity?.unit,
        detail = item.detail,
        checked = item.checked,
    ) > 0

    override suspend fun setChecked(id: ListItemId, checked: Boolean): Boolean = dao.setChecked(id.value, checked) > 0

    override suspend fun remove(id: ListItemId): RemovedItems = database.withWriteTransaction {
        val entity = dao.getById(id.value) ?: return@withWriteTransaction RemovedItems.NONE
        dao.deleteById(id.value)
        removedItemsOf(listOf(entity))
    }

    override suspend fun clear(): RemovedItems = database.withWriteTransaction {
        val entities = dao.getAll()
        dao.deleteAll()
        removedItemsOf(entities)
    }

    override suspend fun restore(removed: RemovedItems) {
        if (removed.isEmpty()) return
        dao.upsertAll(removed.entries.map { it.item.toEntity(addedAt = it.addedAt) })
    }

    /** Agrupa na ordem das seções do catálogo; seção que o catálogo não tem vai para "Outros". */
    private fun groupBySection(items: List<ListItem>): List<ListSection> {
        val sections = catalogRepository.catalog().sections
        val knownIds = sections.mapTo(HashSet()) { it.id }
        val itemsBySection = items.groupBy { if (it.sectionId in knownIds) it.sectionId else CatalogSection.OTHER_ID }
        return sections.mapNotNull { section ->
            itemsBySection[section.id]?.let { ListSection(section, it) }
        }
    }

    private fun removedItemsOf(entities: List<ListItemEntity>) =
        RemovedItems(entities.map { RemovedItems.Entry(it.toDomain(), it.addedAt) })
}
