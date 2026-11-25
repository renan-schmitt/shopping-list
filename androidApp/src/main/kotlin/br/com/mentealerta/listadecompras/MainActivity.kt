package br.com.mentealerta.listadecompras

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import br.com.mentealerta.listadecompras.ui.list.ListScreen
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme

/**
 * A única tela do app. Também recebe a lista mandada pelo "Compartilhar" do Android (#135): com o app
 * fechado, no [onCreate]; já aberto (uma instância só, `singleTask`), no [onNewIntent]. Numa rotação
 * de tela o intent não é lido de novo.
 */
class MainActivity : ComponentActivity() {
    private var receivedText by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) receivedText = intent.receivedText()
        setContent {
            ShoppingListTheme {
                ListScreen(
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
