package br.com.mentealerta.listadecompras.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics

// Fica no source set `debug` para não entrar na versão publicada.

/**
 * Como um aviso aparece por cima da tela (2026-11-25): [behind] desfocado e escurecido, como o
 * [DialogBackdrop] faz na janela do diálogo, e [content] no meio. O Roborazzi não captura a janela do
 * diálogo, então os screenshots e os checks de acessibilidade usam este desenho. O que está atrás
 * fica fora do TalkBack, como no diálogo de verdade.
 */
@Composable
fun BackdropSample(behind: @Composable () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().blur(BackdropBlur).clearAndSetSemantics {}) { behind() }
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = BACKDROP_DIM)),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}
