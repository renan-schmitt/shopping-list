package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.catalog.CatalogSection
import br.com.mentealerta.listadecompras.normalization.ParsedItem

/**
 * Põe na lista os produtos entendidos no texto ou na fala ([ParsedItem]), na ordem em que vieram.
 * Produto desconhecido entra normalmente, em "Outros" (ver [toListItem]). O nome usado ensina o
 * termo preferido do produto ([LearnPreferredTermUseCase]).
 */
class AddItemsUseCase(
    private val repository: ShoppingListRepository,
    private val learnPreferredTerm: LearnPreferredTermUseCase,
) {
    /** Devolve os itens como entraram na lista. */
    suspend operator fun invoke(parsedItems: List<ParsedItem>): List<ListItem> =
        parsedItems.map { it.toListItem() }.onEach {
            repository.add(it)
            learnPreferredTerm(it)
        }
}

/**
 * Item novo da lista, com o nome que a pessoa usou. Produto do catálogo leva o `id` e a seção dele;
 * produto desconhecido fica sem produto e vai para "Outros" ([CatalogSection.OTHER_ID]), até a
 * pessoa mudar a seção (plano §1.3).
 */
fun ParsedItem.toListItem(): ListItem = ListItem.create(
    name = name,
    productId = match?.product?.id,
    sectionId = match?.product?.sectionId ?: CatalogSection.OTHER_ID,
    quantity = quantity,
    detail = detail,
)
