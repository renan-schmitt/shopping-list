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

    @Query("SELECT * FROM preferred_term WHERE product_id IN (:productIds)")
    suspend fun getPreferredTerms(productIds: List<String>): List<PreferredTermEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPreferredTerm(preferredTerm: PreferredTermEntity)

    @Query("SELECT * FROM section_choice")
    suspend fun getSectionChoices(): List<SectionChoiceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSectionChoice(sectionChoice: SectionChoiceEntity)
}
