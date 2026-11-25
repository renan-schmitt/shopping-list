package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.catalog.CatalogSection
import br.com.mentealerta.listadecompras.normalization.ParsedItem

/**
 * Põe na lista os produtos entendidos no texto ou na fala ([ParsedItem]), na ordem em que vieram.
 * Produto desconhecido entra normalmente, em "Outros" (ver [toListItem]). Produto para o qual a
 * pessoa já escolheu a seção entra nela ([RememberSectionUseCase], #56). O nome usado ensina o
 * termo preferido do produto ([LearnPreferredTermUseCase]).
 */
class AddItemsUseCase(
    private val repository: ShoppingListRepository,
    private val learnPreferredTerm: LearnPreferredTermUseCase,
    private val rememberSection: RememberSectionUseCase,
) {
    /** Devolve os itens como entraram na lista. */
    suspend operator fun invoke(parsedItems: List<ParsedItem>): List<ListItem> {
        if (parsedItems.isEmpty()) return emptyList()
        val sectionChoices = rememberSection.choices()
        return parsedItems.map { sectionChoices.applyTo(it.toListItem()) }.onEach {
            repository.add(it)
            learnPreferredTerm(it)
        }
    }
}

/**
 * Item novo da lista, com o nome que a pessoa usou. Produto do catálogo leva o `id` e a seção dele;
 * produto desconhecido fica sem produto e vai para "Outros" ([CatalogSection.OTHER_ID]). A seção
 * escolhida pela pessoa ([SectionChoices]) é aplicada por quem põe na lista (plano §1.3 e §1.4).
 */
fun ParsedItem.toListItem(): ListItem = ListItem.create(
    name = name,
    productId = match?.product?.id,
    sectionId = match?.product?.sectionId ?: CatalogSection.OTHER_ID,
    quantity = quantity,
    detail = detail,
)
