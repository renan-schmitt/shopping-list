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
    lastAdded = AddedUi("Banana"),
)

/** Nada achado: só a opção de pôr o texto como está. */
val SampleAddNothingFoundState = AddUiState(
    query = "bergamota",
    unknown = option("bergamota", R.drawable.catalog_fluent_shopping_cart),
)

/** Produto repetido (#54): a pergunta "Leite já está na lista. Quer mais um?". */
val SampleAddRepeatedState = AddUiState(
    query = "leite",
    repeated = RepeatedUi("leite", option("Leite", R.drawable.catalog_fluent_glass_of_milk)),
)

/** Explicação do microfone, na primeira vez que a pessoa toca em "Falar" (#60). */
val SampleAddVoiceNoticeState = AddUiState(voice = VoiceUi.Notice)

/** Ouvindo (#60): dois trechos entendidos e o texto que está aparecendo. */
val SampleAddListeningState = AddUiState(
    voice = VoiceUi.Listening(
        heard = listOf("arroz feijão e dois litros de leite", "biscoito água e sal"),
        partial = "meio quilo de carne",
    ),
)

/** Recado depois da escuta, com o aviso do que entrou antes (#60). */
val SampleAddVoiceMessageState = AddUiState(
    voice = VoiceUi.Message.NO_INTERNET,
    lastAdded = AddedUi("arroz", count = 3),
)

private fun option(name: String, icon: Int, quantity: Quantity? = null) = AddOptionUi(
    item = ParsedItem(name, match = null, quantity, detail = null),
    name = name,
    quantity = quantity,
    icon = icon,
)

@Preview(showBackground = true)
@Composable
private fun AddListeningPreview() {
    ShoppingListTheme { AddContent(SampleAddListeningState, canSpeak = true) }
}

@Preview(showBackground = true)
@Composable
private fun AddPreview() {
    ShoppingListTheme { AddContent(SampleAddState, canSpeak = true) }
}
