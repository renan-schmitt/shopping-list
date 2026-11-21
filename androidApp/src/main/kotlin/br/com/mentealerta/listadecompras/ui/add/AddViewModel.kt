package br.com.mentealerta.listadecompras.ui.add

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.CatalogSection
import br.com.mentealerta.listadecompras.domain.AddItemsUseCase
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
import kotlinx.coroutines.withContext

/** Estado da tela de adicionar. */
data class AddUiState(
    /** O que está no campo, como a pessoa digitou. */
    val query: String = "",
    /** Produtos do catálogo achados para o [query]. */
    val found: List<AddOptionUi> = emptyList(),
    /** O texto como está, para o produto que o app não conhece (#49). */
    val unknown: AddOptionUi? = null,
    /** Nome do último produto que entrou na lista, para o aviso "Leite entrou na lista.". */
    val lastAdded: String? = null,
)

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
 * A busca roda fora da thread principal ([searchDispatcher]) e só a da última letra vale.
 */
class AddViewModel(
    private val searchProducts: SearchProductsUseCase,
    private val catalogIndex: CatalogIndex,
    private val addItems: AddItemsUseCase,
    private val searchDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddUiState())
    val uiState: StateFlow<AddUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    /** O campo mudou: o texto entra na hora (o campo não pode esperar a busca) e a busca vem depois. */
    fun onQueryChange(text: String) {
        _uiState.update { it.copy(query = text, lastAdded = null) }
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

    /** Põe a opção tocada na lista. */
    fun onAdd(option: AddOptionUi) {
        searchJob?.cancel()
        _uiState.value = AddUiState(lastAdded = option.name)
        viewModelScope.launch { addItems(listOf(option.item)) }
    }

    /** Tecla "pronto" do teclado: põe na lista a primeira opção, se houver. */
    fun onSubmit() {
        val state = _uiState.value
        if (state.query.isBlank()) return
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
