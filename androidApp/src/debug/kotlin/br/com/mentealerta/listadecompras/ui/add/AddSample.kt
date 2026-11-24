package br.com.mentealerta.listadecompras.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.domain.QuantityUnit
import br.com.mentealerta.listadecompras.normalization.ParsedItem
import br.com.mentealerta.listadecompras.ui.list.ListRowUi
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

/** Depois de pôr o leite: campo vazio, o aviso e "Adicionados" com o que entrou (#122). */
val SampleAddAddedState = AddUiState(lastAdded = AddedUi("Leite"))

/** "Adicionados" de exemplo, o mais recente no topo. */
val SampleAdded = listOf(
    ListRowUi(
        ListItemId("milk"),
        "Leite",
        "desnatado",
        Quantity(2.0, QuantityUnit.LITER),
        icon = R.drawable.catalog_fluent_glass_of_milk,
    ),
    ListRowUi(ListItemId("banana"), "Banana", quantity = Quantity(6.0), icon = R.drawable.catalog_fluent_banana),
    ListRowUi(
        ListItemId("crackers"),
        "Biscoito de polvilho salgado da padaria da esquina",
        icon = R.drawable.catalog_fluent_rice_cracker,
    ),
)

/** Depois de falar "leite, feijão e ovos" com o leite já na lista (#127): o que entrou e o que já estava. */
val SampleAddAlreadyOnListState = AddUiState(lastAdded = AddedUi("feijão", count = 2, alreadyOnList = listOf("Leite")))

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

/**
 * Conferência da lista colada (#68): produtos com quantidade, nome longo e desconhecido, e o que já
 * está na lista.
 */
val SampleAddReviewState = AddUiState(
    review = ReviewUi(
        items = listOf(
            option("Leite desnatado", R.drawable.catalog_fluent_glass_of_milk, Quantity(2.0, QuantityUnit.LITER)),
            option("Banana", R.drawable.catalog_fluent_banana, Quantity(6.0)),
            option("Biscoito de polvilho salgado da padaria da esquina", R.drawable.catalog_fluent_rice_cracker),
            option("Zabumbinha", R.drawable.catalog_fluent_shopping_cart),
        ),
        alreadyOnList = listOf("Arroz", "Feijão"),
    ),
)

/** O texto colado não tinha produto (#68), com o botão **Colar a lista** ainda na tela. */
val SampleAddPastedNothingState = AddUiState(pastedNothing = true)

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
private fun AddAddedPreview() {
    ShoppingListTheme { AddContent(SampleAddAddedState, added = SampleAdded, canSpeak = true) }
}

@Preview(showBackground = true)
@Composable
private fun AddReviewPreview() {
    ShoppingListTheme { AddContent(SampleAddReviewState, canSpeak = true) }
}

@Preview(showBackground = true)
@Composable
private fun AddPreview() {
    ShoppingListTheme { AddContent(SampleAddState, canSpeak = true) }
}
