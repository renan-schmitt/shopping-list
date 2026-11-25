package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.CatalogSection
import kotlin.time.Clock

/**
 * **Sim, pôr na lista** do "Está faltando?" (plano §1.5, #64): o produto entra com o nome da última
 * compra, que já é o que a pessoa usa, sem quantidade, na seção do catálogo (desconhecido, em
 * "Outros") ou na que ela escolheu para ele ([RememberSectionUseCase]).
 */
class AcceptSuggestionUseCase(
    private val repository: ShoppingListRepository,
    private val rememberSection: RememberSectionUseCase,
    private val index: CatalogIndex,
) {
    /** Devolve o item como entrou na lista. */
    suspend operator fun invoke(product: OverdueProduct): ListItem {
        val pattern = product.pattern
        val catalogProduct = pattern.productId?.let { index.product(it) }
        val item = ListItem.create(
            name = pattern.name,
            productId = catalogProduct?.id ?: pattern.productId,
            sectionId = catalogProduct?.sectionId ?: CatalogSection.OTHER_ID,
        )
        return rememberSection.choices().applyTo(item).also { repository.add(it) }
    }
}

/**
 * **Não** do "Está faltando?" (plano §3.4, #64): o produto só volta depois de mais um intervalo
 * inteiro, contado de agora; comprar recomeça a contagem ([ObserveOverdueProductsUseCase]).
 */
class DismissSuggestionUseCase(private val repository: SuggestionDismissalRepository, private val clock: Clock) {
    suspend operator fun invoke(product: OverdueProduct) {
        repository.dismiss(product.pattern.key, clock.now())
    }
}
