package br.com.mentealerta.listadecompras.domain

import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

/**
 * Produto na lista de compras.
 *
 * Mostra sempre o nome que a pessoa usou ([name], ex.: "aipim") e aponta para o produto canônico do
 * catálogo ([productId], ex.: `cassava`), que dá o ícone e o histórico.
 */
data class ListItem(
    val id: ListItemId,
    /** Nome como a pessoa escreveu ou falou, sem espaços nas pontas. */
    val name: String,
    /** `id` do produto no catálogo; `null` para produto que o app não reconhece. */
    val productId: String?,
    /** Seção onde o item aparece. Fica no item porque a pessoa pode trocar (#56). */
    val sectionId: String,
    /** `null` quando a pessoa não informou quantidade. */
    val quantity: Quantity? = null,
    /** Detalhe livre junto ao nome ("desnatado", "da marca X"); `null` quando não há. */
    val detail: String? = null,
    /** Riscado (já está no carrinho). */
    val checked: Boolean = false,
) {
    init {
        require(name.isNotBlank() && name == name.trim()) { "Nome vazio ou com espaços nas pontas" }
        require(detail == null || (detail.isNotBlank() && detail == detail.trim())) {
            "Detalhe vazio ou com espaços nas pontas"
        }
        require(sectionId.isNotBlank()) { "Item sem seção" }
    }

    /** Risca o item. */
    fun check(): ListItem = copy(checked = true)

    /** Tira o risco do item. */
    fun uncheck(): ListItem = copy(checked = false)

    /** Cópia do item para gravar no histórico. */
    fun toPurchasedItem(): PurchasedItem = PurchasedItem(productId = productId, name = name, quantity = quantity)

    companion object {
        /**
         * Item novo, ainda não riscado, com `id` novo. Tira os espaços das pontas do nome e do
         * detalhe; detalhe em branco vira `null`.
         */
        fun create(
            name: String,
            productId: String?,
            sectionId: String,
            quantity: Quantity? = null,
            detail: String? = null,
        ): ListItem = ListItem(
            id = ListItemId.random(),
            name = name.trim(),
            productId = productId,
            sectionId = sectionId,
            quantity = quantity,
            detail = detail?.trim()?.takeIf { it.isNotEmpty() },
        )
    }
}

/** Identificador do item, em UUID: o "Ops, voltar" devolve o item com o mesmo `id`. */
@JvmInline
value class ListItemId(val value: String) {
    companion object {
        fun random(): ListItemId = ListItemId(Uuid.random().toString())
    }
}
