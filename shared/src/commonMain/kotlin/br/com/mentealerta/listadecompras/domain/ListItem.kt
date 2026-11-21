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

    /**
     * "Sim, mais um" (#54): soma na quantidade o que a pessoa pediu de novo ([more]). Sem quantidade,
     * o item conta como 1 unidade. Se [more] está em outra unidade (ou não veio), soma 1 na unidade
     * do item: "leite" → 2 leites; "2 pacotes" + "3 pacotes" → 5 pacotes; "1 kg" + "2 latas" → 2 kg.
     */
    fun addMore(more: Quantity?): ListItem {
        val current = quantity ?: Quantity(1.0)
        val added = more?.takeIf { it.unit == current.unit }?.amount ?: 1.0
        return copy(quantity = Quantity(current.amount + added, current.unit))
    }

    /** O botão "−" da edição (#55) tem efeito: sem quantidade, o item conta como 1 e não diminui. */
    val canDecreaseQuantity: Boolean get() = (quantity?.amount ?: 1.0) > 1.0

    /**
     * Botão "+" da edição (#55): soma 1 na unidade do item; sem quantidade, o item conta como 1 e vai
     * a 2. Menos de 1 (ex.: ½ kg) sobe para 1.
     */
    fun increaseQuantity(): ListItem {
        val current = quantity ?: Quantity(1.0)
        val amount = if (current.amount < 1.0) 1.0 else current.amount + 1.0
        return copy(quantity = Quantity(amount, current.unit))
    }

    /**
     * Botão "−" da edição (#55): tira 1, só acima de 1 ([canDecreaseQuantity]). Voltar a 1 numa
     * contagem simples apaga a quantidade, porque o "1" sozinho não aparece na lista; "2 kg" vai a "1 kg".
     */
    fun decreaseQuantity(): ListItem {
        val current = quantity?.takeIf { canDecreaseQuantity } ?: return this
        val amount = current.amount - 1.0
        val single = amount == 1.0 && current.unit == QuantityUnit.UNIT
        return copy(quantity = if (single) null else Quantity(amount, current.unit))
    }

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
