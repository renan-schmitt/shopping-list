package br.com.mentealerta.listadecompras.normalization

import br.com.mentealerta.listadecompras.domain.QuantityUnit

/** Unidade encontrada no começo de uma lista de palavras e quantas palavras ela ocupa. */
data class UnitMatch(val unit: QuantityUnit, val wordCount: Int)

/**
 * Reconhece a unidade (plano §1.2) no começo de [words], as palavras que vêm logo depois do número
 * ("dois **litros** de leite", "uma **meia dúzia** de ovos"). Devolve a unidade e quantas palavras
 * ela ocupa, para quem separa a frase saber onde começa o produto, ou `null` se não há unidade.
 *
 * Aceita maiúsculas, acentos, plural, abreviações ("kg", "cx", "pct") e diminutivos ("caixinha").
 * Expressões mais longas ganham: "meia dúzia" é [QuantityUnit.HALF_DOZEN], não "meia" + "dúzia".
 */
fun matchUnit(words: List<String>): UnitMatch? {
    val keys = words.asSequence().take(MAX_WORDS).map(::unitKey).toList()
    return (keys.size downTo 1).firstNotNullOfOrNull { count ->
        UNITS[keys.take(count).joinToString(" ")]?.let { UnitMatch(it, count) }
    }
}

/** Chave de uma palavra na tabela: sem acento, minúscula e no singular ("Maços" → "maco"). */
private fun unitKey(word: String): String {
    val normalized = normalize(word)
    return ABBREVIATION_PLURALS[normalized] ?: singularize(normalized)
}

/** Plurais curtos que o [singularize] não trata (deixa como estão palavras com menos de 3 letras). */
private val ABBREVIATION_PLURALS = mapOf(
    "kgs" to "kg",
    "grs" to "gr",
    "lts" to "lt",
    "dzs" to "dz",
    "uns" to "un",
    "cxs" to "cx",
)

/** Variações de cada unidade, já como chave ([unitKey]). */
private val UNITS: Map<String, QuantityUnit> = buildMap {
    fun unit(unit: QuantityUnit, vararg keys: String) = keys.forEach { put(it, unit) }
    unit(QuantityUnit.UNIT, "unidade", "un", "und", "unid")
    unit(QuantityUnit.KILOGRAM, "kg", "quilo", "kilo", "quilograma", "kilograma")
    unit(QuantityUnit.GRAM, "g", "gr", "grama")
    unit(QuantityUnit.LITER, "l", "lt", "litro")
    unit(QuantityUnit.DOZEN, "duzia", "dz")
    unit(QuantityUnit.HALF_DOZEN, "meia duzia", "meia dz")
    unit(QuantityUnit.PACKAGE, "pacote", "pct", "pacotinho")
    unit(QuantityUnit.BOX, "caixa", "cx", "caixinha")
    unit(QuantityUnit.CAN, "lata", "latinha")
    unit(QuantityUnit.BOTTLE, "garrafa", "garrafinha")
    unit(QuantityUnit.BUNCH, "maco", "macinho")
    unit(QuantityUnit.TRAY, "bandeja", "bandejinha")
    unit(QuantityUnit.JAR, "pote", "potinho")
}

/** Maior número de palavras de uma variação da tabela ("meia duzia"). */
private val MAX_WORDS = UNITS.keys.maxOf { it.split(" ").size }
