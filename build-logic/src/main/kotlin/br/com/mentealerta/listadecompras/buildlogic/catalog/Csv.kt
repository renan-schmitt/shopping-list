package br.com.mentealerta.listadecompras.buildlogic.catalog

/** Uma linha de dados de um CSV, com o número da linha no arquivo (o cabeçalho é a linha 1). */
data class CsvRow(val line: Int, private val values: Map<String, String>) {
    operator fun get(column: String): String = values.getValue(column)
}

/** Resultado da leitura de um CSV: as linhas lidas e os erros de formato encontrados. */
data class CsvTable(val rows: List<CsvRow>, val errors: List<String>)

/**
 * Lê um CSV (RFC 4180: vírgula, campos entre aspas, `""` para aspas dentro do campo).
 *
 * As colunas do cabeçalho precisam ser exatamente [columns], nesta ordem. Linhas vazias são
 * ignoradas. Os valores voltam sem espaços nas pontas.
 */
fun parseCsv(fileName: String, text: String, columns: List<String>): CsvTable {
    val records = splitRecords(fileName, text)
    val errors = records.errors.toMutableList()
    val header = records.records.firstOrNull()
    if (header == null) {
        return CsvTable(emptyList(), errors + "$fileName: arquivo vazio, sem cabeçalho")
    }
    if (header.fields.map { it.trim() } != columns) {
        val expected = columns.joinToString(",")
        val found = header.fields.joinToString(",")
        return CsvTable(emptyList(), errors + "$fileName:1: cabeçalho deve ser \"$expected\", veio \"$found\"")
    }
    val rows = records.records.drop(1).mapNotNull { record ->
        when {
            record.fields.all { it.isBlank() } -> null
            record.fields.size != columns.size -> {
                errors += "$fileName:${record.line}: ${record.fields.size} colunas, esperado ${columns.size}"
                null
            }
            else -> CsvRow(record.line, columns.zip(record.fields.map { it.trim() }).toMap())
        }
    }
    return CsvTable(rows, errors)
}

private data class CsvRecord(val line: Int, val fields: List<String>)

private data class CsvRecords(val records: List<CsvRecord>, val errors: List<String>)

private fun splitRecords(fileName: String, text: String): CsvRecords {
    val records = mutableListOf<CsvRecord>()
    val errors = mutableListOf<String>()
    val fields = mutableListOf<String>()
    val field = StringBuilder()
    var inQuotes = false
    var line = 1
    var recordLine = 1
    var i = if (text.startsWith('﻿')) 1 else 0

    fun endRecord() {
        fields += field.toString()
        field.clear()
        records += CsvRecord(recordLine, fields.toList())
        fields.clear()
    }

    while (i < text.length) {
        val c = text[i]
        if (inQuotes) {
            when {
                c == '"' && text.getOrNull(i + 1) == '"' -> {
                    field.append('"')
                    i++
                }
                c == '"' -> inQuotes = false
                else -> {
                    if (c == '\n') line++
                    field.append(c)
                }
            }
        } else {
            when (c) {
                '"' -> inQuotes = true
                ',' -> {
                    fields += field.toString()
                    field.clear()
                }
                '\r' -> Unit
                '\n' -> {
                    endRecord()
                    line++
                    recordLine = line
                }
                else -> field.append(c)
            }
        }
        i++
    }
    if (inQuotes) errors += "$fileName:$recordLine: aspas abertas sem fechar"
    if (field.isNotEmpty() || fields.isNotEmpty()) endRecord()
    return CsvRecords(records, errors)
}
