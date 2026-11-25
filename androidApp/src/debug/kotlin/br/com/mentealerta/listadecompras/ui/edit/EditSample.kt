package br.com.mentealerta.listadecompras.ui.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.domain.QuantityUnit
import br.com.mentealerta.listadecompras.ui.theme.ShoppingListTheme

/** As seções do mercado, na ordem, para a escolha da seção (#56). */
private val SampleSections = listOf(
    SectionUi("produce", "Hortifrúti", R.drawable.catalog_fluent_leafy_green),
    SectionUi("bakery", "Padaria", R.drawable.catalog_fluent_bread),
    SectionUi("meat", "Açougue e peixaria", R.drawable.catalog_fluent_cut_of_meat),
    SectionUi("deli_dairy", "Frios e laticínios", R.drawable.catalog_fluent_cheese_wedge),
    SectionUi("frozen", "Congelados", R.drawable.catalog_fluent_ice),
    SectionUi("pantry", "Mercearia", R.drawable.catalog_fluent_canned_food),
    SectionUi("beverages", "Bebidas", R.drawable.catalog_fluent_cup_with_straw),
    SectionUi("cleaning", "Limpeza", R.drawable.catalog_fluent_sponge),
    SectionUi("personal_care", "Higiene e farmácia", R.drawable.catalog_fluent_lotion_bottle),
    SectionUi("pet", "Pet", R.drawable.catalog_fluent_paw_prints),
    SectionUi("other", "Outros", R.drawable.catalog_fluent_shopping_cart),
)

private fun sampleSection(id: String) = SampleSections.first { it.id == id }

/**
 * Edição de exemplo para preview e screenshot (#55): "Leite desnatado, 2 litros", em Frios e
 * laticínios. Fica no source set `debug`.
 */
val SampleEditState = EditUiState.Editing(
    name = "Leite",
    detail = "desnatado",
    quantity = Quantity(2.0, QuantityUnit.LITER),
    canDecrease = true,
    icon = R.drawable.catalog_fluent_glass_of_milk,
    section = sampleSection("deli_dairy"),
    sections = SampleSections,
)

/** Escolhendo a seção do leite (#56): Frios e laticínios marcada com "Está aqui". */
val SampleEditChoosingSectionState = SampleEditState.copy(choosingSection = true)

/** Sem quantidade nem detalhe: "Menos" desativado e o exemplo no campo de detalhe. */
val SampleEditEmptyState = EditUiState.Editing(
    name = "Feijão",
    detail = "",
    quantity = null,
    canDecrease = false,
    icon = R.drawable.catalog_fluent_shopping_cart,
    section = sampleSection("other"),
    sections = SampleSections,
)

@Preview(showBackground = true)
@Composable
private fun EditPreview() {
    ShoppingListTheme { EditContent(SampleEditState) }
}
