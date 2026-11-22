package br.com.mentealerta.listadecompras.ui.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.domain.QuantityUnit
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme

/**
 * Lista de exemplo para preview e screenshot (#50): nomes curtos e longos, detalhe, quantidades e
 * produtos riscados. Fica no source set `debug` para não entrar na versão publicada.
 */
val SampleListState = ListUiState.Content(
    listOf(
        SectionUi(
            id = "produce",
            name = "Hortifrúti",
            rows = listOf(
                row("banana", "Banana", R.drawable.catalog_fluent_banana, Quantity(6.0), checked = true),
                row("tomato", "Tomate", R.drawable.catalog_fluent_tomato),
                row("lettuce", "Alface", R.drawable.catalog_fluent_leafy_green),
            ),
        ),
        SectionUi(
            id = "bakery",
            name = "Padaria",
            rows = listOf(
                row("bread", "Pão francês", R.drawable.catalog_fluent_baguette_bread, Quantity(10.0), checked = true),
            ),
        ),
        SectionUi(
            id = "butcher",
            name = "Açougue",
            rows = listOf(
                row(
                    "meat",
                    "Carne moída",
                    R.drawable.catalog_fluent_cut_of_meat,
                    Quantity(0.5, QuantityUnit.KILOGRAM),
                    detail = "de patinho",
                ),
            ),
        ),
        SectionUi(
            id = "grocery",
            name = "Mercearia",
            rows = listOf(
                row("rice", "Arroz", R.drawable.catalog_own_rice_package, Quantity(5.0, QuantityUnit.KILOGRAM)),
                row("coffee", "Café", R.drawable.catalog_own_coffee_package),
                row(
                    "milk",
                    "Leite",
                    R.drawable.catalog_fluent_glass_of_milk,
                    Quantity(2.0, QuantityUnit.LITER),
                    detail = "desnatado",
                ),
                row(
                    "crackers",
                    "Biscoito de polvilho salgado da padaria da esquina",
                    R.drawable.catalog_fluent_rice_cracker,
                    Quantity(3.0, QuantityUnit.PACKAGE),
                ),
            ),
        ),
    ),
)

/** A mesma lista antes de riscar o primeiro produto: rodapé só com o "Adicionar" grande (#52). */
val SampleListStateNotShopping = ListUiState.Content(
    SampleListState.sections.map { section -> section.copy(rows = section.rows.map { it.copy(checked = false) }) },
)

private fun row(
    id: String,
    name: String,
    icon: Int,
    quantity: Quantity? = null,
    detail: String? = null,
    checked: Boolean = false,
) = ListRowUi(ListItemId(id), name, detail, quantity, checked, icon)

@Preview(name = "Lista — claro", widthDp = 411, heightDp = 1400)
@Composable
private fun ListLightPreview() {
    ShoppingListTheme(darkTheme = false) { ListContent(SampleListState) }
}

@Preview(name = "Lista — escuro", widthDp = 411, heightDp = 1400)
@Composable
private fun ListDarkPreview() {
    ShoppingListTheme(darkTheme = true) { ListContent(SampleListState) }
}

@Preview(name = "Lista — fonte 200%", widthDp = 411, heightDp = 2200, fontScale = 2f)
@Composable
private fun ListFont200Preview() {
    ShoppingListTheme(darkTheme = false) { ListContent(SampleListState) }
}

@Preview(name = "Lista — antes de riscar", widthDp = 411, heightDp = 1400)
@Composable
private fun ListNotShoppingPreview() {
    ShoppingListTheme(darkTheme = false) { ListContent(SampleListStateNotShopping) }
}

@Preview(name = "Lista vazia", widthDp = 411, heightDp = 800)
@Composable
private fun EmptyListPreview() {
    ShoppingListTheme(darkTheme = false) { ListContent(ListUiState.Empty) }
}
