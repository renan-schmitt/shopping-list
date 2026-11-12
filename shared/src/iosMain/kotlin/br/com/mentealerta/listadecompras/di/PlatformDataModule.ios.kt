package br.com.mentealerta.listadecompras.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room3.Room
import br.com.mentealerta.listadecompras.data.database.AppDatabase
import br.com.mentealerta.listadecompras.data.database.DATABASE_FILE_NAME
import br.com.mentealerta.listadecompras.data.database.createAppDatabase
import br.com.mentealerta.listadecompras.data.preferences.PREFERENCES_FILE_NAME
import br.com.mentealerta.listadecompras.data.preferences.createPreferencesDataStore
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

// Arquivos na pasta Documentos do app, que entra no backup do iCloud
internal actual val platformDataModule: Module = module {
    single<AppDatabase> {
        createAppDatabase(Room.databaseBuilder<AppDatabase>("${documentDirectory()}/$DATABASE_FILE_NAME"))
    }
    single<DataStore<Preferences>> {
        createPreferencesDataStore("${documentDirectory()}/$PREFERENCES_FILE_NAME")
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val directory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(directory?.path) { "Pasta Documentos não encontrada" }
}
