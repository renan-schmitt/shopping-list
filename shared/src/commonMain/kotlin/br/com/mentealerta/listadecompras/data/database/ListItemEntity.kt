package br.com.mentealerta.listadecompras.data.database

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import br.com.mentealerta.listadecompras.domain.QuantityUnit
import kotlin.time.Instant

/**
 * Item da lista de compras ([br.com.mentealerta.listadecompras.domain.ListItem]).
 *
 * A quantidade fica em duas colunas, as duas nulas quando a pessoa não informou. [addedAt] dá a
 * ordem dentro da seção: o item que volta com o "Ops, voltar" mantém a data e volta para o lugar.
 */
@Entity(tableName = "list_item")
internal data class ListItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "product_id") val productId: String?,
    @ColumnInfo(name = "section_id") val sectionId: String,
    @ColumnInfo(name = "quantity_amount") val quantityAmount: Double?,
    @ColumnInfo(name = "quantity_unit") val quantityUnit: QuantityUnit?,
    val detail: String?,
    val checked: Boolean,
    @ColumnInfo(name = "added_at") val addedAt: Instant,
)
