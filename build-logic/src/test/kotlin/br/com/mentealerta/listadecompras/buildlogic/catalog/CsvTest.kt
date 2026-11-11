package br.com.mentealerta.listadecompras.buildlogic.catalog

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CsvTest {
    private val columns = listOf("a", "b")

    @Test
    fun readsQuotedFieldsWithCommasQuotesAndLineBreaks() {
        val table = parseCsv("x.csv", "a,b\n1,\"um, dois\"\n2,\"diz \"\"oi\"\"\"\n3,\"linha\nnova\"\n4,fim\n", columns)
        assertEquals(emptyList(), table.errors)
        assertEquals(listOf("um, dois", "diz \"oi\"", "linha\nnova", "fim"), table.rows.map { it["b"] })
        assertEquals(listOf(2, 3, 4, 6), table.rows.map { it.line })
    }

    @Test
    fun ignoresBomCrlfAndEmptyLines() {
        val table = parseCsv("x.csv", "﻿a,b\r\n1, x \r\n\r\n2,y", columns)
        assertEquals(emptyList(), table.errors)
        assertEquals(listOf("x", "y"), table.rows.map { it["b"] })
    }

    @Test
    fun reportsWrongHeader() {
        val table = parseCsv("x.csv", "a,c\n1,2\n", columns)
        assertTrue(table.errors.single().startsWith("x.csv:1: cabeçalho"))
    }

    @Test
    fun reportsWrongColumnCountAndOpenQuotes() {
        val table = parseCsv("x.csv", "a,b\n1,2,3\n4,\"aberta\n", columns)
        assertEquals(listOf("x.csv:3: aspas abertas sem fechar", "x.csv:2: 3 colunas, esperado 2"), table.errors)
    }
}
