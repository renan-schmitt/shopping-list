package br.com.mentealerta.listadecompras.ui.add

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.ui.components.BackToListButton
import br.com.mentealerta.listadecompras.ui.components.ComicCard
import br.com.mentealerta.listadecompras.ui.components.HoldButton
import br.com.mentealerta.listadecompras.ui.components.LabeledTextField
import br.com.mentealerta.listadecompras.ui.components.PrimaryButton
import br.com.mentealerta.listadecompras.ui.components.SecondaryButton
import br.com.mentealerta.listadecompras.ui.components.UppercaseText
import br.com.mentealerta.listadecompras.ui.components.comicStyle
import br.com.mentealerta.listadecompras.ui.list.ItemName
import br.com.mentealerta.listadecompras.ui.list.ListRowUi
import br.com.mentealerta.listadecompras.ui.list.format
import br.com.mentealerta.listadecompras.ui.theme.ComicTheme
import org.koin.compose.viewmodel.koinViewModel
import br.com.mentealerta.listadecompras.ui.list.QuantityBadge as ListQuantityBadge

private val PlusSize = 36.dp
private val OptionIconSize = 44.dp
private val AddedDividerWidth = 2.dp

/**
 * Tela de adicionar digitando (#53, plano §1.2) ou falando (#60): ocupa a tela toda, por cima da
 * lista. [onClose] volta para a lista, pelo botão "Voltar para a lista" ou pelo voltar do celular.
 *
 * O pedido de microfone do Android sai daqui, quando o [AddViewModel] pede ([VoiceUi.AskingPermission]).
 * Se o app sai da tela durante a escuta, o microfone fecha.
 */
@Composable
fun AddScreen(onClose: () -> Unit, modifier: Modifier = Modifier, viewModel: AddViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val added by viewModel.added.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val close = {
        viewModel.onClose()
        onClose()
    }
    BackHandler(onBack = close)
    val askMicrophone = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        viewModel::onMicrophonePermission,
    )
    LaunchedEffect(uiState.voice) {
        if (uiState.voice == VoiceUi.AskingPermission) askMicrophone.launch(Manifest.permission.RECORD_AUDIO)
    }
    LaunchedEffect(Unit) { viewModel.onOpen() }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onSpeakRelease() }
    AddContent(
        uiState = uiState,
        added = added,
        canSpeak = viewModel.canSpeak,
        onQueryChange = viewModel::onQueryChange,
        onAdd = viewModel::onAdd,
        onSubmit = viewModel::onSubmit,
        onAddMore = viewModel::onAddMore,
        onKeepAsIs = viewModel::onKeepAsIs,
        onSpeakPress = { viewModel.onSpeakPress(context.hasMicrophone()) },
        onSpeakRelease = viewModel::onSpeakRelease,
        onSpeakToggle = { viewModel.onSpeakPress(context.hasMicrophone(), byToggle = true) },
        onAcceptNotice = { viewModel.onNoticeAnswer(accepted = true, context.hasMicrophone()) },
        onDeclineNotice = { viewModel.onNoticeAnswer(accepted = false, context.hasMicrophone()) },
        onClose = close,
        modifier = modifier,
    )
}

private fun Context.hasMicrophone(): Boolean =
    checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

/**
 * Topo verde com "Voltar para a lista", o campo "Digite ou fale o que quer comprar." e, se o celular
 * tem voz ([canSpeak]), o botão **Falar**. Com busca, as opções aparecem embaixo, no mesmo verde do
 * topo, cada uma numa caixa de papel. Sem busca, no papel: o aviso do que entrou ("Adicionado:
 * Leite.") e a lista **Adicionados** ([added], #122), para a pessoa ver que o produto entrou. O campo
 * abre com o teclado. Com o produto repetido (#54), a pergunta toma o lugar
 * do aviso e das opções até a pessoa responder; o mesmo vale para a explicação do microfone e a
 * escuta (#60), que escondem o teclado. Depois de um recado que sugere escrever, o teclado volta.
 */
@Composable
fun AddContent(
    uiState: AddUiState,
    modifier: Modifier = Modifier,
    added: List<ListRowUi> = emptyList(),
    canSpeak: Boolean = false,
    onQueryChange: (String) -> Unit = {},
    onAdd: (AddOptionUi) -> Unit = {},
    onSubmit: () -> Unit = {},
    onAddMore: () -> Unit = {},
    onKeepAsIs: () -> Unit = {},
    onSpeakPress: () -> Unit = {},
    onSpeakRelease: (short: Boolean) -> Unit = {},
    onSpeakToggle: () -> Unit = {},
    onAcceptNotice: () -> Unit = {},
    onDeclineNotice: () -> Unit = {},
    onClose: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val voice = uiState.voice
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    LaunchedEffect(voice) {
        when (voice) {
            null, VoiceUi.Message.NOT_UNDERSTOOD -> Unit
            is VoiceUi.Message -> focusRequester.requestFocus()
            else -> focusManager.clearFocus()
        }
    }
    Column(modifier = modifier.fillMaxSize().background(colors.surface)) {
        AddTopBar(
            query = uiState.query,
            speak = speakButton(canSpeak, voice),
            focusRequester = focusRequester,
            onQueryChange = onQueryChange,
            onSubmit = onSubmit,
            onSpeakPress = onSpeakPress,
            onSpeakRelease = onSpeakRelease,
            onSpeakToggle = onSpeakToggle,
            onClose = onClose,
        )
        val repeated = uiState.repeated
        val searching = uiState.showsOptions
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(if (searching) colors.primary else colors.surface)
                .imePadding(),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            when {
                voice == VoiceUi.Notice -> item(key = "voice-notice", contentType = "voice-notice") {
                    VoiceNotice(onAccept = onAcceptNotice, onDecline = onDeclineNotice)
                }
                voice is VoiceUi.Listening -> item(key = "listening", contentType = "listening") {
                    Listening(listening = voice)
                }
                repeated != null -> item(key = "repeated", contentType = "repeated") {
                    RepeatedQuestion(name = repeated.name, onAddMore = onAddMore, onKeepAsIs = onKeepAsIs)
                }
                searching -> options(uiState, onAdd)
                else -> {
                    if (voice is VoiceUi.Message) {
                        item(key = "voice-message", contentType = "voice-message") { VoiceMessage(voice) }
                    }
                    item(key = "added", contentType = "added") { AddedNotice(uiState.lastAdded) }
                    addedList(added)
                }
            }
            item(key = "bottom", contentType = "bottom") { Box(Modifier.navigationBarsPadding()) }
        }
    }
}

/** Botão de voz no topo: parado ("Segure para falar.") ou com o microfone aberto ("Terminei"). */
private enum class SpeakButton { IDLE, LISTENING }

/** Sem voz no celular, ou com a explicação na tela, o botão some. */
private fun speakButton(canSpeak: Boolean, voice: VoiceUi?): SpeakButton? = when {
    !canSpeak || voice == VoiceUi.Notice -> null
    voice is VoiceUi.Listening -> SpeakButton.LISTENING
    else -> SpeakButton.IDLE
}

/**
 * Topo verde: "Voltar para a lista", o campo e, se o celular tem voz ([speak]), o botão de segurar
 * para falar (#123). Ele fica no mesmo lugar durante a escuta, para o dedo não perder o botão.
 */
@Composable
private fun AddTopBar(
    query: String,
    speak: SpeakButton?,
    focusRequester: FocusRequester,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onSpeakPress: () -> Unit,
    onSpeakRelease: (short: Boolean) -> Unit,
    onSpeakToggle: () -> Unit,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BackToListButton(onClick = onClose)
        SearchField(query = query, onQueryChange = onQueryChange, onSubmit = onSubmit, focusRequester = focusRequester)
        if (speak != null) {
            Text(
                text = stringResource(R.string.add_speak_hint),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.clearAndSetSemantics {},
            )
            HoldButton(
                text = stringResource(
                    if (speak ==
                        SpeakButton.LISTENING
                    ) {
                        R.string.add_voice_done
                    } else {
                        R.string.add_speak
                    },
                ),
                onPress = onSpeakPress,
                onRelease = onSpeakRelease,
                onToggle = onSpeakToggle,
                icon = painterResource(R.drawable.ic_mic),
                held = speak == SpeakButton.LISTENING,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * "Adicionados" (#122): o que entrou desde que a tela abriu, o mais recente no topo, sem seções. Só
 * para ver: mudar fica na lista.
 */
private fun LazyListScope.addedList(added: List<ListRowUi>) {
    if (added.isEmpty()) return
    item(key = "added-title", contentType = "title") {
        OptionsTitle(stringResource(R.string.add_added_title), color = MaterialTheme.colorScheme.onSurface)
    }
    items(added, key = { "added-${it.id.value}" }, contentType = { "added-row" }) { AddedRow(it) }
}

/** Os produtos achados e, depois, a opção de pôr o texto como está, no verde do topo. */
private fun LazyListScope.options(uiState: AddUiState, onAdd: (AddOptionUi) -> Unit) {
    if (uiState.found.isNotEmpty()) {
        item(key = "found-title", contentType = "title") {
            OptionsTitle(stringResource(R.string.add_found), color = MaterialTheme.colorScheme.onPrimary)
        }
        items(uiState.found, contentType = { "option" }) {
            OptionButton(option = it, unknown = false, onClick = { onAdd(it) })
        }
    }
    uiState.unknown?.let { unknown ->
        if (uiState.found.isEmpty()) {
            item(key = "nothing-found", contentType = "nothing-found") { NothingFound() }
        }
        item(key = "unknown", contentType = "option") {
            OptionButton(option = unknown, unknown = true, onClick = { onAdd(unknown) })
        }
    }
}

/**
 * Campo "Digite ou fale o que quer comprar.", que abre com o teclado; o rótulo é frase, em caixa
 * normal. Tecla "pronto" põe a primeira opção na lista.
 */
@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    focusRequester: FocusRequester,
) {
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    LabeledTextField(
        label = stringResource(R.string.add_search_label),
        value = query,
        onValueChange = onQueryChange,
        labelColor = MaterialTheme.colorScheme.onPrimary,
        uppercaseLabel = false,
        onImeAction = onSubmit,
        focusRequester = focusRequester,
    )
}

/**
 * "Adicionado: Leite." (ou "Adicionado de novo: Leite.", depois do "Sim, mais um"), lido pelo
 * TalkBack assim que aparece. Some quando a pessoa volta a digitar.
 */
@Composable
private fun AddedNotice(added: AddedUi?) {
    val text = when {
        added == null || added.count == 0 -> null
        added.count > 1 -> pluralStringResource(R.plurals.add_added_many, added.count, added.count)
        added.again -> stringResource(R.string.add_added_again, added.name.capitalized())
        else -> stringResource(R.string.add_added, added.name.capitalized())
    }
    val already = added?.alreadyOnList.orEmpty()
    val alreadyText = when {
        already.isEmpty() -> null
        already.size == 1 -> stringResource(R.string.add_already_on_list, already.single().capitalized())
        else -> pluralStringResource(R.plurals.add_already_on_list_many, already.size, already.size)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (added == null) 0.dp else 8.dp)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        listOfNotNull(text, alreadyText).forEach {
            Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

/**
 * "Leite já está na lista. Quer mais um?" com **Sim, mais um** e **Não** lado a lado, da mesma altura
 * (#54, protótipo navegável). A pergunta é lida pelo TalkBack assim que aparece; com fonte grande, o
 * texto dos botões quebra a linha.
 */
@Composable
private fun RepeatedQuestion(name: String, onAddMore: () -> Unit, onKeepAsIs: () -> Unit) {
    val tokens = ComicTheme.tokens
    ComicCard(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = tokens.largeShadow)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = stringResource(R.string.add_already_there, name.capitalized()),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(IntrinsicSize.Min)) {
                PrimaryButton(
                    text = stringResource(R.string.add_more_one),
                    onClick = onAddMore,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                // Mesma letra do "Sim, mais um" (#122), no papel para não parecer a ação principal.
                SecondaryButton(
                    text = stringResource(R.string.add_keep_as_is),
                    onClick = onKeepAsIs,
                    prominent = true,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
        }
    }
}

/**
 * Explicação antes do pedido de microfone, só na primeira vez e só no serviço padrão (#60, #123,
 * plano §3.3): **Usar o microfone** e **Escrever**, um embaixo do outro, em largura total.
 */
@Composable
private fun VoiceNotice(onAccept: () -> Unit, onDecline: () -> Unit) {
    val tokens = ComicTheme.tokens
    ComicCard(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = tokens.largeShadow)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = stringResource(R.string.add_voice_notice),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
            )
            PrimaryButton(
                text = stringResource(R.string.add_voice_notice_yes),
                onClick = onAccept,
                icon = painterResource(R.drawable.ic_mic),
                modifier = Modifier.fillMaxWidth(),
            )
            SecondaryButton(
                text = stringResource(R.string.add_voice_notice_no),
                onClick = onDecline,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * Microfone aberto: "Estou ouvindo. Pode falar.", cada trecho entendido numa linha e, por último, o
 * texto que está aparecendo. Soltar o botão (ou tocar em **Terminei** pelo TalkBack) fecha o
 * microfone (#123). Só o "Estou ouvindo" é lido pelo TalkBack: ler o que aparece faria o microfone
 * ouvir o próprio celular.
 */
@Composable
private fun Listening(listening: VoiceUi.Listening) {
    val tokens = ComicTheme.tokens
    val colors = MaterialTheme.colorScheme
    ComicCard(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = tokens.largeShadow)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.add_voice_listening),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.primary,
                modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
            )
            listening.heard.forEach {
                Text(text = it, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
            }
            if (listening.partial.isNotEmpty()) {
                Text(
                    text = listening.partial,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

/** Recado depois da escuta, lido pelo TalkBack assim que aparece. */
@Composable
private fun VoiceMessage(message: VoiceUi.Message) {
    val text = when (message) {
        VoiceUi.Message.NOT_UNDERSTOOD -> R.string.add_voice_not_understood
        VoiceUi.Message.NO_INTERNET -> R.string.add_voice_no_internet
        VoiceUi.Message.NO_MICROPHONE -> R.string.add_voice_no_microphone
        VoiceUi.Message.UNAVAILABLE -> R.string.add_voice_unavailable
        VoiceUi.Message.HOLD_TO_SPEAK -> R.string.add_voice_hold
        VoiceUi.Message.DOWNLOADING_VOICE -> R.string.add_voice_downloading
    }
    Text(
        text = stringResource(text),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
    )
}

@Composable
private fun OptionsTitle(text: String, color: Color) {
    UppercaseText(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        modifier = Modifier.padding(top = 8.dp).semantics { heading() },
    )
}

@Composable
private fun NothingFound() {
    Text(
        text = stringResource(R.string.add_nothing_found),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).semantics { heading() },
    )
}

/**
 * Opção da busca: "+" → nome (com detalhe e quantidade) → ícone do produto, num cartão de no mínimo
 * 72dp. Nome longo quebra a linha. O produto desconhecido aparece como frase, em caixa normal:
 * "Pôr “bergamota” na lista". Para o TalkBack é um botão só, lido em caixa normal.
 */
@Composable
private fun OptionButton(option: AddOptionUi, unknown: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val tokens = ComicTheme.tokens
    val named = option.detail?.let { stringResource(R.string.list_item_name_with_detail, option.name, it) }
        ?: option.name
    val name = if (unknown) stringResource(R.string.add_unknown, named) else named
    val quantity = option.quantity?.format(LocalContext.current.resources)
    val label = quantity?.let { stringResource(R.string.list_item_with_quantity, name, it.spoken) } ?: name
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                text = AnnotatedString(label)
                role = Role.Button
                onClick {
                    onClick()
                    true
                }
            }
            .comicStyle(
                shape = MaterialTheme.shapes.medium,
                background = colors.surface,
                outline = colors.outline,
                outlineWidth = tokens.thinOutline,
                shadow = tokens.smallShadow,
            )
            .heightIn(min = tokens.minRowHeight)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_add),
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(PlusSize),
        )
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (unknown) name else name.toUpperCase(LocaleList.current),
                style = if (unknown) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                color = colors.onSurface,
            )
            if (quantity != null) QuantityBadge(quantity.visible)
        }
        Image(
            painter = painterResource(option.icon),
            contentDescription = null,
            modifier = Modifier.size(OptionIconSize),
        )
    }
}

@Composable
private fun QuantityBadge(text: String) {
    val color = MaterialTheme.colorScheme.onSurface
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = Modifier
            .border(2.dp, color, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

/**
 * Linha de "Adicionados": nome (com detalhe e quantidade) → ícone, com uma linha discreta embaixo.
 * Para o TalkBack é um texto só, em caixa normal.
 */
@Composable
private fun AddedRow(row: ListRowUi) {
    val dividerColor = MaterialTheme.colorScheme.outlineVariant
    val name = row.detail?.let { stringResource(R.string.list_item_name_with_detail, row.name, it) } ?: row.name
    val quantity = row.quantity?.format(LocalContext.current.resources)
    val label = quantity?.let { stringResource(R.string.list_item_with_quantity, name, it.spoken) } ?: name
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ComicTheme.tokens.minRowHeight)
            .drawBehind {
                val y = size.height - AddedDividerWidth.toPx() / 2
                drawLine(dividerColor, Offset(0f, y), Offset(size.width, y), AddedDividerWidth.toPx())
            }
            .clearAndSetSemantics { text = AnnotatedString(label) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            ItemName(text = name, checked = row.checked)
            if (quantity != null) ListQuantityBadge(text = quantity.visible, checked = row.checked)
        }
        Image(painter = painterResource(row.icon), contentDescription = null, modifier = Modifier.size(OptionIconSize))
    }
}

/** "xyzzy" → "Xyzzy", para o aviso começar com maiúscula como frase. */
private fun String.capitalized(): String = replaceFirstChar { it.titlecase() }
