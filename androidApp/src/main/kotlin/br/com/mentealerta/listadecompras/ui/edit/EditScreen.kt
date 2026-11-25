package br.com.mentealerta.listadecompras.ui.edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.ui.components.BackToListButton
import br.com.mentealerta.listadecompras.ui.components.LabeledTextField
import br.com.mentealerta.listadecompras.ui.components.PrimaryButton
import br.com.mentealerta.listadecompras.ui.components.SecondaryButton
import br.com.mentealerta.listadecompras.ui.components.UppercaseText
import br.com.mentealerta.listadecompras.ui.components.comicStyle
import br.com.mentealerta.listadecompras.ui.list.format
import br.com.mentealerta.listadecompras.ui.theme.ComicTheme
import org.koin.compose.viewmodel.koinViewModel

private val ProductIconSize = 64.dp

/**
 * Edição do produto [id] (#55, plano §1.1): ocupa a tela toda, por cima da lista, como a de
 * adicionar. [onClose] volta para a lista e diz como a edição acabou: mudou, não mudou ou o produto
 * saiu da lista (com o nome, para o aviso "Feijão saiu da lista."). O rascunho é gravado ao sair e
 * quando o app vai para segundo plano.
 */
@Composable
fun EditScreen(
    id: ListItemId,
    onClose: (EditOutcome) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnClose by rememberUpdatedState(onClose)
    LaunchedEffect(id) { viewModel.open(id) }
    LaunchedEffect(uiState) {
        (uiState as? EditUiState.Closed)?.let {
            // Consome o fechamento: o próximo produto aberto não pode herdar o aviso deste.
            viewModel.onClosedHandled()
            currentOnClose(it.removedName?.let(EditOutcome::Removed) ?: EditOutcome.Unchanged)
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onSave() }
    val close = { onClose(viewModel.onClose()) }
    BackHandler(onBack = close)
    EditContent(
        uiState = uiState,
        onNameChange = viewModel::onNameChange,
        onDetailChange = viewModel::onDetailChange,
        onIncrease = viewModel::onIncrease,
        onDecrease = viewModel::onDecrease,
        onRemove = viewModel::onRemove,
        onClose = close,
        modifier = modifier,
    )
}

/**
 * Topo verde com "Voltar para a lista"; embaixo, no papel: nome com o ícone do produto, quantidade
 * com **Menos** / **Mais** em verde, detalhe e, por último, **Tirar da lista** em destaque, com o
 * cesto de lixo do "Jogar a lista fora" (#124). Não existe "Salvar". O
 * teclado só abre quando a pessoa toca num campo, para não cobrir os botões.
 */
@Composable
fun EditContent(
    uiState: EditUiState,
    modifier: Modifier = Modifier,
    onNameChange: (String) -> Unit = {},
    onDetailChange: (String) -> Unit = {},
    onIncrease: () -> Unit = {},
    onDecrease: () -> Unit = {},
    onRemove: () -> Unit = {},
    onClose: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = modifier.fillMaxSize().background(colors.surface)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.primary)
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
        ) {
            BackToListButton(onClick = onClose)
        }
        if (uiState is EditUiState.Editing) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Bottom) {
                    LabeledTextField(
                        label = stringResource(R.string.edit_name_label),
                        value = uiState.name,
                        onValueChange = onNameChange,
                        modifier = Modifier.weight(1f),
                    )
                    Image(
                        painter = painterResource(uiState.icon),
                        contentDescription = null,
                        modifier = Modifier.size(ProductIconSize),
                    )
                }
                QuantityStepper(
                    quantity = uiState.quantity,
                    canDecrease = uiState.canDecrease,
                    onIncrease = onIncrease,
                    onDecrease = onDecrease,
                )
                LabeledTextField(
                    label = stringResource(R.string.edit_detail_label),
                    value = uiState.detail,
                    onValueChange = onDetailChange,
                    placeholder = stringResource(R.string.edit_detail_hint),
                )
                SecondaryButton(
                    text = stringResource(R.string.edit_remove),
                    onClick = onRemove,
                    icon = painterResource(R.drawable.catalog_fluent_wastebasket),
                    tintIcon = false,
                    prominent = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/**
 * "Quantidade": a quantidade numa linha ("2 litros"; sem quantidade, 1) e, embaixo, **Menos** e
 * **Mais** lado a lado, verdes como o botão principal (#124) e da mesma altura — com três colunas,
 * a fonte em 200% cortaria as palavras. A quantidade nova é lida pelo TalkBack a cada toque, como
 * se fala ("meio quilo").
 */
@Composable
private fun QuantityStepper(quantity: Quantity?, canDecrease: Boolean, onIncrease: () -> Unit, onDecrease: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val tokens = ComicTheme.tokens
    val text = (quantity ?: Quantity(1.0)).format(LocalContext.current.resources)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        UppercaseText(
            text = stringResource(R.string.edit_quantity_label),
            style = MaterialTheme.typography.labelLarge,
            color = colors.onSurface,
            textAlign = TextAlign.Start,
            modifier = Modifier.semantics { heading() },
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .comicStyle(
                    shape = MaterialTheme.shapes.medium,
                    background = colors.surface,
                    outline = colors.outline,
                    outlineWidth = tokens.thinOutline,
                    shadow = 0.dp,
                )
                .heightIn(min = tokens.minTouchTarget)
                .padding(8.dp)
                .clearAndSetSemantics {
                    this.text = AnnotatedString(text.spoken)
                    liveRegion = LiveRegionMode.Polite
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text.visible,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurface,
                textAlign = TextAlign.Center,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        ) {
            PrimaryButton(
                text = stringResource(R.string.edit_less),
                onClick = onDecrease,
                icon = painterResource(R.drawable.ic_remove),
                enabled = canDecrease,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            PrimaryButton(
                text = stringResource(R.string.edit_more),
                onClick = onIncrease,
                icon = painterResource(R.drawable.ic_add),
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
    }
}
