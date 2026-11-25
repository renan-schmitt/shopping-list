package br.com.mentealerta.listadecompras.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.ui.components.BackToListButton
import br.com.mentealerta.listadecompras.ui.components.ComicCard
import br.com.mentealerta.listadecompras.ui.components.UppercaseText
import br.com.mentealerta.listadecompras.ui.theme.ComicTheme
import org.koin.compose.viewmodel.koinViewModel

/**
 * "Ajustes" (#89; a tela completa vem na #70): abre por cima da lista, como o "O que já comprei". Topo
 * verde com "Voltar para a lista"; embaixo, o título e a chave "Anúncios pelo meu interesse".
 */
@Composable
fun SettingsScreen(onClose: () -> Unit, modifier: Modifier = Modifier, viewModel: SettingsViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BackHandler(onBack = onClose)
    SettingsContent(
        uiState = uiState,
        onPersonalizedAdsChange = viewModel::onPersonalizedAdsChange,
        onClose = onClose,
        modifier = modifier,
    )
}

@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    modifier: Modifier = Modifier,
    onPersonalizedAdsChange: (Boolean) -> Unit = {},
    onClose: () -> Unit = {},
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            UppercaseText(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onSurface,
                modifier = Modifier.fillMaxWidth().semantics { heading() },
            )
            if (uiState is SettingsUiState.Content) {
                PersonalizedAdsSwitch(checked = uiState.personalizedAds, onCheckedChange = onPersonalizedAdsChange)
            }
        }
    }
}

/**
 * A linha toda liga e desliga, com área de toque grande; o TalkBack lê o nome, o texto de apoio e se
 * está ligada. Além da cor, a bolinha muda de lado e mostra um ✓ quando ligada: o estado nunca fica
 * só na cor.
 */
@Composable
private fun PersonalizedAdsSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val colors = MaterialTheme.colorScheme
    ComicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ComicTheme.tokens.minTouchTarget)
                .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.settings_personalized_ads),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                )
                Text(
                    text = stringResource(R.string.settings_personalized_ads_off),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface,
                )
                Text(
                    text = stringResource(R.string.settings_personalized_ads_privacy),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface,
                )
            }
            // A linha já é a chave para o TalkBack: o Switch só desenha.
            Switch(
                checked = checked,
                onCheckedChange = null,
                thumbContent = if (checked) {
                    {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else {
                    null
                },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = colors.primary,
                    checkedThumbColor = colors.onPrimary,
                    checkedIconColor = colors.primary,
                    uncheckedTrackColor = colors.surface,
                    uncheckedThumbColor = colors.outline,
                    uncheckedBorderColor = colors.outline,
                ),
            )
        }
    }
}
