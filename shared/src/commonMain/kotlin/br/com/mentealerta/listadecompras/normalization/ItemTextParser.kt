package br.com.mentealerta.listadecompras.normalization

import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.ProductMatch
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.domain.QuantityUnit

/** Texto de um produto separado em partes, pronto para virar item da lista. */
data class ParsedItem(
    /** Palavras do produto como a pessoa escreveu ou falou ("leites", "Carne moída"). */
    val name: String,
    /** Produto do catálogo; `null` quando o app não reconhece o nome. */
    val match: ProductMatch?,
    /** `null` quando a pessoa não informou quantidade. */
    val quantity: Quantity?,
    /** O que sobra depois do produto, como foi escrito ("da marca X"); `null` quando não sobra nada. */
    val detail: String?,
)

/**
 * Lê o texto que a pessoa escreveu ou falou e o transforma em itens da lista.
 *
 * [parse] separa o texto de **um** produto em quantidade, unidade, produto e detalhe (plano §1.2):
 * "dois litros de leite" → 2 × litro de Leite; "meio quilo de carne moída"; "uma dúzia de ovos";
 * "café da marca X" → Café com detalhe "da marca X".
 *
 * Ordem: número ([matchNumber]), unidade ([matchUnit]) seguida ou não de "de/da/do", produto e
 * detalhe. Tudo é opcional menos o produto; unidade sem número vale 1 ("pacote de arroz").
 *
 * O produto é o maior começo do texto restante que o catálogo reconhece ([CatalogIndex.resolve]):
 * "leite desnatado" é o produto Leite desnatado, não Leite com detalhe. Quando o número ou a unidade
 * também fazem parte de um nome do catálogo ("meia" é o produto Meia, "caixa de fósforo" é termo de
 * Fósforo), vale a leitura em que o produto reconhecido vai mais longe no texto; no empate, o nome
 * mais longo. Sem produto reconhecido, o nome é todo o texto depois da quantidade.
 *
 * [parseAll] separa uma frase com vários produtos ("arroz feijão e dois litros de leite") e lê cada
 * um com [parse] (plano §3.3).
 */
class ItemTextParser(private val index: CatalogIndex) {

    /** `null` quando não há produto: texto vazio ou só quantidade ("2 kg"). */
    fun parse(text: String): ParsedItem? = parseWords(words(text))

    /**
     * Separa [text] em produtos (plano §3.3), na ordem em que aparecem; vazia quando não há produto.
     *
     * Separa em vírgula, ponto, ponto-e-vírgula e quebra de linha (vírgula e ponto entre dígitos não:
     * "1,5 kg"). Sem pontuação, como vem da voz, as fronteiras vêm do catálogo. Depois do começo de
     * cada item (quantidade e o maior nome que o catálogo conhece, então "biscoito água e sal" e
     * "pão de queijo" ficam inteiros), um novo item começa:
     * - na palavra "e", que é descartada;
     * - onde começa um produto conhecido, com ou sem quantidade antes ("arroz **dois litros de leite**").
     *
     * Só nome ou sinônimo exato gera fronteira, sem a busca aproximada. Depois de uma palavra de
     * ligação o produto não separa: "frango com batata" é Frango com detalhe "com batata". O resto
     * fica no item: detalhe de um produto conhecido ou parte do nome de um desconhecido. Por isso
     * dois desconhecidos seguidos, sem "e", viram um item só.
     */
    fun parseAll(text: String): List<ParsedItem> =
        splitOnSeparators(text).flatMap { segments(words(it)) }.mapNotNull(::parseWords)

    private fun parseWords(words: List<String>): ParsedItem? {
        if (words.isEmpty()) return null
        val readings = readings(words)
        return bestKnown(readings, words)?.toParsedItem(words) ?: readings.first().unknown(words)
    }

    /** Leitura em que o produto reconhecido vai mais longe; no empate, a com menos quantidade. */
    private fun bestKnown(readings: List<Reading>, words: List<String>): ParsedReading? = readings
        .mapNotNull { it.known(words) }
        .maxWithOrNull(compareBy<ParsedReading> { it.productEnd }.thenByDescending { it.reading.start })

    /** Leituras possíveis, da que usa mais palavras como quantidade para a que não usa nenhuma. */
    private fun readings(words: List<String>): List<Reading> {
        val number = matchNumber(words)
        val afterNumber = number?.wordCount ?: 0
        val unit = matchUnit(words.drop(afterNumber))
        return buildList {
            if (unit != null) {
                val afterUnit = afterNumber + unit.wordCount
                val connector = words.getOrNull(afterUnit)?.let(::normalize) in CONNECTORS
                add(Reading(afterUnit + if (connector) 1 else 0, Quantity(number?.amount ?: 1.0, unit.unit)))
            }
            if (number != null) add(Reading(afterNumber, Quantity(number.amount, QuantityUnit.UNIT)))
            add(Reading(start = 0, quantity = null))
        }
    }

    /** Separa as palavras de um trecho sem pontuação em grupos, um por produto. */
    private fun segments(words: List<String>): List<List<String>> = buildList {
        var start = 0
        while (start < words.size) {
            if (normalize(words[start]) == AND) {
                start++
                continue
            }
            var end = start + headSize(words.subList(start, words.size))
            while (end < words.size && !isBoundary(words, end)) end++
            add(words.subList(start, end))
            start = end
        }
    }

    /**
     * Palavras do começo do item que nunca são fronteira: quantidade e produto conhecido; sem
     * produto conhecido, a quantidade e ao menos uma palavra.
     */
    private fun headSize(words: List<String>): Int {
        val readings = readings(words)
        return bestKnown(readings, words)?.productEnd ?: maxOf(readings.first().start, 1)
    }

    /** Se um novo item começa em [words]`[position]`. */
    private fun isBoundary(words: List<String>, position: Int): Boolean = when {
        normalize(words[position]) == AND -> true
        normalize(words[position - 1]) in LINKING_WORDS -> false
        else -> words.subList(position, words.size).let { bestKnown(readings(it), it) != null }
    }

    /** Leitura em que o produto começa em [start], depois da [quantity]. */
    private inner class Reading(val start: Int, val quantity: Quantity?) {
        /** Com o maior começo reconhecido pelo catálogo, ou `null` se nenhum é reconhecido. */
        fun known(words: List<String>): ParsedReading? {
            val longest = minOf(words.size, start + index.longestKeyWords)
            return (longest downTo start + 1).firstNotNullOfOrNull { end ->
                index.resolve(words.subList(start, end).joinToString(" "))?.let { ParsedReading(this, end, it) }
            }
        }

        /** Produto desconhecido: o nome é todo o resto; `null` se não sobrou nada. */
        fun unknown(words: List<String>): ParsedItem? = words.drop(start).takeIf { it.isNotEmpty() }?.let {
            ParsedItem(it.joinToString(" "), match = null, quantity, detail = null)
        }
    }

    private class ParsedReading(val reading: Reading, val productEnd: Int, val match: ProductMatch) {
        fun toParsedItem(words: List<String>) = ParsedItem(
            name = words.subList(reading.start, productEnd).joinToString(" "),
            match = match,
            quantity = reading.quantity,
            detail = words.drop(productEnd).joinToString(" ").takeIf { it.isNotEmpty() },
        )
    }

    private companion object {
        val WHITESPACE = Regex("\\s+")

        /** Pontuação colada nas palavras ("leite." vindo da voz); hífen e apóstrofo ficam. */
        val PUNCTUATION = charArrayOf('.', ',', ';', ':', '!', '?', '"', '(', ')')

        /** Sempre separam produtos. */
        val SEPARATORS = setOf(';', '\n', '\r')

        /** Separam produtos, menos entre dígitos ("1,5", "0.5"). */
        val SEPARATORS_OUTSIDE_NUMBERS = setOf(',', '.')

        /** Ligam a unidade ao produto: "dois litros **de** leite". Já sem acento. */
        val CONNECTORS = setOf("de", "da", "do", "das", "dos")

        /** Antes delas um produto não começa item novo: "frango **com** batata". Já sem acento. */
        val LINKING_WORDS = CONNECTORS + setOf("com", "sem", "para", "pra", "em", "no", "na", "nos", "nas")

        /** Separa produtos fora de um nome do catálogo: "arroz **e** feijão". */
        const val AND = "e"

        fun words(text: String): List<String> =
            text.split(WHITESPACE).map { it.trim(*PUNCTUATION) }.filter { it.isNotEmpty() }

        fun splitOnSeparators(text: String): List<String> = buildList {
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
    }
}
