package br.com.mentealerta.listadecompras.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import br.com.mentealerta.listadecompras.ads.AdMoment
import br.com.mentealerta.listadecompras.ads.AdPreferences
import br.com.mentealerta.listadecompras.ads.AdState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Instant

/** [AdPreferences] nas preferências simples do app (plano §4.3); entram no backup com elas. */
internal class DataStoreAdPreferences(private val dataStore: DataStore<Preferences>) : AdPreferences {
    override val state: Flow<AdState> = dataStore.data.map { it.toAdState() }

    override suspend fun update(transform: (AdState) -> AdState): AdState {
        var updated = AdState()
        dataStore.edit { preferences ->
            updated = transform(preferences.toAdState())
            preferences.write(updated)
        }
        return updated
    }

    private fun Preferences.toAdState() = AdState(
        firstUse = this[FIRST_USE]?.let(Instant::fromEpochMilliseconds),
        weekNoticeShownAt = this[WEEK_NOTICE_SHOWN_AT]?.let(Instant::fromEpochMilliseconds),
        counts = AdMoment.entries.associateWith { this[countKey(it)] ?: 0 },
        personalized = this[PERSONALIZED] ?: true,
    )

    private fun MutablePreferences.write(state: AdState) {
        state.firstUse?.let { this[FIRST_USE] = it.toEpochMilliseconds() } ?: remove(FIRST_USE)
        state.weekNoticeShownAt?.let { this[WEEK_NOTICE_SHOWN_AT] = it.toEpochMilliseconds() }
            ?: remove(WEEK_NOTICE_SHOWN_AT)
        AdMoment.entries.forEach { this[countKey(it)] = state.counts[it] ?: 0 }
        this[PERSONALIZED] = state.personalized
    }

    private companion object {
        val FIRST_USE = longPreferencesKey("ads_first_use")
        val WEEK_NOTICE_SHOWN_AT = longPreferencesKey("ads_week_notice_shown_at")
        val PERSONALIZED = booleanPreferencesKey("ads_personalized")

        fun countKey(moment: AdMoment) = intPreferencesKey(
            when (moment) {
                AdMoment.BackFromAdd -> "ads_count_back_from_add"
                AdMoment.Edit -> "ads_count_edit"
            },
        )
    }
}
