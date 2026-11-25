package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.time.Instant

/** Compras ao meio-dia em Brasília a partir de [first], com os intervalos dados em dias. */
fun buyEvery(productId: String?, name: String, first: String, vararg intervals: Int): List<Purchase> {
    var day = LocalDate.parse(first)
    val days = listOf(day) + intervals.map {
        day = day.plus(it, DateTimeUnit.DAY)
        day
    }
    return days.map { buy(productId, name, it.toString()) }
}

/** Uma compra de um produto ao meio-dia em Brasília, no [day]. */
fun buy(productId: String?, name: String, day: String) = Purchase(
    PurchaseId.random(),
    Instant.parse("${day}T15:00:00Z"),
    listOf(PurchasedItem(productId, name, quantity = null)),
)

/** Histórico em memória, para os testes da previsão. */
class InMemoryHistoryRepository : PurchaseHistoryRepository {
    val purchases = MutableStateFlow(emptyList<Purchase>())

    override fun observePurchases(since: Instant?): Flow<List<Purchase>> =
        purchases.map { all -> all.filter { since == null || it.date >= since } }

    override fun observeHasPurchasesBefore(before: Instant): Flow<Boolean> =
        purchases.map { all -> all.any { it.date < before } }
}

/** "Não" do "Está faltando?" em memória. */
class InMemoryDismissalRepository : SuggestionDismissalRepository {
    val values = MutableStateFlow(emptyMap<String, Instant>())

    override fun observeDismissals(): Flow<Map<String, Instant>> = values

    override suspend fun dismiss(key: String, at: Instant) {
        values.value += key to at
    }
}

/** Só a leitura da lista; as seções não importam para a previsão. */
class ReadOnlyListRepository(private val index: CatalogIndex) : ShoppingListRepository {
    val items = MutableStateFlow(emptyList<ListItem>())

    override fun observeSections(): Flow<List<ListSection>> = items.map { items ->
        items.groupBy {
            it.sectionId
        }.map { (sectionId, inSection) -> ListSection(index.section(sectionId)!!, inSection) }
    }

    override suspend fun add(item: ListItem) = error("não usado")

    override suspend fun update(item: ListItem): ListItem? = error("não usado")

    override suspend fun setChecked(id: ListItemId, checked: Boolean): ListItem? = error("não usado")

    override suspend fun remove(id: ListItemId): RemovedItems = error("não usado")

    override suspend fun clear(): RemovedItems = error("não usado")

    override suspend fun restore(removed: RemovedItems) = error("não usado")

    override suspend fun finishShopping(): FinishedShopping? = error("não usado")

    override suspend fun undoFinishShopping(finished: FinishedShopping) = error("não usado")
}
