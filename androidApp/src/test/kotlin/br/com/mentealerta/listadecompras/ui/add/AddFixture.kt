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
import br.com.mentealerta.listadecompras.domain.FinishedShopping
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.domain.ListSection
import br.com.mentealerta.listadecompras.domain.ObserveListUseCase
import br.com.mentealerta.listadecompras.domain.ParseSpeechUseCase
import br.com.mentealerta.listadecompras.domain.RemovedItems
import br.com.mentealerta.listadecompras.domain.SearchProductsUseCase
import br.com.mentealerta.listadecompras.domain.ShoppingListRepository
import br.com.mentealerta.listadecompras.domain.undo.UndoStack
import br.com.mentealerta.listadecompras.normalization.ItemTextParser
import br.com.mentealerta.listadecompras.voice.VoiceEvent
import br.com.mentealerta.listadecompras.voice.VoicePreferences
import br.com.mentealerta.listadecompras.voice.VoiceRecognizer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Catálogo pequeno e repositório falso para os testes da tela de adicionar: guarda o que entrou na
 * lista em [added] e a lista como está em [items], observada como no banco. O "Ops, voltar" fica
 * na [undoStack]. A voz é o [voice], que entrega o que o teste mandar.
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

    val voice = FakeVoiceRecognizer()
    val voicePreferences = FakeVoicePreferences()

    val added = mutableListOf<ListItem>()
    val items = mutableListOf<ListItem>()
    val undoStack = UndoStack()

    /** Muda a cada escrita, para a lista observada ("Adicionados") emitir de novo. */
    private val version = MutableStateFlow(0)

    /** Tira o item da lista, como o "Tirar da lista" faria em outra tela. */
    fun remove(id: ListItemId) {
        items.removeAll { it.id == id }
        version.value++
    }

    private val repository = object : ShoppingListRepository {
        override fun observeSections(): Flow<List<ListSection>> = version.map {
            listOf(ListSection(other, items.toList()))
        }

        override suspend fun add(item: ListItem) {
            added += item
            items += item
            version.value++
        }

        override suspend fun update(item: ListItem): ListItem? {
            val index = items.indexOfFirst { it.id == item.id }.takeIf { it >= 0 } ?: return null
            return items[index].also {
                items[index] = item
                version.value++
            }
        }

        override suspend fun setChecked(id: ListItemId, checked: Boolean): ListItem? =
            items.find { it.id == id }?.let { update(it.copy(checked = checked)) }

        override suspend fun remove(id: ListItemId) = RemovedItems.NONE

        override suspend fun clear() = RemovedItems.NONE

        override suspend fun restore(removed: RemovedItems) = Unit

        override suspend fun finishShopping(): FinishedShopping? = null

        override suspend fun undoFinishShopping(finished: FinishedShopping) = Unit
    }

    fun viewModel(searchDispatcher: CoroutineDispatcher = Dispatchers.Unconfined) = AddViewModel(
        SearchProductsUseCase(index, ItemTextParser(index)),
        index,
        AddItemsUseCase(repository),
        FindSameItemUseCase(repository),
        EditItemUseCase(repository, undoStack),
        ParseSpeechUseCase(ItemTextParser(index)),
        voice,
        voicePreferences,
        ObserveListUseCase(repository),
        searchDispatcher,
    )
}

/**
 * Voz falsa: cada [VoiceRecognizer.listen] entrega o que o teste mandar por [say] e termina com
 * [finish] (o silêncio longo) ou ao soltar o botão ([stop]). Com [mayUseInternet], faz de conta que
 * vai pelo serviço padrão (o aviso aparece); senão, pelo reconhecedor do aparelho.
 */
class FakeVoiceRecognizer(override val isAvailable: Boolean = true) : VoiceRecognizer {
    private var events = Channel<VoiceEvent>(Channel.UNLIMITED)
    var listens = 0
        private set
    var mayUseInternet = true

    /** O `stopOnSilence` da última escuta: `true` quando aberta pelo TalkBack. */
    var lastStopOnSilence: Boolean? = null
        private set

    /** Quantas vezes a tela pediu para preparar a voz. */
    var prepares = 0
        private set

    override suspend fun prepare() {
        prepares++
    }

    override suspend fun mayUseInternet() = mayUseInternet

    override fun listen(stopOnSilence: Boolean): Flow<VoiceEvent> {
        listens++
        lastStopOnSilence = stopOnSilence
        events = Channel(Channel.UNLIMITED)
        return events.receiveAsFlow()
    }

    fun say(vararg event: VoiceEvent) = event.forEach { events.trySend(it) }

    fun finish() {
        events.close()
    }

    override fun stop() = finish()
}

class FakeVoicePreferences(var noticeShown: Boolean = false) : VoicePreferences {
    override suspend fun wasNoticeShown() = noticeShown

    override suspend fun setNoticeShown() {
        noticeShown = true
    }
}
