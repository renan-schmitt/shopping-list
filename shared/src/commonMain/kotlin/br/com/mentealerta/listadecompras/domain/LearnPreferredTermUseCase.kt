package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.catalog.CatalogIndex

/**
 * Aprende o termo preferido (plano §1.3) a partir do item que a pessoa pôs na lista ou renomeou.
 *
 * Só aprende quando o nome do item é exatamente um nome ou sinônimo do catálogo para o produto do
 * item ("macaxeiras" → Mandioca). Grava a grafia do catálogo, com acento e no singular
 * ("macaxeira"). Nome livre ("cacetinho da padaria") e produto desconhecido não ensinam nada. Vale
 * o último termo usado.
 */
class LearnPreferredTermUseCase(private val index: CatalogIndex, private val repository: PreferredTermRepository) {
    suspend operator fun invoke(item: ListItem) {
        val productId = item.productId?.let { index.product(it)?.id } ?: return
        val match = index.resolve(item.name)?.takeIf { it.product.id == productId } ?: return
        repository.learn(productId, match.term?.term ?: match.product.name)
    }
}
