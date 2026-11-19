package br.com.mentealerta.listadecompras.normalization

import br.com.mentealerta.listadecompras.domain.QuantityUnit

/** Número encontrado no começo de uma lista de palavras e quantas palavras ele ocupa. */
data class NumberMatch(val amount: Double, val wordCount: Int)

/**
 * Reconhece o número (plano §1.2) no começo de [words] ("**dois** litros de leite", "**meio** quilo",
 * "**1,5** kg"). Devolve o valor e quantas palavras ele ocupa, para quem separa a frase saber onde
 * começa a unidade, ou `null` se não há número.
 *
 * Aceita por extenso de um a vinte (masculino e feminino, com ou sem acento), "meio"/"meia",
 * "dois e meio" e dígitos com vírgula ou ponto ("2", "1,5", "0.5"). Só valores maiores que zero.
 *
 * Em "meia dúzia" não há número: a expressão inteira é a unidade [QuantityUnit.HALF_DOZEN] (ver
 * [matchUnit]), e não 0,5 × dúzia.
 */
fun matchNumber(words: List<String>): NumberMatch? {
    val first = words.firstOrNull()?.let(::normalize) ?: return null
    if (matchUnit(words)?.unit == QuantityUnit.HALF_DOZEN) return null
    val whole = WORDS[first] ?: parseDigits(first) ?: return null
    val andHalf = words.drop(1).take(2).map(::normalize) in AND_HALF
    return if (whole >= 1 && andHalf) {
        NumberMatch(whole + HALF, wordCount = 3)
    } else {
        NumberMatch(whole, wordCount = 1)
    }
}

/** "2", "1,5" ou "0.5" → valor, se for maior que zero. */
private fun parseDigits(word: String): Double? = word
    .takeIf { DIGITS.matches(it) }
    ?.replace(',', '.')
    ?.toDoubleOrNull()
    ?.takeIf { it > 0 }

private val DIGITS = Regex("\\d+([.,]\\d+)?")

private const val HALF = 0.5

/** "e meio" / "e meia" depois de um número inteiro ("dois e meio"), já sem acento. */
private val AND_HALF = setOf(listOf("e", "meio"), listOf("e", "meia"))

/** Números por extenso, já sem acento ([normalize]). */
private val WORDS: Map<String, Double> = buildMap {
    fun number(value: Number, vararg keys: String) = keys.forEach { put(it, value.toDouble()) }
    number(HALF, "meio", "meia")
    number(1, "um", "uma")
    number(2, "dois", "duas")
    number(3, "tres")
    number(4, "quatro")
    number(5, "cinco")
    number(6, "seis")
    number(7, "sete")
    number(8, "oito")
    number(9, "nove")
    number(10, "dez")
    number(11, "onze")
    number(12, "doze")
    number(13, "treze")
    number(14, "quatorze", "catorze")
    number(15, "quinze")
    number(16, "dezesseis", "dezasseis")
    number(17, "dezessete", "dezassete")
    number(18, "dezoito")
    number(19, "dezenove", "dezanove")
    number(20, "vinte")
}
