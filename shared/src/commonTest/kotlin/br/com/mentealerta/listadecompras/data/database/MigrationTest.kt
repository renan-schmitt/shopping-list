package br.com.mentealerta.listadecompras.data.database

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * As migrations criam as tabelas como o Room espera (colunas, tipos, nulos e chave, que é o que ele
 * confere ao abrir o banco) e não mexem nos dados que já estavam lá.
 */
class MigrationTest {
    @Test
    fun from1To2CreatesTheSuggestionDismissalTableAndKeepsTheData() = runTest {
        val connection = BundledSQLiteDriver().open(":memory:")
        try {
            connection.execSQL(
                "CREATE TABLE `section_choice` (`product_key` TEXT NOT NULL, `section_id` TEXT NOT NULL, " +
                    "PRIMARY KEY(`product_key`))",
            )
            connection.execSQL("INSERT INTO section_choice VALUES ('coffee', 'beverages')")

            MIGRATION_1_2.migrate(connection)

            assertEquals(
                listOf(
                    Column("product_key", "TEXT", notNull = true, primaryKey = true),
                    Column("dismissed_at", "INTEGER", notNull = true, primaryKey = false),
                ),
                connection.columns("suggestion_dismissal"),
            )
            assertEquals(listOf("coffee" to "beverages"), connection.sectionChoices())
        } finally {
            connection.close()
        }
    }

    private data class Column(val name: String, val type: String, val notNull: Boolean, val primaryKey: Boolean)

    private fun SQLiteConnection.columns(table: String): List<Column> = prepare("PRAGMA table_info(`$table`)").use {
        buildList {
            while (it.step()) {
                add(Column(it.getText(1), it.getText(2), it.getLong(3) == 1L, it.getLong(5) > 0L))
            }
        }
    }

    private fun SQLiteConnection.sectionChoices(): List<Pair<String, String>> =
        prepare("SELECT product_key, section_id FROM section_choice").use {
            buildList { while (it.step()) add(it.getText(0) to it.getText(1)) }
        }
}
