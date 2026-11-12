package br.com.mentealerta.listadecompras.data.database

import androidx.room3.RoomDatabase

/** Builder de banco em memória da plataforma, para os testes. */
internal expect fun inMemoryDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>

/** Banco em memória com a mesma configuração do app (driver, migrations). */
internal fun createTestDatabase(): AppDatabase = createAppDatabase(inMemoryDatabaseBuilder())
