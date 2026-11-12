package br.com.mentealerta.listadecompras.data.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class PreferencesDataStoreTest {
    private val path = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "${Uuid.random()}.preferences_pb"
    private val key = booleanPreferencesKey("example")

    @AfterTest
    fun tearDown() {
        FileSystem.SYSTEM.delete(path, mustExist = false)
    }

    @Test
    fun startsWithoutValues() = runTest {
        assertNull(dataStore().data.first()[key])
    }

    @Test
    fun keepsWhatWasSavedAfterReopening() = runTest {
        val firstScope = CoroutineScope(coroutineContext + Job())
        createPreferencesDataStore(path.toString(), firstScope).edit { it[key] = true }
        // Só pode haver um DataStore aberto por arquivo: fecha o primeiro antes de reabrir
        firstScope.coroutineContext.job.cancelAndJoin()

        assertEquals(true, dataStore().data.first()[key])
    }

    private fun TestScope.dataStore() = createPreferencesDataStore(path.toString(), backgroundScope)
}
