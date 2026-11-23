package br.com.mentealerta.listadecompras.ui.history

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.domain.QuantityUnit
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme

/**
 * Histórico de exemplo (#66): compras de hoje, de ontem e de 12 dias atrás, com quantidade, nome
 * longo e produto desconhecido (ícone de "Outros"). Fica no source set `debug` para não entrar na
 * versão publicada.
 */
val SampleHistoryState = HistoryUiState.Content(
    listOf(
        PurchaseUi(
            id = "today",
            daysAgo = 0,
            rows = listOf(
                PurchasedRowUi("Banana", Quantity(6.0), R.drawable.catalog_fluent_banana),
                PurchasedRowUi("Pão francês", Quantity(10.0), R.drawable.catalog_fluent_baguette_bread),
            ),
        ),
        PurchaseUi(
            id = "yesterday",
            daysAgo = 1,
            rows = listOf(PurchasedRowUi("Café", null, R.drawable.catalog_own_coffee_package)),
        ),
        PurchaseUi(
            id = "older",
            daysAgo = 12,
            rows = listOf(
                PurchasedRowUi("Arroz", Quantity(5.0, QuantityUnit.KILOGRAM), R.drawable.catalog_own_rice_package),
                PurchasedRowUi(
                    "Biscoito de polvilho salgado da padaria da esquina",
                    Quantity(3.0, QuantityUnit.PACKAGE),
                    R.drawable.catalog_fluent_rice_cracker,
                ),
                PurchasedRowUi("Flocão da vó", null, R.drawable.catalog_fluent_shopping_cart),
            ),
        ),
    ),
)

@Preview(name = "O que já comprei — claro", widthDp = 411, heightDp = 1400)
@Composable
private fun HistoryLightPreview() {
    ShoppingListTheme(darkTheme = false) { HistoryContent(SampleHistoryState) }
}

@Preview(name = "O que já comprei — escuro", widthDp = 411, heightDp = 1400)
@Composable
private fun HistoryDarkPreview() {
    ShoppingListTheme(darkTheme = true) { HistoryContent(SampleHistoryState) }
}

@Preview(name = "O que já comprei — vazio", widthDp = 411, heightDp = 800)
@Composable
private fun HistoryEmptyPreview() {
    ShoppingListTheme(darkTheme = false) { HistoryContent(HistoryUiState.Empty) }
}
