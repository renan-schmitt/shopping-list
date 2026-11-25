package br.com.mentealerta.listadecompras.data.database

import androidx.room3.ColumnTypeConverters
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * Banco local do app (plano §4.3): lista, histórico e o que o app aprende. Fica só no celular.
 *
 * Mudou alguma tabela? Suba [version], rode o build para gerar o schema novo em `shared/schemas/`
 * (vai para o git) e escreva a migration em [MIGRATIONS], com teste. Nunca apagar os dados da
 * pessoa para migrar.
 */
@Database(
    entities = [
        ListItemEntity::class,
        PurchaseEntity::class,
        PurchasedItemEntity::class,
        PreferredTermEntity::class,
        SectionChoiceEntity::class,
        SuggestionDismissalEntity::class,
    ],
    version = 2,
)
@ColumnTypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun listItemDao(): ListItemDao

    abstract fun purchaseDao(): PurchaseDao

    abstract fun learnedPreferenceDao(): LearnedPreferenceDao
}

/** Gerado pelo Room em cada plataforma. */
@Suppress("KotlinNoActualForExpect")
internal expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

/** Nome do arquivo do banco. Mudou? Atualize as regras de backup em `androidApp/src/main/res/xml`. */
internal const val DATABASE_FILE_NAME = "shopping-list.db"

/** 1 → 2 (#64): a tabela do "Não" das sugestões. */
internal val MIGRATION_1_2 = object : Migration(1, 2) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `suggestion_dismissal` (`product_key` TEXT NOT NULL, " +
                "`dismissed_at` INTEGER NOT NULL, PRIMARY KEY(`product_key`))",
        )
    }
}

/** Migrations de uma versão para a seguinte, em ordem. */
internal val MIGRATIONS: List<Migration> = listOf(MIGRATION_1_2)

/**
 * Termina de montar o banco a partir do builder de cada plataforma: SQLite embarcado (mesma versão
 * no Android e no iOS), consultas fora da thread principal e as migrations.
 */
internal fun createAppDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase = builder
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.IO)
    .addMigrations(*MIGRATIONS.toTypedArray())
    .build()
