package br.com.mentealerta.listadecompras.data.database

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlin.time.Instant

/**
 * Termo que a pessoa usa para um produto do catálogo (ex.: `cassava` → "aipim"), para as sugestões
 * mostrarem o nome dela (plano §1.3).
 */
@Entity(tableName = "preferred_term")
internal data class PreferredTermEntity(
    @PrimaryKey @ColumnInfo(name = "product_id") val productId: String,
    val term: String,
)

/**
 * Seção que a pessoa escolheu para um produto (plano §1.4). [productKey] é o `id` do produto no
 * catálogo ou, para produto desconhecido, o nome normalizado; quem grava monta a chave.
 */
@Entity(tableName = "section_choice")
internal data class SectionChoiceEntity(
    @PrimaryKey @ColumnInfo(name = "product_key") val productKey: String,
    @ColumnInfo(name = "section_id") val sectionId: String,
)

/**
 * "Não" que a pessoa respondeu ao "Está faltando?" (plano §3.4, #64), o último de cada produto.
 * [productKey] é a mesma chave da escolha de seção; [dismissedAt] é quando ela tocou em "Não".
 */
@Entity(tableName = "suggestion_dismissal")
internal data class SuggestionDismissalEntity(
    @PrimaryKey @ColumnInfo(name = "product_key") val productKey: String,
    @ColumnInfo(name = "dismissed_at") val dismissedAt: Instant,
)
