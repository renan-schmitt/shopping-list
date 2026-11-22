package br.com.mentealerta.listadecompras.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import br.com.mentealerta.listadecompras.voice.VoicePreferences
import kotlinx.coroutines.flow.first

/** [VoicePreferences] nas preferências simples do app (plano §4.3). */
internal class DataStoreVoicePreferences(private val dataStore: DataStore<Preferences>) : VoicePreferences {
    override suspend fun wasNoticeShown(): Boolean = dataStore.data.first()[NOTICE_SHOWN] == true

    override suspend fun setNoticeShown() {
        dataStore.edit { it[NOTICE_SHOWN] = true }
    }

    private companion object {
        val NOTICE_SHOWN = booleanPreferencesKey("voice_notice_shown")
    }
}
