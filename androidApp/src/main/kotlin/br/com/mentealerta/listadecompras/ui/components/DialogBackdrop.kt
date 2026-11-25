package br.com.mentealerta.listadecompras.ui.components

import android.os.Build
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider

/** Quanto o fundo escurece atrás de um aviso ou pergunta (2026-11-25). */
const val BACKDROP_DIM = 0.5f

/** Quanto o fundo desfoca atrás de um aviso ou pergunta, onde o celular permite (Android 12+). */
val BackdropBlur = 16.dp

/**
 * Fundo dos avisos e perguntas que abrem por cima da tela (2026-11-25): chamado dentro de um
 * `Dialog`, escurece o que está atrás ([BACKDROP_DIM]) e, no Android 12+, também desfoca
 * ([BackdropBlur]), para a caixa da frente não se misturar com a tela de baixo. O Android só desfoca
 * quando o aparelho aguenta (com economia de bateria, por exemplo, fica só o escurecido). O próprio
 * diálogo trava o toque em tudo que está atrás.
 */
@Composable
fun DialogBackdrop() {
    val window = (LocalView.current.parent as? DialogWindowProvider)?.window ?: return
    val blurRadius = with(LocalDensity.current) { BackdropBlur.roundToPx() }
    SideEffect {
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.setDimAmount(BACKDROP_DIM)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window.attributes = window.attributes.apply { blurBehindRadius = blurRadius }
        }
    }
}
