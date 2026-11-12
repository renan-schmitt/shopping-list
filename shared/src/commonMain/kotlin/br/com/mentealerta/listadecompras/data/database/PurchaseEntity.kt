package br.com.mentealerta.listadecompras.data.database

import androidx.room3.ColumnInfo
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import androidx.room3.Relation
import br.com.mentealerta.listadecompras.domain.QuantityUnit
import kotlin.time.Instant

/** Compra do histórico ([br.com.mentealerta.listadecompras.domain.Purchase]), gravada a cada "Terminei". */
@Entity(tableName = "purchase")
internal data class PurchaseEntity(@PrimaryKey val id: String, val date: Instant)

/**
 * Produto de uma compra. Sai junto com a compra (`CASCADE`); [position] guarda a ordem dos itens.
 * O índice em [productId] serve à previsão, que busca as compras de cada produto.
 */
@Entity(
    tableName = "purchased_item",
    foreignKeys = [
        ForeignKey(
            entity = PurchaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["purchase_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("purchase_id"), Index("product_id")],
)
internal data class PurchasedItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "purchase_id") val purchaseId: String,
    val position: Int,
    @ColumnInfo(name = "product_id") val productId: String?,
    val name: String,
    @ColumnInfo(name = "quantity_amount") val quantityAmount: Double?,
    @ColumnInfo(name = "quantity_unit") val quantityUnit: QuantityUnit?,
)

/** Compra com os produtos, lida numa transação. */
internal data class PurchaseWithItems(
    @Embedded val purchase: PurchaseEntity,
    @Relation(parentColumns = ["id"], entityColumns = ["purchase_id"])
    val items: List<PurchasedItemEntity>,
)
