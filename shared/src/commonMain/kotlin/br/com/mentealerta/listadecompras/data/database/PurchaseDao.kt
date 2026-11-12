package br.com.mentealerta.listadecompras.data.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow

/** Histórico de compras. */
@Dao
internal interface PurchaseDao {
    /** Grava a compra e os produtos juntos: ou entra tudo, ou nada. */
    @Transaction
    suspend fun insert(purchase: PurchaseEntity, items: List<PurchasedItemEntity>) {
        insertPurchase(purchase)
        insertItems(items)
    }

    @Insert
    suspend fun insertPurchase(purchase: PurchaseEntity)

    @Insert
    suspend fun insertItems(items: List<PurchasedItemEntity>)

    /** Compras com os produtos, da mais recente para a mais antiga. */
    @Transaction
    @Query("SELECT * FROM purchase ORDER BY date DESC, id")
    fun observeAll(): Flow<List<PurchaseWithItems>>

    @Query("DELETE FROM purchase WHERE id = :id")
    suspend fun deleteById(id: String)
}
