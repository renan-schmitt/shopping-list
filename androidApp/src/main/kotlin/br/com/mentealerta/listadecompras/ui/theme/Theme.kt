package br.com.mentealerta.listadecompras.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/** Tema do app: segue o modo escuro do sistema (paleta "papel invertido", plano §4.8). */
@Composable
fun ShoppingListTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalComicTokens provides ComicTokens(),
        LocalComicColors provides if (darkTheme) DarkComicColors else LightComicColors,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = ComicTypography,
            shapes = ComicShapes,
            content = content,
        )
    }
}

/** Acesso aos tokens do estilo quadrinhos: `ComicTheme.tokens.thickOutline`. */
object ComicTheme {
    val tokens: ComicTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalComicTokens.current

    /** Cores do estilo quadrinhos fora do Material 3: `ComicTheme.colors.shadow`. */
    val colors: ComicExtraColors
        @Composable
        @ReadOnlyComposable
        get() = LocalComicColors.current
}
