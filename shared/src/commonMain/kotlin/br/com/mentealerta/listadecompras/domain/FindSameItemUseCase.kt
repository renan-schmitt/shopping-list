package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.normalization.ParsedItem
import br.com.mentealerta.listadecompras.normalization.searchKey
import kotlinx.coroutines.flow.first

/**
 * Produto repetido não duplica (#54, plano §1.1): acha na lista o item que já é o produto que a
 * pessoa quer pôr, para perguntar "Leite já está na lista. Quer mais um?".
 *
 * É o mesmo item quando:
 * - é o mesmo produto do catálogo, mesmo com outro nome ("aipim" e "mandioca"); produto
 *   desconhecido compara o nome normalizado ("Pão de queijo" = "paes de queijo");
 * - o detalhe é igual, normalizado ("leite desnatado" ≠ "leite da marca X" ≠ "leite");
 * - o item da lista não está riscado: riscado já está no carrinho, então o novo entra como outra
 *   linha, para não esconder que ainda falta comprar.
 */
class FindSameItemUseCase(private val repository: ShoppingListRepository) {
    /** O item da lista que é o mesmo que [parsed], ou `null` se ele ainda não está na lista. */
    suspend operator fun invoke(parsed: ParsedItem): ListItem? {
        val productId = parsed.match?.product?.id
        val name = searchKey(parsed.name)
        val detail = detailKey(parsed.detail)
        return repository.observeSections().first()
            .asSequence()
            .flatMap { it.items }
            .firstOrNull { item ->
                val sameProduct = if (productId != null) {
                    item.productId == productId
                } else {
                    item.productId == null && searchKey(item.name) == name
                }
                sameProduct && !item.checked && detailKey(item.detail) == detail
            }
    }

    private fun detailKey(detail: String?): String? = detail?.let(::searchKey)?.takeIf { it.isNotEmpty() }
}
