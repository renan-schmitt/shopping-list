package br.com.mentealerta.listadecompras.normalization

import br.com.mentealerta.listadecompras.domain.QuantityUnit

/**
 * Limpa uma lista recebida em texto, como vem do WhatsApp (#68, plano §1.6), e devolve um trecho por
 * produto, pronto para o [ItemTextParser.parseAll]:
 *
 * - tira o cabeçalho de mensagem copiada ("[18:23, 24/11/2026] Maria: arroz", "24/11/2026 18:23 - Maria: arroz");
 * - tira o negrito, o itálico e o riscado do WhatsApp (`*`, `_`, `~`);
 * - tira marcadores do começo da linha ("-", "•", "☐", "[x]", emoji) e a numeração ("1.", "2)", "3-");
 * - pula títulos: linha que termina em ":" e linha que é só um dos [headings] ("Lista de compras",
 *   "Hortifrúti"); com "Limpeza: detergente", fica só o que vem depois dos dois-pontos;
 * - separa em vírgula, ponto e ponto-e-vírgula, menos entre dígitos ("1,5 kg");
 * - traz para o começo a quantidade escrita depois do produto: "Leite (2 litros)", "Leite 2 litros",
 *   "Arroz 5kg", "Banana x6" → "2 litros Leite", "5 kg Arroz", "6 Banana". Também separa número e
 *   unidade colados ("5kg arroz") e tira o "x" de "2x".
 *
 * [headings] vêm já como [searchKey].
 */
fun cleanPastedList(text: String, headings: Set<String>): List<String> = text
    .lines()
    .mapNotNull { line -> cleanLine(line, headings) }
    .flatMap(::splitOnSeparators)
    .map { moveTrailingQuantity(it.trim()) }
    .filter { piece -> piece.any { it.isLetter() } }

/**
 * Separa em `;`, `,` e `.`, menos `,` e `.` entre dígitos ("1,5", "0.5"). Quebra de linha também
 * separa. Usado também pelo [ItemTextParser.parseAll].
 */
internal fun splitOnSeparators(text: String): List<String> = buildList {
    val current = StringBuilder()
    text.forEachIndexed { i, char ->
        val betweenDigits = text.getOrNull(i - 1)?.isDigit() == true && text.getOrNull(i + 1)?.isDigit() == true
        val separates = char in SEPARATORS || (char in SEPARATORS_OUTSIDE_NUMBERS && !betweenDigits)
        if (separates) {
            add(current.toString())
            current.clear()
        } else {
            current.append(char)
        }
    }
    add(current.toString())
}

/** A linha sem cabeçalho, formatação e marcadores; `null` quando é título. */
private fun cleanLine(line: String, headings: Set<String>): String? {
    var text = MESSAGE_HEADERS.fold(line) { acc, header -> acc.replaceFirst(header, "") }
    text = text.filterNot { it in FORMATTING }
    text = stripMarkers(text)
    val trimmed = text.trim()
    if (trimmed.endsWith(':')) return null
    val colon = trimmed.indexOf(':')
    if (colon > 0 && searchKey(trimmed.substring(0, colon)) in headings) text = trimmed.substring(colon + 1)
    return text.trim().takeUnless { it.isEmpty() || searchKey(it.trimEnd(*TRAILING_JUNK)) in headings }
}

/** Tira marcadores e numeração do começo, quantas vezes aparecerem ("1. [ ] arroz"). */
private fun stripMarkers(text: String): String {
    var current = text
    while (true) {
        val next = current
            .trimStart { !it.isLetterOrDigit() && it != '(' && it != '[' }
            .replaceFirst(CHECKBOX, "")
            .replaceFirst(NUMBERING, "")
        if (next == current) return current
        current = next
    }
}

/** Traz a quantidade escrita depois do produto para o começo, onde o [ItemTextParser] a lê. */
private fun moveTrailingQuantity(piece: String): String {
    val parenthesis = TRAILING_PARENTHESIS.find(piece)
    val head = parenthesis?.let { piece.substring(0, it.range.first) } ?: piece
    val words = splitGlued(head.trim().trimEnd(*TRAILING_JUNK).trim())
    val inside = parenthesis?.let { splitGlued(it.groupValues[1].trim()) }
    val reordered = when {
        inside != null -> if (isQuantity(inside)) inside + words else words + inside
        // Já começa com número: a quantidade está no lugar.
        matchNumber(words) != null -> words
        else -> {
            val tailSize = (MAX_QUANTITY_WORDS downTo 1).firstOrNull { size ->
                size < words.size && isQuantity(words.takeLast(size), requireNumber = true)
            } ?: 0
            words.takeLast(tailSize) + words.dropLast(tailSize)
        }
    }
    return reordered.joinToString(" ")
}

/**
 * As palavras são só uma quantidade: número com ou sem unidade ("2", "2 litros"), ou só a unidade
 * ("pacote", "meia dúzia"), que sem [requireNumber] também vale. Depois do produto e fora de
 * parênteses, exige o número: "Biscoito pacote" pode ser o nome.
 */
private fun isQuantity(words: List<String>, requireNumber: Boolean = false): Boolean {
    if (words.isEmpty()) return false
    val number = matchNumber(words)
    val afterNumber = number?.wordCount ?: 0
    val unit = matchUnit(words.drop(afterNumber))
    // "Ovos meia dúzia": "meia dúzia" é unidade, mas conta como número.
    val unitOnly = !requireNumber || unit?.unit == QuantityUnit.HALF_DOZEN
    return (number != null || (unit != null && unitOnly)) && afterNumber + (unit?.wordCount ?: 0) == words.size
}

/** Palavras, com "5kg" → "5", "kg" e "2x"/"x2" → "2". */
private fun splitGlued(text: String): List<String> = text.split(WHITESPACE).filter { it.isNotEmpty() }.flatMap { word ->
    TIMES.matchEntire(word)?.let { listOfNotNull(it.groupValues[1].ifEmpty { null } ?: it.groupValues[2]) }
        ?: GLUED_UNIT.matchEntire(word)?.let { listOf(it.groupValues[1], it.groupValues[2]) }
        ?: listOf(word)
}

private val WHITESPACE = Regex("\\s+")

private val SEPARATORS = setOf(';', '\n', '\r')
private val SEPARATORS_OUTSIDE_NUMBERS = setOf(',', '.')

/** Negrito, itálico, riscado e código do WhatsApp. */
private val FORMATTING = setOf('*', '_', '~', '`')

/** Sobras no fim de um trecho: "Leite -", "Arroz!". */
private val TRAILING_JUNK = charArrayOf('-', '–', '—', ':', '!', '?', ' ', '=')

/** Mensagens copiadas do WhatsApp: "[18:23, 24/11/2026] Maria: " e "24/11/2026 18:23 - Maria: ". */
private val MESSAGE_HEADERS = listOf(
    Regex("^\\s*\\[[^\\]]*\\d{1,2}:\\d{2}[^\\]]*]\\s*[^:]{1,40}:\\s*"),
    Regex("^\\s*\\d{1,2}/\\d{1,2}/\\d{2,4},?\\s+\\d{1,2}:\\d{2}\\s*-\\s*[^:]{1,40}:\\s*"),
)

/** "[ ]", "[x]", "( )", "(x)" no começo. */
private val CHECKBOX = Regex("^[\\[(]\\s*[xX✓✔]?\\s*[])]\\s*")

/** "1.", "2)", "3-", "4º" no começo, seguido de espaço; "1.5 kg" e "2 litros" não. */
private val NUMBERING = Regex("^\\d{1,2}\\s*[.)\\-–º°]\\s+")

/** Parênteses no fim: "Leite (2 litros)". */
private val TRAILING_PARENTHESIS = Regex("\\(([^()]*)\\)\\s*$")

/** "2x", "x2", "2X". */
private val TIMES = Regex("^(?:(\\d+)[xX]|[xX](\\d+))$")

/** Número e unidade colados: "5kg", "500g", "2l", "1,5kg". */
private val GLUED_UNIT = Regex("^(\\d+(?:[.,]\\d+)?)([a-zA-Z]+)$")

/** Maior quantidade escrita depois do produto: "dois e meio quilos" (número de 3 palavras + unidade). */
private const val MAX_QUANTITY_WORDS = 4
