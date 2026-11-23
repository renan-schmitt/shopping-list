package br.com.mentealerta.listadecompras.ui.edit

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.domain.EditItemUseCase
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.domain.ObserveListUseCase
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.domain.RemoveItemUseCase
import br.com.mentealerta.listadecompras.domain.RenameItemUseCase
import br.com.mentealerta.listadecompras.ui.catalog.iconRes
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Estado da tela de edição. */
sealed interface EditUiState {
    /** O produto ainda não chegou do banco. */
    data object Loading : EditUiState

    /**
     * O rascunho na tela: [name] e [detail] como a pessoa digitou; [quantity] já com o − / +; [icon]
     * acompanha o nome (trocar "leite" por "pão" troca o ícone).
     */
    data class Editing(
        val name: String,
        val detail: String,
        val quantity: Quantity?,
        val canDecrease: Boolean,
        @DrawableRes val icon: Int,
    ) : EditUiState

    /** A edição acabou sozinha: o produto saiu da lista ([removedName]) ou já não estava nela (`null`). */
    data class Closed(val removedName: String?) : EditUiState
}

/**
 * Edição do produto (#55, plano §1.1): nome, quantidade com − / + e detalhe. Não existe "Salvar":
 * o rascunho é gravado ao sair da tela ([onClose]) e quando o app vai para segundo plano ([onSave]),
 * pelo [EditItemUseCase] — cada gravação que muda algo vira uma entrada do "Desfazer". Nome em
 * branco mantém o nome anterior.
 *
 * "Tirar da lista" ([onRemove]) tira o produto pelo [RemoveItemUseCase] e fecha a edição; a lista
 * mostra o aviso com o "Desfazer".
 *
 * Um só ViewModel para a tela, como o de adicionar: [open] carrega o produto e, numa rotação de tela,
 * mantém o rascunho. Gravar e tirar passam por um cadeado, para não se cruzarem.
 */
class EditViewModel(
    private val observeList: ObserveListUseCase,
    private val catalogIndex: CatalogIndex,
    private val renameItem: RenameItemUseCase,
    private val editItem: EditItemUseCase,
    private val removeItem: RemoveItemUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<EditUiState>(EditUiState.Loading)
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    /** Produto aberto e como ele está no banco (atualizado a cada gravação). */
    private var openId: ListItemId? = null
    private var saved: ListItem? = null
    private var loadJob: Job? = null
    private val mutex = Mutex()

    /** Abre o produto [id]. Se ele já está aberto (rotação de tela), o rascunho continua. */
    fun open(id: ListItemId) {
        if (id == openId && _uiState.value !is EditUiState.Closed) return
        openId = id
        saved = null
        _uiState.value = EditUiState.Loading
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val item = observeList().first().asSequence().flatMap { it.items }.find { it.id == id }
            saved = item
            _uiState.value = item?.let { editingOf(it, it.name, it.detail.orEmpty()) } ?: EditUiState.Closed(null)
        }
    }

    fun onNameChange(name: String) = updateDraft { base, editing ->
        editingOf(base, name, editing.detail, editing.quantity)
    }

    fun onDetailChange(detail: String) = updateDraft { base, editing ->
        editingOf(base, editing.name, detail, editing.quantity)
    }

    fun onIncrease() = updateQuantity { it.increaseQuantity() }

    fun onDecrease() = updateQuantity { it.decreaseQuantity() }

    /** Grava o rascunho, se ele muda algo. */
    fun onSave() {
        val item = draft() ?: return
        viewModelScope.launch {
            mutex.withLock {
                if (editItem(item)) saved = item
            }
        }
    }

    /** Saindo da tela pelo "Voltar para a lista" ou pelo voltar do celular: grava e fecha. */
    fun onClose() {
        onSave()
        close()
    }

    /** "Tirar da lista": o rascunho não é gravado; o aviso usa o nome que está na tela. */
    fun onRemove() {
        val item = saved ?: return
        val name = draft()?.name ?: item.name
        close()
        viewModelScope.launch {
            mutex.withLock {
                val removed = removeItem(item.id)
                _uiState.value = EditUiState.Closed(removedName = name.takeIf { removed })
            }
        }
    }

    /**
     * A tela já tratou o [EditUiState.Closed]. Sem isso, o ViewModel (um só para a tela) continuava
     * fechado e a edição do próximo produto fechava na hora, repetindo o aviso "Feijão saiu da lista.".
     */
    fun onClosedHandled() {
        if (_uiState.value is EditUiState.Closed) _uiState.value = EditUiState.Loading
    }

    private fun close() {
        loadJob?.cancel()
        openId = null
        _uiState.value = EditUiState.Loading
    }

    /** O produto como ficaria com o rascunho; `null` se nada está aberto. */
    private fun draft(): ListItem? {
        val base = saved ?: return null
        val editing = _uiState.value as? EditUiState.Editing ?: return null
        return draftOf(base, editing.name, editing.detail, editing.quantity)
    }

    private fun draftOf(base: ListItem, name: String, detail: String, quantity: Quantity?): ListItem =
        renameItem(base, name).copy(quantity = quantity, detail = detail.trim().ifEmpty { null })

    private fun editingOf(
        base: ListItem,
        name: String,
        detail: String,
        quantity: Quantity? = base.quantity,
    ): EditUiState.Editing {
        val item = draftOf(base, name, detail, quantity)
        return EditUiState.Editing(
            name = name,
            detail = detail,
            quantity = quantity,
            canDecrease = item.canDecreaseQuantity,
            icon = item.iconRes(catalogIndex),
        )
    }

    private fun updateDraft(change: (ListItem, EditUiState.Editing) -> EditUiState.Editing) {
        val base = saved ?: return
        _uiState.update { state -> (state as? EditUiState.Editing)?.let { change(base, it) } ?: state }
    }

    private fun updateQuantity(change: (ListItem) -> ListItem) = updateDraft { base, editing ->
        val changed = change(base.copy(quantity = editing.quantity))
        editingOf(base, editing.name, editing.detail, changed.quantity)
    }
}
