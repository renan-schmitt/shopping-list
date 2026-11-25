package br.com.mentealerta.listadecompras.ui.edit

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.CatalogSection
import br.com.mentealerta.listadecompras.domain.EditItemUseCase
import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.domain.ObserveListUseCase
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.domain.RememberSectionUseCase
import br.com.mentealerta.listadecompras.domain.RemoveItemUseCase
import br.com.mentealerta.listadecompras.domain.RenameItemUseCase
import br.com.mentealerta.listadecompras.domain.SectionChoices
import br.com.mentealerta.listadecompras.ui.catalog.drawableRes
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
     *
     * [section]: onde o produto vai ficar na lista, com o **Mudar de seção** (#56); `null` esconde o
     * bloco (produto da conferência, que ainda não está na lista). Com [choosingSection], a tela
     * mostra a escolha entre as [sections], na ordem do mercado.
     */
    data class Editing(
        val name: String,
        val detail: String,
        val quantity: Quantity?,
        val canDecrease: Boolean,
        @DrawableRes val icon: Int,
        val section: SectionUi? = null,
        val sections: List<SectionUi> = emptyList(),
        val choosingSection: Boolean = false,
    ) : EditUiState

    /** A edição acabou sozinha: o produto saiu da lista ([removedName]) ou já não estava nela (`null`). */
    data class Closed(val removedName: String?) : EditUiState
}

/** Seção do catálogo na edição (#56): o nome com o ícone ao lado. */
data class SectionUi(val id: String, val name: String, @DrawableRes val icon: Int)

/** Como a pessoa saiu da edição; mudar ou tirar da lista conta para os anúncios (#89). */
sealed interface EditOutcome {
    data object Unchanged : EditOutcome

    data object Changed : EditOutcome

    /** "Tirar da lista": a lista mostra "Feijão saiu da lista." com o [name]. */
    data class Removed(val name: String) : EditOutcome
}

/**
 * Edição do produto (#55, plano §1.1): nome, quantidade com − / + e detalhe. Não existe "Salvar":
 * o rascunho é gravado ao sair da tela ([onClose]) e quando o app vai para segundo plano ([onSave]),
 * pelo [EditItemUseCase] — cada gravação que muda algo vira uma entrada do "Desfazer". Nome em
 * branco mantém o nome anterior.
 *
 * **Mudar de seção** ([onChoosingSectionChange]) abre a escolha das seções; tocar numa ([onSectionSelect])
 * volta para a edição com o produto nela. Ao gravar, a escolha fica lembrada para o produto (#56).
 * Trocar o nome para outro produto leva o item para a seção que a pessoa escolheu antes para esse
 * produto, se houver, a não ser que ela tenha escolhido a seção nesta edição.
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
    private val rememberSection: RememberSectionUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<EditUiState>(EditUiState.Loading)
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    /** Produto aberto e como ele está no banco (atualizado a cada gravação). */
    private var openId: ListItemId? = null
    private var saved: ListItem? = null
    private var loadJob: Job? = null
    private val mutex = Mutex()

    /** Se alguma gravação desta edição mudou o produto (ex.: o app foi para segundo plano no meio). */
    private var edited = false

    /** Seção escolhida pela pessoa nesta edição; `null` enquanto ela não escolhe. */
    private var chosenSectionId: String? = null

    /** Seções escolhidas antes, para quem troca o nome para outro produto. */
    private var sectionChoices = SectionChoices.none(catalogIndex)

    private val sections = catalogIndex.catalog.sections.map { it.toUi() }

    /** Abre o produto [id]. Se ele já está aberto (rotação de tela), o rascunho continua. */
    fun open(id: ListItemId) {
        if (id == openId && _uiState.value !is EditUiState.Closed) return
        openId = id
        saved = null
        edited = false
        chosenSectionId = null
        _uiState.value = EditUiState.Loading
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            sectionChoices = rememberSection.choices()
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

    /**
     * **Mudar de seção** ([choosing] verdadeiro) mostra as seções para escolher; **Voltar para o
     * produto** (ou o voltar do celular) na escolha volta para a edição sem mudar nada.
     */
    fun onChoosingSectionChange(choosing: Boolean) {
        _uiState.update { state -> (state as? EditUiState.Editing)?.copy(choosingSection = choosing) ?: state }
    }

    /** A pessoa tocou numa seção: o produto vai para ela e a edição volta. */
    fun onSectionSelect(sectionId: String) {
        if (catalogIndex.section(sectionId) == null) return
        chosenSectionId = sectionId
        updateDraft { base, editing ->
            editingOf(base, editing.name, editing.detail, editing.quantity).copy(choosingSection = false)
        }
    }

    fun onDecrease() = updateQuantity { it.decreaseQuantity() }

    /** Grava o rascunho, se ele muda algo. */
    fun onSave() {
        val item = draft() ?: return
        viewModelScope.launch {
            mutex.withLock {
                if (editItem(item, sectionChosen = chosenSectionId != null)) {
                    saved = item
                    edited = true
                }
            }
        }
    }

    /**
     * Saindo da tela pelo "Voltar para a lista" ou pelo voltar do celular: grava e fecha. Diz se a
     * pessoa mudou o produto nesta edição.
     */
    fun onClose(): EditOutcome {
        val changed = edited || draft().let { it != null && it != saved }
        onSave()
        close()
        return if (changed) EditOutcome.Changed else EditOutcome.Unchanged
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

    private fun draftOf(base: ListItem, name: String, detail: String, quantity: Quantity?): ListItem {
        val renamed = renameItem(base, name)
        val chosen = chosenSectionId
        val placed = when {
            chosen != null -> renamed.copy(sectionId = chosen)
            sectionChoices.sameProduct(base, renamed) -> renamed
            else -> sectionChoices.applyTo(renamed)
        }
        return placed.copy(quantity = quantity, detail = detail.trim().ifEmpty { null })
    }

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
            section = (catalogIndex.section(item.sectionId) ?: catalogIndex.section(CatalogSection.OTHER_ID))?.toUi(),
            sections = sections,
            choosingSection = (_uiState.value as? EditUiState.Editing)?.choosingSection == true,
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

private fun CatalogSection.toUi() = SectionUi(
    id = id,
    name = name,
    icon = checkNotNull(icon.drawableRes()) { "Seção \"$id\" sem ícone convertido" },
)
