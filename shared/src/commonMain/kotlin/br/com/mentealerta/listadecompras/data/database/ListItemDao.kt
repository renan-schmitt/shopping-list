package br.com.mentealerta.listadecompras.data.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import br.com.mentealerta.listadecompras.domain.QuantityUnit
import kotlinx.coroutines.flow.Flow

/**
 * Itens da lista de compras.
 *
 * "Adicionar ou atualizar" usa `REPLACE` em vez de `@Upsert`: o `@Upsert` reconhece o conflito pela
 * mensagem do erro, que não chega nos testes de host do Android. Só serve para tabela sem filhos
 * (`REPLACE` apaga e grava de novo a linha).
 */
@Dao
internal interface ListItemDao {
    /** Todos os itens, na ordem em que entraram na lista. Emite de novo a cada mudança. */
    @Query("SELECT * FROM list_item ORDER BY added_at, id")
    fun observeAll(): Flow<List<ListItemEntity>>

    @Query("SELECT * FROM list_item WHERE id = :id")
    suspend fun getById(id: String): ListItemEntity?

    /** Adiciona ou atualiza o item. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ListItemEntity)

    /** Adiciona ou atualiza vários itens de uma vez (ex.: "Ops, voltar" de "Jogar a lista fora"). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ListItemEntity>)

    /** Edita o item sem mexer em [ListItemEntity.addedAt]. Devolve quantas linhas mudaram (0 ou 1). */
    @Query(
        """
        UPDATE list_item
        SET name = :name, product_id = :productId, section_id = :sectionId,
            quantity_amount = :quantityAmount, quantity_unit = :quantityUnit, detail = :detail,
            checked = :checked
        WHERE id = :id
        """,
    )
    suspend fun update(
        id: String,
        name: String,
        productId: String?,
        sectionId: String,
        quantityAmount: Double?,
        quantityUnit: QuantityUnit?,
        detail: String?,
        checked: Boolean,
    ): Int

    /** Risca ou tira o risco. Devolve quantas linhas mudaram (0 ou 1). */
    @Query("UPDATE list_item SET checked = :checked WHERE id = :id")
    suspend fun setChecked(id: String, checked: Boolean): Int

    @Query("SELECT * FROM list_item ORDER BY added_at, id")
    suspend fun getAll(): List<ListItemEntity>

    @Query("DELETE FROM list_item WHERE id = :id")
    suspend fun deleteById(id: String)

    /** Tira da lista os itens riscados ("Terminei"). */
    @Query("DELETE FROM list_item WHERE checked = 1")
    suspend fun deleteChecked()

    /** Esvazia a lista ("Jogar a lista fora"). */
    @Query("DELETE FROM list_item")
    suspend fun deleteAll()
}
