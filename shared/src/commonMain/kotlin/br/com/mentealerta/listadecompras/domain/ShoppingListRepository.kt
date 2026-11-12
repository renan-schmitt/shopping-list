package br.com.mentealerta.listadecompras.domain

import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

/**
 * A lista de compras, guardada só no celular. A implementação fica em `data`; quem usa recebe esta
 * interface pelo Koin.
 */
interface ShoppingListRepository {
    /**
     * A lista agrupada por seção, na ordem típica do mercado, sem seções vazias. Dentro da seção, os
     * itens ficam na ordem em que entraram; riscar ou editar não muda o lugar. Emite de novo a cada
     * mudança.
     */
    fun observeSections(): Flow<List<ListSection>>

    /** Põe o item na lista, no fim da seção dele. */
    suspend fun add(item: ListItem)

    /**
     * Grava a edição do item (nome, produto, seção, quantidade, detalhe e risco) sem mudar o lugar
     * dele. Devolve o item como estava antes, para o "Ops, voltar", ou `null` se o item não está mais
     * na lista.
     */
    suspend fun update(item: ListItem): ListItem?

    /**
     * Risca ou tira o risco do item. Devolve o item como estava antes, para o "Ops, voltar", ou `null`
     * se nada mudou: o item não está mais na lista ou já estava assim.
     */
    suspend fun setChecked(id: ListItemId, checked: Boolean): ListItem?

    /** Tira o item da lista. O que volta serve para o "Ops, voltar" ([restore]). */
    suspend fun remove(id: ListItemId): RemovedItems

    /** Joga a lista fora: tira todos os itens. O que volta serve para o "Ops, voltar" ([restore]). */
    suspend fun clear(): RemovedItems

    /** Devolve à lista os itens tirados, com o mesmo `id` e no mesmo lugar de antes. */
    suspend fun restore(removed: RemovedItems)
}

/**
 * Itens tirados da lista, guardados do jeito que estavam para voltarem ao mesmo lugar. Quem usa só
 * passa adiante para [ShoppingListRepository.restore].
 */
class RemovedItems internal constructor(internal val entries: List<Entry>) {
    /** Os itens tirados, na ordem em que estavam na lista. */
    val items: List<ListItem> get() = entries.map { it.item }

    fun isEmpty(): Boolean = entries.isEmpty()

    /** Item com o momento em que entrou na lista, que dá o lugar dele. */
    internal data class Entry(val item: ListItem, val addedAt: Instant)

    companion object {
        /** Nada foi tirado (ex.: o item já não estava na lista). */
        val NONE = RemovedItems(emptyList())
    }
}
