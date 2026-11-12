package br.com.mentealerta.listadecompras.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import okio.Path.Companion.toPath

/** Nome do arquivo das preferências simples (ajustes do app). */
internal const val PREFERENCES_FILE_NAME = "settings.preferences_pb"

/**
 * Preferências simples (plano §4.3), num arquivo em [path]. Deve existir uma só instância por
 * arquivo: no app, quem garante é o Koin (`single`).
 */
internal fun createPreferencesDataStore(
    path: String,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(scope = scope, produceFile = { path.toPath() })
