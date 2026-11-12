package br.com.mentealerta.listadecompras.data.database

import br.com.mentealerta.listadecompras.domain.ListItem
import br.com.mentealerta.listadecompras.domain.ListItemId
import br.com.mentealerta.listadecompras.domain.Purchase
import br.com.mentealerta.listadecompras.domain.PurchaseId
import br.com.mentealerta.listadecompras.domain.PurchasedItem
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.domain.QuantityUnit
import kotlin.time.Instant

/** Linha da tabela a partir do item; [addedAt] é quando o item entrou na lista. */
internal fun ListItem.toEntity(addedAt: Instant): ListItemEntity = ListItemEntity(
    id = id.value,
    name = name,
    productId = productId,
    sectionId = sectionId,
    quantityAmount = quantity?.amount,
    quantityUnit = quantity?.unit,
    detail = detail,
    checked = checked,
    addedAt = addedAt,
)

internal fun ListItemEntity.toDomain(): ListItem = ListItem(
    id = ListItemId(id),
    name = name,
    productId = productId,
    sectionId = sectionId,
    quantity = quantityOf(quantityAmount, quantityUnit),
    detail = detail,
    checked = checked,
)

/** Linhas das tabelas a partir da compra: a compra e os produtos, na ordem. */
internal fun Purchase.toEntities(): Pair<PurchaseEntity, List<PurchasedItemEntity>> =
    PurchaseEntity(id = id.value, date = date) to items.mapIndexed { position, item ->
        PurchasedItemEntity(
            purchaseId = id.value,
            position = position,
            productId = item.productId,
            name = item.name,
            quantityAmount = item.quantity?.amount,
            quantityUnit = item.quantity?.unit,
        )
    }

internal fun PurchaseWithItems.toDomain(): Purchase = Purchase(
    id = PurchaseId(purchase.id),
    date = purchase.date,
    items = items.sortedBy { it.position }.map {
        PurchasedItem(
            productId = it.productId,
            name = it.name,
            quantity = quantityOf(it.quantityAmount, it.quantityUnit),
        )
    },
)

private fun quantityOf(amount: Double?, unit: QuantityUnit?): Quantity? =
    if (amount != null && unit != null) Quantity(amount, unit) else null
