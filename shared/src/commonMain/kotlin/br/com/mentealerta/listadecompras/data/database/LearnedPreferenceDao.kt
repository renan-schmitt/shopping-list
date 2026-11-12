package br.com.mentealerta.listadecompras.data.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query

/**
 * O que o app aprende com a pessoa: termo preferido e seção escolhida para cada produto. Gravar
 * substitui o valor anterior (`REPLACE`, ver [ListItemDao]).
 */
@Dao
internal interface LearnedPreferenceDao {
    @Query("SELECT term FROM preferred_term WHERE product_id = :productId")
    suspend fun getPreferredTerm(productId: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPreferredTerm(preferredTerm: PreferredTermEntity)

    @Query("SELECT section_id FROM section_choice WHERE product_key = :productKey")
    suspend fun getSectionChoice(productKey: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSectionChoice(sectionChoice: SectionChoiceEntity)
}
