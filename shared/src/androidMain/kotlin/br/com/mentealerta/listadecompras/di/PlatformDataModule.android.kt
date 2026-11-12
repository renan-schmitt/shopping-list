package br.com.mentealerta.listadecompras.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room3.Room
import br.com.mentealerta.listadecompras.data.database.AppDatabase
import br.com.mentealerta.listadecompras.data.database.DATABASE_FILE_NAME
import br.com.mentealerta.listadecompras.data.database.createAppDatabase
import br.com.mentealerta.listadecompras.data.preferences.PREFERENCES_FILE_NAME
import br.com.mentealerta.listadecompras.data.preferences.createPreferencesDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

// Arquivos no armazenamento interno do app, que entra no Auto Backup (plano §1.8)
internal actual val platformDataModule: Module = module {
    single<AppDatabase> {
        val context = androidContext()
        createAppDatabase(
            Room.databaseBuilder<AppDatabase>(context, context.getDatabasePath(DATABASE_FILE_NAME).absolutePath),
        )
    }
    single<DataStore<Preferences>> {
        createPreferencesDataStore(androidContext().filesDir.resolve(PREFERENCES_FILE_NAME).absolutePath)
    }
}
