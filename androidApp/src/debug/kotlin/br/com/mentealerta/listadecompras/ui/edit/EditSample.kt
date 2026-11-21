package br.com.mentealerta.listadecompras.ui.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.domain.QuantityUnit
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme

/**
 * Edição de exemplo para preview e screenshot (#55): "Leite desnatado, 2 litros". Fica no source
 * set `debug`.
 */
val SampleEditState = EditUiState.Editing(
    name = "Leite",
    detail = "desnatado",
    quantity = Quantity(2.0, QuantityUnit.LITER),
    canDecrease = true,
    icon = R.drawable.catalog_fluent_glass_of_milk,
)

/** Sem quantidade nem detalhe: "Menos" desativado e o exemplo no campo de detalhe. */
val SampleEditEmptyState = EditUiState.Editing(
    name = "Feijão",
    detail = "",
    quantity = null,
    canDecrease = false,
    icon = R.drawable.catalog_fluent_shopping_cart,
)

@Preview(showBackground = true)
@Composable
private fun EditPreview() {
    ShoppingListTheme { EditContent(SampleEditState) }
}
