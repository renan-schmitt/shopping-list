package br.com.mentealerta.listadecompras.ui.add

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.CatalogSection
import br.com.mentealerta.listadecompras.domain.AddItemsUseCase
import br.com.mentealerta.listadecompras.domain.EditItemUseCase
import br.com.mentealerta.listadecompras.domain.FindSameItemUseCase
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.domain.ObserveListUseCase
import br.com.mentealerta.listadecompras.domain.ParsePastedListUseCase
import br.com.mentealerta.listadecompras.domain.ParseSpeechUseCase
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.domain.RemoveItemUseCase
import br.com.mentealerta.listadecompras.domain.SearchProductsUseCase
import br.com.mentealerta.listadecompras.domain.UndoLastActionUseCase
import br.com.mentealerta.listadecompras.normalization.ParsedItem
import br.com.mentealerta.listadecompras.ui.catalog.drawableRes
import br.com.mentealerta.listadecompras.ui.list.ListRowUi
import br.com.mentealerta.listadecompras.ui.list.toRow
import br.com.mentealerta.listadecompras.voice.VoiceEvent
import br.com.mentealerta.listadecompras.voice.VoicePreferences
import br.com.mentealerta.listadecompras.voice.VoiceRecognizer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
    /** O [unknown] aparece antes do [found]: só houve batidas aproximadas (2026-11-23). */
    val unknownFirst: Boolean = false,
    /** Último produto que entrou na lista, para o aviso "Adicionado: Leite.". */
    val lastAdded: AddedUi? = null,
    /** Produto que já estava na lista, para a pergunta "Leite já está na lista. Quer mais um?" (#54). */
    val repeated: RepeatedUi? = null,
    /** Falar os produtos (#60): explicação, escuta ou recado; `null` quando a voz não está em uso. */
    val voice: VoiceUi? = null,
    /** Conferência da lista colada (#68), antes de pôr na lista; `null` fora dela. */
    val review: ReviewUi? = null,
    /**
     * O texto colado ou recebido não tinha produto: de onde ele veio, para o recado ("Não achei
     * produtos no texto copiado." ou "Não achei produtos na mensagem."); `null` fora dele.
     */
    val pastedNothing: PasteSource? = null,
) {
    /**
     * As opções da busca estão na tela (#122): há o que mostrar e nenhuma pergunta, explicação ou
     * escuta na frente. Enquanto isso, "Adicionados" fica escondido.
     */
    val showsOptions: Boolean
        get() = (found.isNotEmpty() || unknown != null) && repeated == null && review == null &&
            voice != VoiceUi.Notice && voice !is VoiceUi.Listening
}

/** De onde veio a lista: copiada e colada (#68) ou recebida pelo "Compartilhar" do Android (#135). */
enum class PasteSource { COPIED, SHARED }

/**
 * Conferência (#68): os produtos entendidos na lista colada que ainda não estão na lista ([items]),
 * na ordem do texto, e os nomes dos que já estão nela ([alreadyOnList]), que não entram de novo.
 */
data class ReviewUi(val items: List<AddOptionUi>, val alreadyOnList: List<String> = emptyList())

/**
 * O que entrou na lista: [again] quando a pessoa respondeu "Sim, mais um". Pela voz podem entrar
 * vários de uma vez: com [count] maior que 1, o aviso diz quantos ("Adicionados: 3 produtos.").
 */
data class AddedUi(
    val name: String,
    val again: Boolean = false,
    val count: Int = 1,
    /**
     * Falados que já estavam na lista e não entraram de novo (#127), com o nome que têm na lista.
     * Com [count] 0, nada entrou: o aviso só diz o que já estava.
     */
    val alreadyOnList: List<String> = emptyList(),
)

/** Etapas de falar os produtos (#60, plano §3.3). */
sealed interface VoiceUi {
    /** Explicação do microfone e do envio do som ao Google, na primeira vez, só no serviço padrão. */
    data object Notice : VoiceUi

    /** Esperando a pessoa responder ao pedido de microfone do Android. */
    data object AskingPermission : VoiceUi

    /** Ouvindo: os trechos já entendidos ([heard]) e o que está aparecendo agora ([partial]). */
    data class Listening(val heard: List<String> = emptyList(), val partial: String = "") : VoiceUi

    /**
     * Recados depois da escuta. [HOLD_TO_SPEAK]: o botão foi só tocado, sem segurar; [DOWNLOADING_VOICE]:
     * a voz pt-BR ainda está sendo baixada (#123).
     */
    enum class Message : VoiceUi {
        NOT_UNDERSTOOD,
        NO_INTERNET,
        NO_MICROPHONE,
        UNAVAILABLE,
        HOLD_TO_SPEAK,
        DOWNLOADING_VOICE,
    }
}

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
 * na quantidade do item que já está, pelo [EditItemUseCase] (o "Desfazer" desfaz); "Não" deixa
 * como está. Voltar a digitar também conta como "Não".
 *
 * Falar (#60, #123): segurar o botão abre o microfone e soltar fecha. Quem usa o TalkBack toca para
 * abrir e toca para fechar. Na primeira vez pelo serviço padrão, vem antes a explicação do microfone
 * ([VoicePreferences]); depois, o pedido de microfone do Android, se faltar. O [ParseSpeechUseCase]
 * separa os produtos, que entram direto na lista, menos os que já estão nela (#127). (A conferência
 * antes de pôr na lista é a #61.)
 *
 * Colar a lista (#68) fica no [paste]. Voltar a digitar ou falar deixa a conferência de lado.
 *
 * O que entrou desde que a tela abriu aparece em [added] ("Adicionados", #122), o mais recente no
 * topo, como está na lista agora ([ObserveListUseCase]): com a quantidade nova depois do "Sim, mais
 * um" e sem o que saiu da lista.
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
    removeItem: RemoveItemUseCase,
    undoLastAction: UndoLastActionUseCase,
    private val parseSpeech: ParseSpeechUseCase,
    parsePastedList: ParsePastedListUseCase,
    private val voiceRecognizer: VoiceRecognizer,
    private val voicePreferences: VoicePreferences,
    observeList: ObserveListUseCase,
    private val searchDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddUiState())
    val uiState: StateFlow<AddUiState> = _uiState.asStateFlow()

    /** Ids do que entrou desde que a tela abriu, o mais recente primeiro. */
    private val addedIds = MutableStateFlow<List<ListItemId>>(emptyList())

    /** "Adicionados" (#122): o que entrou desde que a tela abriu, como está na lista agora. */
    val added: StateFlow<List<ListRowUi>> = combine(observeList(), addedIds) { sections, ids ->
        val items = sections.flatMap { it.items }.associateBy { it.id }
        ids.mapNotNull { items[it]?.toRow(catalogIndex) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    /** Colar a lista e a conferência (#68). */
    val paste = PasteReview(
        scope = viewModelScope,
        state = _uiState,
        parsePastedList = parsePastedList,
        findSameItem = findSameItem,
        dispatcher = searchDispatcher,
        toOption = { it.toOption() },
        put = { items ->
            addMutex.withLock { addOneByOne(items) }
        },
    )

    /** Lixeira de "Adicionados" e o aviso dela (2026-11-23). */
    val removal = AddedRemoval(viewModelScope, removeItem, undoLastAction)

    /** Sem serviço de voz no celular, o botão "Falar" não aparece. */
    val canSpeak: Boolean = voiceRecognizer.isAvailable

    private var searchJob: Job? = null
    private var voiceJob: Job? = null

    /** O dedo está no botão "Falar". */
    private var holding = false

    /** A escuta foi aberta com um toque (TalkBack): fecha com outro toque ou no silêncio longo. */
    private var toggled = false
    private val addMutex = Mutex()

    /** O campo mudou: o texto entra na hora (o campo não pode esperar a busca) e a busca vem depois. */
    fun onQueryChange(text: String) {
        // Voltar a digitar fecha o aviso do produto tirado.
        removal.dismiss()
        _uiState.update {
            // Escrever tira o recado da voz; a escuta continua
            val voice = it.voice.takeIf { voice -> voice is VoiceUi.Listening }
            it.copy(
                query = text,
                lastAdded = null,
                repeated = null,
                voice = voice,
                review = null,
                pastedNothing = null,
            )
        }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val options = withContext(searchDispatcher) { searchProducts(text) }
            _uiState.update { state ->
                if (state.query != text) {
                    state
                } else {
                    state.copy(
                        found = options.found.map { it.toOption() },
                        unknown = options.unknown?.toOption(),
                        unknownFirst = options.unknownFirst,
                    )
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
                    remember(addItems(listOf(option.item)).map { it.id })
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
                if (same == null) {
                    remember(addItems(listOf(item)).map { it.id })
                } else {
                    editItem(same.addMore(item.quantity))
                    remember(listOf(same.id))
                }
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
        val first = if (state.unknownFirst) state.unknown else state.found.firstOrNull() ?: state.unknown
        first?.let(::onAdd)
    }

    /**
     * Apertou o botão "Falar": abre o microfone, se já pode (ver [startSpeaking]). Pelo TalkBack
     * ([byToggle]) é um toque: abre, e outro toque com o microfone aberto fecha.
     */
    fun onSpeakPress(hasMicrophone: Boolean, byToggle: Boolean = false) {
        if (_uiState.value.voice is VoiceUi.Listening) {
            if (byToggle) voiceRecognizer.stop()
            return
        }
        holding = !byToggle
        startSpeaking(hasMicrophone, byToggle)
    }

    /**
     * Soltou o botão "Falar" (ou o app saiu da tela): fecha o microfone e o último trecho ainda
     * entra. Um toque rápido ([short]) não ouve nada: lembra de segurar.
     */
    fun onSpeakRelease(short: Boolean = false) {
        val wasHolding = holding
        holding = false
        if (_uiState.value.voice !is VoiceUi.Listening) return
        if (short && wasHolding) {
            voiceJob?.cancel()
            _uiState.update { it.copy(voice = VoiceUi.Message.HOLD_TO_SPEAK) }
        } else {
            voiceRecognizer.stop()
        }
    }

    /**
     * A tela abriu: escolhe o reconhecedor e, se faltar a voz, o pedido de download do Android aparece
     * agora, e não no meio do toque em "Falar" (#123).
     */
    fun onOpen() {
        if (canSpeak) viewModelScope.launch { voiceRecognizer.prepare() }
    }

    /**
     * Resposta à explicação. "Usar o microfone" ([accepted]): ela não aparece mais e a pessoa aperta
     * "Falar" de novo. "Escrever": na próxima vez ela aparece de novo.
     */
    fun onNoticeAnswer(accepted: Boolean, hasMicrophone: Boolean) {
        if (!accepted) {
            _uiState.update { it.copy(voice = null) }
            return
        }
        viewModelScope.launch {
            voicePreferences.setNoticeShown()
            _uiState.update { it.copy(voice = if (hasMicrophone) null else VoiceUi.AskingPermission) }
        }
    }

    /** Resposta ao pedido de microfone do Android. Com permissão, a pessoa aperta "Falar" de novo. */
    fun onMicrophonePermission(granted: Boolean) {
        _uiState.update { it.copy(voice = if (granted) null else VoiceUi.Message.NO_MICROPHONE) }
    }

    /** Saindo da tela: na próxima vez ela abre com o campo vazio. */
    fun onClose() {
        removal.dismiss()
        searchJob?.cancel()
        voiceJob?.cancel()
        _uiState.value = AddUiState()
        addedIds.value = emptyList()
    }

    /** Põe os [ids] no topo de "Adicionados"; quem já estava sobe. */
    private fun remember(ids: List<ListItemId>) {
        addedIds.update { current -> ids.reversed() + current.filterNot { it in ids } }
    }

    /**
     * Antes de ouvir: a explicação, se a escuta pode ir ao Google e ela ainda não apareceu; o pedido
     * de microfone, se falta. Se a pessoa já soltou o botão nesse meio tempo, lembra de segurar.
     */
    private fun startSpeaking(hasMicrophone: Boolean, byToggle: Boolean) {
        viewModelScope.launch {
            val needsNotice = voiceRecognizer.mayUseInternet() && !voicePreferences.wasNoticeShown()
            val voice = when {
                needsNotice -> VoiceUi.Notice
                !hasMicrophone -> VoiceUi.AskingPermission
                !byToggle && !holding -> VoiceUi.Message.HOLD_TO_SPEAK
                else -> null
            }
            if (voice == null) {
                listen(byToggle)
            } else {
                holding = false
                _uiState.update { it.copy(voice = voice, lastAdded = null, repeated = null, review = null) }
            }
        }
    }

    private fun listen(byToggle: Boolean) {
        voiceJob?.cancel()
        toggled = byToggle
        _uiState.update {
            it.copy(
                voice = VoiceUi.Listening(),
                lastAdded = null,
                repeated = null,
                review = null,
                pastedNothing = null,
            )
        }
        voiceJob = viewModelScope.launch {
            val segments = mutableListOf<List<String>>()
            var problem: VoiceUi.Message? = null
            var switched = false
            voiceRecognizer.listen(stopOnSilence = byToggle).collect { event ->
                when (event) {
                    is VoiceEvent.Partial -> updateListening { it.copy(partial = event.text) }
                    is VoiceEvent.Heard -> {
                        segments += event.alternatives
                        updateListening { it.copy(heard = it.heard + event.alternatives.first(), partial = "") }
                    }
                    VoiceEvent.NoInternet -> problem = VoiceUi.Message.NO_INTERNET
                    VoiceEvent.Unavailable -> problem = VoiceUi.Message.UNAVAILABLE
                    VoiceEvent.VoiceMissing -> problem = VoiceUi.Message.DOWNLOADING_VOICE
                    VoiceEvent.SwitchedToService -> switched = true
                }
            }
            if (switched) {
                switchedToService()
                return@launch
            }
            val items = parseSpeech(segments)
            if (items.isEmpty()) {
                _uiState.update { it.copy(voice = problem ?: VoiceUi.Message.NOT_UNDERSTOOD) }
            } else {
                val added = addMutex.withLock { addOneByOne(items) }
                _uiState.update { it.copy(voice = problem, lastAdded = added) }
            }
        }
    }

    /**
     * Põe na lista o que foi falado ou colado, um por vez, menos o que já está nela (#127): o mesmo
     * do "Não" na pergunta de produto repetido. Um por vez, para o repetido na mesma fala também
     * ficar de fora.
     */
    private suspend fun addOneByOne(items: List<ParsedItem>): AddedUi {
        val entered = mutableListOf<ParsedItem>()
        val alreadyOnList = mutableListOf<String>()
        items.forEach { item ->
            val same = findSameItem(item)
            if (same == null) {
                remember(addItems(listOf(item)).map { it.id })
                entered += item
            } else if (same.name !in alreadyOnList) {
                alreadyOnList += same.name
            }
        }
        return AddedUi(
            name = entered.firstOrNull()?.name.orEmpty(),
            count = entered.size,
            alreadyOnList = alreadyOnList,
        )
    }

    /**
     * O aparelho não tem a voz pt-BR (Android 12): a escuta passa ao serviço padrão. Se a explicação
     * já apareceu e a pessoa continua segurando (ou abriu pelo TalkBack), ouve de novo na hora.
     */
    private suspend fun switchedToService() {
        when {
            !voicePreferences.wasNoticeShown() -> {
                holding = false
                _uiState.update { it.copy(voice = VoiceUi.Notice) }
            }
            holding || toggled -> listen(toggled)
            else -> _uiState.update { it.copy(voice = null) }
        }
    }

    private fun updateListening(change: (VoiceUi.Listening) -> VoiceUi.Listening) {
        _uiState.update { state ->
            val listening = state.voice as? VoiceUi.Listening ?: return@update state
            state.copy(voice = change(listening))
        }
    }

    private companion object {
        /** Mantém a coleta numa rotação de tela, sem reabrir a consulta. */
        const val STOP_TIMEOUT_MILLIS = 5_000L
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

/**
 * Lixeira de "Adicionados" (2026-11-23): [remove] tira o produto da lista e guarda o nome em
 * [removed], para o aviso "Leite saiu da lista." com **Desfazer** e **Certo**. O produto continua
 * lembrado em "Adicionados", para voltar ao mesmo lugar com o **Desfazer**.
 */
class AddedRemoval(
    private val scope: CoroutineScope,
    private val removeItem: RemoveItemUseCase,
    private val undoLastAction: UndoLastActionUseCase,
) {
    private val mutableRemoved = MutableStateFlow<String?>(null)
    val removed: StateFlow<String?> = mutableRemoved.asStateFlow()

    fun remove(row: ListRowUi) {
        scope.launch {
            if (removeItem(row.id)) mutableRemoved.value = row.name
        }
    }

    /** O aviso saiu da tela; com [undo] (**Desfazer**), o produto volta. */
    fun onNoticeClosed(undo: Boolean) {
        mutableRemoved.value = null
        if (undo) scope.launch { undoLastAction() }
    }

    /** Fecha o aviso sem desfazer (a pessoa voltou a digitar ou saiu da tela). */
    fun dismiss() {
        mutableRemoved.value = null
    }
}

/**
 * Colar a lista (#68) e receber pelo "Compartilhar" (#135): o texto vira produtos pelo
 * [ParsePastedListUseCase] e aparece para conferir ([AddUiState.review]). A pessoa tira o que não
 * quer e toca em **Pôr na lista**; o que já está na lista não entra de novo, como na voz. [put] põe
 * na lista, um por vez, e diz o que entrou.
 */
class PasteReview(
    private val scope: CoroutineScope,
    private val state: MutableStateFlow<AddUiState>,
    private val parsePastedList: ParsePastedListUseCase,
    private val findSameItem: FindSameItemUseCase,
    private val dispatcher: CoroutineDispatcher,
    private val toOption: (ParsedItem) -> AddOptionUi,
    private val put: suspend (List<ParsedItem>) -> AddedUi,
) {
    /**
     * Tocou em **Colar a lista**: separa os produtos do [text] copiado e abre a conferência com os
     * que ainda não estão na lista. Se todos já estão, só avisa; sem produto, "Não achei".
     */
    fun onPaste(text: String) = review(text, PasteSource.COPIED)

    /**
     * Chegou um texto pelo "Compartilhar" do Android (#135): o mesmo do [onPaste], e uma conferência
     * aberta dá lugar à nova. O texto nunca vai para log.
     */
    fun onShared(text: String) = review(text, PasteSource.SHARED)

    private fun review(text: String, source: PasteSource) {
        scope.launch {
            val items = withContext(dispatcher) { parsePastedList(text) }
            val fresh = mutableListOf<ParsedItem>()
            val alreadyOnList = mutableListOf<String>()
            items.forEach { item ->
                val same = findSameItem(item)
                if (same == null) {
                    fresh += item
                } else if (same.name !in alreadyOnList) {
                    alreadyOnList += same.name
                }
            }
            state.value = when {
                items.isEmpty() -> AddUiState(pastedNothing = source)
                fresh.isEmpty() -> AddUiState(lastAdded = AddedUi(name = "", count = 0, alreadyOnList = alreadyOnList))
                else -> AddUiState(review = ReviewUi(fresh.map(toOption), alreadyOnList))
            }
        }
    }

    /** Lixeira na conferência: o produto não entra. Sem nenhum, a conferência fecha. */
    fun onRemove(option: AddOptionUi) {
        state.update { current ->
            val review = current.review ?: return@update current
            val items = review.items - option
            current.copy(review = if (items.isEmpty()) null else review.copy(items = items))
        }
    }

    /** **Pôr na lista**: os produtos conferidos entram, um por vez, e o aviso diz quantos. */
    fun onConfirm() {
        val review = state.value.review ?: return
        state.value = AddUiState()
        scope.launch {
            val added = put(review.items.map { it.item })
            val alreadyOnList = (review.alreadyOnList + added.alreadyOnList).distinct()
            state.update { it.copy(lastAdded = added.copy(alreadyOnList = alreadyOnList)) }
        }
    }

    /** **Deixa pra lá** (ou o voltar do celular): nada entra. */
    fun onDismiss() {
        state.update { it.copy(review = null) }
    }
}
