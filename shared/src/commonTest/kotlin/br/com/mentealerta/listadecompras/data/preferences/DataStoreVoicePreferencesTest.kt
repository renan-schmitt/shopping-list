package br.com.mentealerta.listadecompras.data.preferences

import kotlinx.coroutines.test.runTest
import okio.FileSystem
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class DataStoreVoicePreferencesTest {
    private val path = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "${Uuid.random()}.preferences_pb"

    @AfterTest
    fun tearDown() {
        FileSystem.SYSTEM.delete(path, mustExist = false)
    }

    @Test
    fun noticeIsShownOnlyOnce() = runTest {
        val preferences = DataStoreVoicePreferences(createPreferencesDataStore(path.toString(), backgroundScope))

        assertFalse(preferences.wasNoticeShown())
        preferences.setNoticeShown()
        assertTrue(preferences.wasNoticeShown())
    }
}
