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
 * Separa o texto de **um** produto em quantidade, unidade, produto e detalhe (plano §1.2):
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
 */
class ItemTextParser(private val index: CatalogIndex) {

    /** `null` quando não há produto: texto vazio ou só quantidade ("2 kg"). */
    fun parse(text: String): ParsedItem? {
        val words = text.split(WHITESPACE).map { it.trim(*PUNCTUATION) }.filter { it.isNotEmpty() }
        if (words.isEmpty()) return null
        val readings = readings(words)
        val known = readings.mapNotNull { it.known(words) }
        return known.maxWithOrNull(compareBy<ParsedReading> { it.productEnd }.thenByDescending { it.reading.start })
            ?.toParsedItem(words)
            ?: readings.first().unknown(words)
    }

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

    /** Leitura em que o produto começa em [start], depois da [quantity]. */
    private inner class Reading(val start: Int, val quantity: Quantity?) {
        /** Com o maior começo reconhecido pelo catálogo, ou `null` se nenhum é reconhecido. */
        fun known(words: List<String>): ParsedReading? = (words.size downTo start + 1).firstNotNullOfOrNull { end ->
            index.resolve(words.subList(start, end).joinToString(" "))?.let { ParsedReading(this, end, it) }
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

        /** Ligam a unidade ao produto: "dois litros **de** leite". Já sem acento. */
        val CONNECTORS = setOf("de", "da", "do", "das", "dos")
    }
}
