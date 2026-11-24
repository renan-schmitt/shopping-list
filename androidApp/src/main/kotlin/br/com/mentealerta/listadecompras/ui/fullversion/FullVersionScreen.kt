package br.com.mentealerta.listadecompras.ui.fullversion

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.premium.Feature
import br.com.mentealerta.listadecompras.ui.components.BackToListButton
import br.com.mentealerta.listadecompras.ui.components.PrimaryButton
import br.com.mentealerta.listadecompras.ui.components.SecondaryButton
import br.com.mentealerta.listadecompras.ui.components.SectionTitle
import br.com.mentealerta.listadecompras.ui.components.UppercaseText
import org.koin.compose.viewmodel.koinViewModel

private val CheckSize = 28.dp

/**
 * "Versão completa" (#88, plano §5): abre por cima da lista, como "O que já comprei". Topo verde com
 * "Voltar para a lista"; embaixo, o título, "Pague uma vez e use para sempre." com o preço da loja, o
 * que vem e que o essencial continua de graça. Os botões ficam no fim, sem pressa: **Comprar a versão
 * completa** e **Já comprei**. Com a versão completa, a tela agradece no lugar dos botões.
 */
@Composable
fun FullVersionScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FullVersionViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BackHandler(onBack = onClose)
    FullVersionContent(
        uiState = uiState,
        modifier = modifier,
        onClose = onClose,
        onBuy = viewModel::onBuy,
        onRestore = viewModel::onRestore,
    )
}

@Composable
fun FullVersionContent(
    uiState: FullVersionUiState,
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onBuy: () -> Unit = {},
    onRestore: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    Scaffold(
        modifier = modifier,
        containerColor = colors.surface,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.primary)
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
            ) {
                BackToListButton(onClick = onClose)
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            UppercaseText(
                text = stringResource(R.string.full_version_title),
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onSurface,
                modifier = Modifier.fillMaxWidth().semantics { heading() },
            )
            if (uiState.isComplete) {
                BodyText(stringResource(R.string.full_version_thanks))
            } else {
                BodyText(stringResource(R.string.full_version_intro))
                uiState.price?.let { BodyText(stringResource(R.string.full_version_price, it)) }
            }
            SectionTitle(text = stringResource(R.string.full_version_includes), modifier = Modifier.padding(top = 8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Feature.entries.mapNotNull { it.label() }.forEach { FeatureRow(stringResource(it)) }
                FeatureRow(stringResource(R.string.full_version_feature_more))
            }
            if (!uiState.isComplete) {
                BodyText(stringResource(R.string.full_version_free_note), modifier = Modifier.padding(top = 8.dp))
                Purchase(uiState, onBuy = onBuy, onRestore = onRestore, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

/** O recado da última tentativa (lido pelo TalkBack quando aparece), **Comprar** e **Já comprei**. */
@Composable
private fun Purchase(
    uiState: FullVersionUiState,
    onBuy: () -> Unit,
    onRestore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        uiState.message?.let { message ->
            Text(
                text = stringResource(message.text()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        PrimaryButton(
            text = stringResource(R.string.full_version_buy),
            onClick = onBuy,
            enabled = !uiState.busy,
            modifier = Modifier.fillMaxWidth(),
        )
        SecondaryButton(
            text = stringResource(R.string.full_version_restore),
            onClick = onRestore,
            enabled = !uiState.busy,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Uma coisa que vem na versão completa: o ✓ ao lado do texto, que quebra a linha se for longo. */
@Composable
private fun FeatureRow(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(R.drawable.ic_check),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(CheckSize),
        )
        BodyText(text, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun BodyText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.fillMaxWidth(),
    )
}

/**
 * Nome de cada [Feature] na tela; `null` fica de fora por enquanto. O `when` sem `else` obriga a
 * decidir o texto de toda funcionalidade nova.
 */
@StringRes
internal fun Feature.label(): Int? = when (this) {
    Feature.NoAds -> R.string.full_version_feature_no_ads
    Feature.FullHistory -> R.string.full_version_feature_full_history
    Feature.Reminders -> R.string.full_version_feature_reminders
    Feature.UsualItems -> R.string.full_version_feature_usual_items
    Feature.HomeScreenShortcut -> R.string.full_version_feature_shortcut
    Feature.SectionOrder -> R.string.full_version_feature_section_order
    Feature.DecorativeThemes -> R.string.full_version_feature_themes
    Feature.Spending -> R.string.full_version_feature_spending
    // Fase 4: só entra na tela quando o aviso existir.
    Feature.NearStoreAlert -> null
}

@StringRes
private fun FullVersionMessage.text(): Int = when (this) {
    FullVersionMessage.Pending -> R.string.full_version_pending
    FullVersionMessage.Unavailable -> R.string.full_version_unavailable
    FullVersionMessage.Failed -> R.string.full_version_failed
    FullVersionMessage.NothingToRestore -> R.string.full_version_nothing_to_restore
}
