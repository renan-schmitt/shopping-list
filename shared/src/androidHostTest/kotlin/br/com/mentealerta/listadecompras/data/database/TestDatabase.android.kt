package br.com.mentealerta.listadecompras.data.database

import androidx.room3.Room
import androidx.room3.RoomDatabase

internal actual fun inMemoryDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> =
    Room.inMemoryDatabaseBuilder<AppDatabase> { AppDatabaseConstructor.initialize() }
