package br.com.mentealerta.listadecompras.data.database

import androidx.room3.ColumnTypeConverter
import kotlin.time.Instant

/**
 * Conversões de coluna. Datas em milissegundos desde 1970 (UTC). Enums, como a unidade da
 * quantidade, usam o conversor embutido do Room, que grava o nome (não a posição).
 */
internal object Converters {
    @ColumnTypeConverter
    fun instantToEpochMillis(instant: Instant): Long = instant.toEpochMilliseconds()

    @ColumnTypeConverter
    fun epochMillisToInstant(epochMillis: Long): Instant = Instant.fromEpochMilliseconds(epochMillis)
}
