package br.com.mentealerta.listadecompras.ui.tema

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/** Tema do app: segue o modo escuro do sistema (paleta escura provisória, #9). */
@Composable
fun ListaDeComprasTheme(
    escuro: Boolean = isSystemInDarkTheme(),
    conteudo: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalTokensQuadrinhos provides TokensQuadrinhos()) {
        MaterialTheme(
            colorScheme = if (escuro) EsquemaEscuro else EsquemaClaro,
            typography = TipografiaQuadrinhos,
            shapes = FormasQuadrinhos,
            content = conteudo,
        )
    }
}

/** Acesso aos tokens do estilo quadrinhos: `TemaQuadrinhos.tokens.contornoGrosso`. */
object TemaQuadrinhos {
    val tokens: TokensQuadrinhos
        @Composable
        @ReadOnlyComposable
        get() = LocalTokensQuadrinhos.current
}
