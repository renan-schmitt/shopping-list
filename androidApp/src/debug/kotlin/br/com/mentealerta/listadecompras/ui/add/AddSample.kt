package br.com.mentealerta.listadecompras.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.domain.QuantityUnit
import br.com.mentealerta.listadecompras.normalization.ParsedItem
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme

/**
 * Tela de adicionar de exemplo para preview e screenshot (#53): "2 litros de lei" com produtos
 * achados (nome longo, quantidade) e o aviso do último que entrou. Fica no source set `debug`.
 */
val SampleAddState = AddUiState(
    query = "2 litros de lei",
    found = listOf(
        option("Leite", R.drawable.catalog_fluent_glass_of_milk, Quantity(2.0, QuantityUnit.LITER)),
        option("Leite desnatado", R.drawable.catalog_fluent_glass_of_milk, Quantity(2.0, QuantityUnit.LITER)),
        option(
            "Leite condensado de caixinha da marca preferida",
            R.drawable.catalog_fluent_glass_of_milk,
            Quantity(2.0, QuantityUnit.LITER),
        ),
    ),
    unknown = option("lei", R.drawable.catalog_fluent_shopping_cart, Quantity(2.0, QuantityUnit.LITER)),
    lastAdded = "Banana",
)

/** Nada achado: só a opção de pôr o texto como está. */
val SampleAddNothingFoundState = AddUiState(
    query = "bergamota",
    unknown = option("bergamota", R.drawable.catalog_fluent_shopping_cart),
)

private fun option(name: String, icon: Int, quantity: Quantity? = null) = AddOptionUi(
    item = ParsedItem(name, match = null, quantity, detail = null),
    name = name,
    quantity = quantity,
    icon = icon,
)

@Preview(showBackground = true)
@Composable
private fun AddPreview() {
    ShoppingListTheme { AddContent(SampleAddState) }
}
