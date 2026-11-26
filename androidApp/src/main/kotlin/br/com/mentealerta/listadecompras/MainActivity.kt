package br.com.mentealerta.listadecompras

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import br.com.mentealerta.listadecompras.ui.list.ListScreen
import br.com.mentealerta.listadecompras.ui.list.ListUiState
import br.com.mentealerta.listadecompras.ui.list.ListViewModel
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A única tela do app. Também recebe a lista mandada pelo "Compartilhar" do Android (#135): com o app
 * fechado, no [onCreate]; já aberto (uma instância só, `singleTask`), no [onNewIntent]. Numa rotação
 * de tela o intent não é lido de novo.
 *
 * A tela de abertura (#74) fica até a lista carregar, para não aparecer o fundo vazio antes dela. O
 * [listViewModel] é passado à [ListScreen], para a abertura e a lista olharem o mesmo estado.
 */
class MainActivity : ComponentActivity() {
    private var receivedText by mutableStateOf<String?>(null)
    private val listViewModel: ListViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { listViewModel.uiState.value == ListUiState.Loading }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) receivedText = intent.receivedText()
        setContent {
            ShoppingListTheme {
                ListScreen(
                    viewModel = listViewModel,
                    receivedText = receivedText,
                    onReceivedTextConsume = { receivedText = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.receivedText()?.let { receivedText = it }
    }
}
