package br.com.mentealerta.listadecompras.ui.add

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.CatalogSection
import br.com.mentealerta.listadecompras.domain.AddItemsUseCase
import br.com.mentealerta.listadecompras.domain.EditItemUseCase
import br.com.mentealerta.listadecompras.domain.FindSameItemUseCase
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.domain.SearchProductsUseCase
import br.com.mentealerta.listadecompras.normalization.ParsedItem
import br.com.mentealerta.listadecompras.ui.catalog.drawableRes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Estado da tela de adicionar. */
data class AddUiState(
    /** O que está no campo, como a pessoa digitou. */
    val query: String = "",
    /** Produtos do catálogo achados para o [query]. */
    val found: List<AddOptionUi> = emptyList(),
    /** O texto como está, para o produto que o app não conhece (#49). */
    val unknown: AddOptionUi? = null,
    /** Último produto que entrou na lista, para o aviso "Leite entrou na lista.". */
    val lastAdded: AddedUi? = null,
    /** Produto que já estava na lista, para a pergunta "Leite já está na lista. Quer mais um?" (#54). */
    val repeated: RepeatedUi? = null,
)

/** O que entrou na lista: [again] quando a pessoa respondeu "Sim, mais um". */
data class AddedUi(val name: String, val again: Boolean = false)

/** A pergunta do produto repetido: [name] é o nome do item que já está na lista; [option], a tocada. */
data class RepeatedUi(val name: String, val option: AddOptionUi)

/** Opção da busca: um toque põe o [item] na lista. */
data class AddOptionUi(
    val item: ParsedItem,
    val name: String,
    val detail: String? = null,
    val quantity: Quantity? = null,
    @DrawableRes val icon: Int,
)

/**
 * Tela de adicionar digitando (#53): a cada letra, busca no catálogo ([SearchProductsUseCase]) e
 * mostra as opções. Um toque põe o produto na lista, limpa o campo e avisa o que entrou; a tela
 * continua aberta para o próximo produto.
 *
 * Se o produto já está na lista ([FindSameItemUseCase]), pergunta antes (#54): "Sim, mais um" soma
 * na quantidade do item que já está, pelo [EditItemUseCase] (o "Ops, voltar" desfaz); "Não" deixa
 * como está. Voltar a digitar também conta como "Não".
 *
 * A busca roda fora da thread principal ([searchDispatcher]) e só a da última letra vale. Pôr na
 * lista passa por um cadeado, para dois toques seguidos não criarem duas linhas.
 */
class AddViewModel(
    private val searchProducts: SearchProductsUseCase,
    private val catalogIndex: CatalogIndex,
    private val addItems: AddItemsUseCase,
    private val findSameItem: FindSameItemUseCase,
    private val editItem: EditItemUseCase,
    private val searchDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddUiState())
    val uiState: StateFlow<AddUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private val addMutex = Mutex()

    /** O campo mudou: o texto entra na hora (o campo não pode esperar a busca) e a busca vem depois. */
    fun onQueryChange(text: String) {
        _uiState.update { it.copy(query = text, lastAdded = null, repeated = null) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val options = withContext(searchDispatcher) { searchProducts(text) }
            _uiState.update { state ->
                if (state.query != text) {
                    state
                } else {
                    state.copy(found = options.found.map { it.toOption() }, unknown = options.unknown?.toOption())
                }
            }
        }
    }

    /** Põe a opção tocada na lista, ou pergunta se o produto já está nela. */
    fun onAdd(option: AddOptionUi) {
        searchJob?.cancel()
        val query = _uiState.value.query
        viewModelScope.launch {
            addMutex.withLock {
                val same = findSameItem(option.item)
                if (same == null) {
                    addItems(listOf(option.item))
                    _uiState.update { state ->
                        // Se a pessoa já voltou a digitar, o campo fica como está.
                        if (state.query == query) {
                            AddUiState(lastAdded = AddedUi(option.name))
                        } else {
                            state.copy(lastAdded = AddedUi(option.name))
                        }
                    }
                } else {
                    _uiState.update { state ->
                        if (state.query == query) state.copy(repeated = RepeatedUi(same.name, option)) else state
                    }
                }
            }
        }
    }

    /** "Sim, mais um": soma na quantidade do item que já está na lista. */
    fun onAddMore() {
        val repeated = _uiState.value.repeated ?: return
        _uiState.value = AddUiState(lastAdded = AddedUi(repeated.name, again = true))
        val item = repeated.option.item
        viewModelScope.launch {
            addMutex.withLock {
                // Lê de novo: o item pode ter mudado enquanto a pergunta estava na tela.
                val same = findSameItem(item)
                if (same == null) addItems(listOf(item)) else editItem(same.addMore(item.quantity))
            }
        }
    }

    /** "Não": deixa a lista como está e limpa o campo. */
    fun onKeepAsIs() {
        if (_uiState.value.repeated != null) _uiState.value = AddUiState()
    }

    /**
     * Tecla "pronto" do teclado: põe na lista a primeira opção, se houver. Com a pergunta do produto
     * repetido na tela, espera a resposta.
     */
    fun onSubmit() {
        val state = _uiState.value
        if (state.query.isBlank() || state.repeated != null) return
        (state.found.firstOrNull() ?: state.unknown)?.let(::onAdd)
    }

    /** Saindo da tela: na próxima vez ela abre com o campo vazio. */
    fun onClose() {
        searchJob?.cancel()
        _uiState.value = AddUiState()
    }

    private fun ParsedItem.toOption(): AddOptionUi {
        val product = match?.product
        val icon = catalogIndex.icon(product?.id, product?.sectionId ?: CatalogSection.OTHER_ID)
        return AddOptionUi(
            item = this,
            name = name,
            detail = detail,
            quantity = quantity,
            icon = checkNotNull(icon.drawableRes()) { "Ícone $icon sem recurso convertido" },
        )
    }
}
