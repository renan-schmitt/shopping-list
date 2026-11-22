package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.normalization.ItemTextParser
import br.com.mentealerta.listadecompras.normalization.ParsedItem

/**
 * Transforma o que a pessoa falou em produtos (plano §3.3). Cada trecho (o que veio entre duas
 * pausas) é separado por conta própria pelo [ItemTextParser.parseAll]. Das alternativas de um
 * trecho, fica a que tem mais produtos do catálogo; no empate, a mais provável (a primeira).
 */
class ParseSpeechUseCase(private val parser: ItemTextParser) {
    /** [segments]: as alternativas de cada trecho, da mais provável para a menos. */
    operator fun invoke(segments: List<List<String>>): List<ParsedItem> = segments.flatMap(::bestAlternative)

    private fun bestAlternative(alternatives: List<String>): List<ParsedItem> = alternatives
        .map(parser::parseAll)
        .maxByOrNull { items -> items.count { it.match != null } }
        .orEmpty()
}
