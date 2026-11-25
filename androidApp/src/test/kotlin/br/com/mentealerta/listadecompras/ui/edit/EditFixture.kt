package br.com.mentealerta.listadecompras.ui.edit

import br.com.mentealerta.listadecompras.catalog.Catalog
import br.com.mentealerta.listadecompras.catalog.CatalogIcon
import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.CatalogProduct
import br.com.mentealerta.listadecompras.catalog.CatalogSection
import br.com.mentealerta.listadecompras.catalog.IconFit
import br.com.mentealerta.listadecompras.domain.AcceptSuggestionUseCase
import br.com.mentealerta.listadecompras.domain.ClearListUseCase
import br.com.mentealerta.listadecompras.domain.DismissSuggestionUseCase
import br.com.mentealerta.listadecompras.domain.EditItemUseCase
import br.com.mentealerta.listadecompras.domain.FinishShoppingUseCase
import br.com.mentealerta.listadecompras.domain.FinishedShopping
import br.com.mentealerta.listadecompras.domain.LearnPreferredTermUseCase
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.domain.ListSection
import br.com.mentealerta.listadecompras.domain.ObserveFeaturesUseCase
import br.com.mentealerta.listadecompras.domain.ObserveListUseCase
import br.com.mentealerta.listadecompras.domain.ObserveOverdueProductsUseCase
import br.com.mentealerta.listadecompras.domain.ObservePremiumUseCase
import br.com.mentealerta.listadecompras.domain.ObservePurchaseHistoryUseCase
import br.com.mentealerta.listadecompras.domain.Purchase
import br.com.mentealerta.listadecompras.domain.PurchaseHistoryRepository
import br.com.mentealerta.listadecompras.domain.RememberSectionUseCase
import br.com.mentealerta.listadecompras.domain.RemoveItemUseCase
import br.com.mentealerta.listadecompras.domain.RemovedItems
import br.com.mentealerta.listadecompras.domain.RenameItemUseCase
import br.com.mentealerta.listadecompras.domain.SetItemCheckedUseCase
import br.com.mentealerta.listadecompras.domain.ShoppingListRepository
import br.com.mentealerta.listadecompras.domain.UndoLastActionUseCase
import br.com.mentealerta.listadecompras.domain.undo.UndoStack
import br.com.mentealerta.listadecompras.normalization.ItemTextParser
import br.com.mentealerta.listadecompras.ui.FakePreferredTermRepository
import br.com.mentealerta.listadecompras.ui.FakeSectionChoiceRepository
import br.com.mentealerta.listadecompras.ui.FakeSuggestionDismissalRepository
import br.com.mentealerta.listadecompras.ui.fullversion.FakePremiumRepository
import br.com.mentealerta.listadecompras.ui.history.HistoryViewModel
import br.com.mentealerta.listadecompras.ui.list.AdsFixture
import br.com.mentealerta.listadecompras.ui.list.ListViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Catálogo pequeno e repositório falso, com estado, para os testes da edição (#55), do "Terminei"
 * (#57), do "Jogar a lista fora" (#58) e do "O que já comprei" (#66): a lista em [items], na ordem em
 * que os itens entraram, e o histórico em [purchases], da compra mais recente para a mais antiga; tirar
 * e o "Desfazer" devolvem o item ao lugar. O relógio fica parado em [now], em UTC.
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

    /** Versão grátis até comprar pela loja falsa. */
    val premium = FakePremiumRepository()

    /** Ordem de entrada de cada item; fica guardada para o item voltar ao mesmo lugar. */
    private val order = mutableMapOf<ListItemId, Long>()
    private val state = MutableStateFlow<List<ListItem>>(emptyList())

    val items: List<ListItem> get() = state.value

    /** Agora, para o "Terminei" e o histórico. */
    val now: Instant = Instant.parse("2026-11-23T15:00:00Z")
    private val clock = object : Clock {
        override fun now(): Instant = now
    }

    private val purchaseState = MutableStateFlow<List<Purchase>>(emptyList())

    /** Compras gravadas pelo "Terminei" ou por [putPurchase], da mais recente para a mais antiga. */
    val purchases: List<Purchase> get() = purchaseState.value

    /** Põe a compra direto no histórico. */
    fun putPurchase(purchase: Purchase) {
        purchaseState.update { (it + purchase).sortedByDescending(Purchase::date) }
    }

    /** Filtra pela data como o banco. */
    val historyRepository = object : PurchaseHistoryRepository {
        override fun observePurchases(since: Instant?): Flow<List<Purchase>> =
            purchaseState.map { all -> all.filter { since == null || it.date >= since } }

        override fun observeHasPurchasesBefore(before: Instant): Flow<Boolean> =
            purchaseState.map { all -> all.any { it.date < before } }
    }

    /** Põe o item direto na lista, sem passar pela pilha do "Desfazer". */
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
            return removedItemsOf(listOf(item))
        }

        override suspend fun clear(): RemovedItems {
            val all = state.value.takeIf { it.isNotEmpty() } ?: return RemovedItems.NONE
            state.value = emptyList()
            return removedItemsOf(all)
        }

        override suspend fun restore(removed: RemovedItems) {
            state.update { list -> (list + removed.items).sortedBy { order.getValue(it.id) } }
        }

        override suspend fun finishShopping(): FinishedShopping? {
            val checked = state.value.filter { it.checked }.takeIf { it.isNotEmpty() } ?: return null
            val purchase = Purchase.create(now, checked)
            putPurchase(purchase)
            state.update { list -> list - checked.toSet() }
            return FinishedShopping(purchase, removedItemsOf(checked), remaining = state.value.size)
        }

        override suspend fun undoFinishShopping(finished: FinishedShopping) {
            purchaseState.update { list -> list.filterNot { it.id == finished.purchase.id } }
            restore(finished.removed)
        }
    }

    private fun removedItemsOf(items: List<ListItem>) =
        RemovedItems.of(items.map { it to Instant.fromEpochMilliseconds(order.getValue(it.id)) })

    /** Anúncios (#89): primeira semana, a não ser que o teste mude. */
    val ads = AdsFixture(premium, repository)

    /** Termos que a pessoa usa (#44), aprendidos ao renomear. */
    val preferredTerms = FakePreferredTermRepository()

    /** Seções escolhidas pela pessoa (#56). */
    val sectionChoices = FakeSectionChoiceRepository()
    private val rememberSection get() = RememberSectionUseCase(index, sectionChoices)

    /** O "Não" do "Está faltando?" (#64). */
    val dismissals = FakeSuggestionDismissalRepository()

    fun editViewModel() = EditViewModel(
        ObserveListUseCase(repository),
        index,
        RenameItemUseCase(ItemTextParser(index)),
        EditItemUseCase(repository, undoStack, LearnPreferredTermUseCase(index, preferredTerms), rememberSection),
        RemoveItemUseCase(repository, undoStack),
        rememberSection,
    )

    fun listViewModel() = ListViewModel(
        ObserveListUseCase(repository),
        index,
        SetItemCheckedUseCase(repository, undoStack),
        FinishShoppingUseCase(repository, undoStack),
        ClearListUseCase(repository, undoStack),
        RemoveItemUseCase(repository, undoStack),
        UndoLastActionUseCase(repository, undoStack),
        observeHistory(),
        ObservePremiumUseCase(premium),
        ads.useCases,
        ObserveOverdueProductsUseCase(historyRepository, repository, dismissals, index, clock, TimeZone.UTC),
        AcceptSuggestionUseCase(repository, rememberSection, index),
        DismissSuggestionUseCase(dismissals, clock),
    )

    fun historyViewModel() = HistoryViewModel(observeHistory(), index)

    private fun observeHistory() =
        ObservePurchaseHistoryUseCase(historyRepository, ObserveFeaturesUseCase(premium), clock, TimeZone.UTC)
}
