package br.com.mentealerta.listadecompras.domain

import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

/** Compra do histórico, gravada a cada "Terminei": a data e o que foi comprado. */
data class Purchase(val id: PurchaseId, val date: Instant, val items: List<PurchasedItem>) {
    init {
        require(items.isNotEmpty()) { "Compra sem produtos" }
    }

    companion object {
        /** Compra nova, com `id` novo, a partir dos itens riscados. */
        fun create(date: Instant, items: List<ListItem>): Purchase =
            Purchase(id = PurchaseId.random(), date = date, items = items.map { it.toPurchasedItem() })
    }
}

/**
 * Produto comprado, copiado do item no momento da compra. A previsão usa [productId] (ou [name],
 * para produto desconhecido); a quantidade fica gravada para uso futuro (plano §1.5).
 */
data class PurchasedItem(val productId: String?, val name: String, val quantity: Quantity?) {
    init {
        require(name.isNotBlank()) { "Produto comprado sem nome" }
    }
}

/** Identificador da compra, em UUID. */
@JvmInline
value class PurchaseId(val value: String) {
    companion object {
        fun random(): PurchaseId = PurchaseId(Uuid.random().toString())
    }
}
