package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.normalization.searchKey

/**
 * Lembra a seção que a pessoa escolheu para um produto (plano §1.4, #56). A escolha vale para o
 * produto do catálogo (pelo `id`, qualquer que seja o nome usado: "aipim" e "mandioca" juntos) ou,
 * para produto desconhecido, para o nome normalizado ("Sabões de coco" = "sabão de coco").
 */
class RememberSectionUseCase(private val index: CatalogIndex, private val repository: SectionChoiceRepository) {
    /** Guarda a seção do [item] como a escolha da pessoa para o produto dele. */
    suspend fun learn(item: ListItem) {
        repository.learn(sectionChoiceKey(item, index), item.sectionId)
    }

    /** As escolhas guardadas, para aplicar nos itens que entram ou mudam de produto. */
    suspend fun choices(): SectionChoices = SectionChoices(repository.choices(), index)
}

/** As seções escolhidas pela pessoa, já carregadas do banco ([RememberSectionUseCase.choices]). */
class SectionChoices internal constructor(private val choices: Map<String, String>, private val index: CatalogIndex) {
    /**
     * O [item] na seção escolhida para o produto dele, se houver; senão, como veio. Escolha de uma
     * seção que o catálogo não tem mais é ignorada.
     */
    fun applyTo(item: ListItem): ListItem {
        val sectionId = choices[sectionChoiceKey(item, index)]?.takeIf { index.section(it) != null } ?: return item
        return item.copy(sectionId = sectionId)
    }

    /** Se [a] e [b] são o mesmo produto para a escolha da seção. */
    fun sameProduct(a: ListItem, b: ListItem): Boolean = sectionChoiceKey(a, index) == sectionChoiceKey(b, index)

    companion object {
        /** Nenhuma escolha (ex.: antes de carregar do banco). */
        fun none(index: CatalogIndex): SectionChoices = SectionChoices(emptyMap(), index)
    }
}

/** Chave da escolha: o `id` do produto ou, para produto desconhecido, a chave de busca do nome. */
private fun sectionChoiceKey(item: ListItem, index: CatalogIndex): String =
    item.productId?.let { index.product(it)?.id ?: it } ?: searchKey(item.name)
