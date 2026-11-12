package br.com.mentealerta.listadecompras.normalization

/**
 * Primeira etapa da normalização (plano §1.3): tira os espaços das pontas, junta espaços repetidos,
 * passa para minúsculas e remove os acentos ("  Maçã " → "maca").
 *
 * Usa uma tabela própria em vez de `java.text.Normalizer`, que não existe no iOS; assim o resultado
 * é igual nas duas plataformas. Hífen, apóstrofo e demais sinais ficam como estão.
 */
fun normalize(text: String): String {
    val collapsed = text.trim().replace(WHITESPACE, " ").lowercase()
    return buildString(collapsed.length) {
        collapsed.forEach { char ->
            when {
                char in COMBINING_MARKS -> Unit
                else -> append(ACCENT_FREE[char] ?: char)
            }
        }
    }
}

private val WHITESPACE = Regex("\\s+")

/** Acentos soltos (U+0300–U+036F), como chegam quando o teclado manda letra e acento separados. */
private val COMBINING_MARKS = '̀'..'ͯ'

/** Letras latinas minúsculas com acento → sem acento (Latin-1 e Latin Extended-A). */
private val ACCENT_FREE: Map<Char, String> = buildMap {
    fun map(base: String, accented: String) = accented.forEach { put(it, base) }
    map("a", "àáâãäåāăą")
    map("c", "çćĉċč")
    map("d", "ďđ")
    map("e", "èéêëēĕėęě")
    map("g", "ĝğġģ")
    map("h", "ĥħ")
    map("i", "ìíîïĩīĭįı")
    map("j", "ĵ")
    map("k", "ķ")
    map("l", "ĺļľŀł")
    map("n", "ñńņňŉ")
    map("o", "òóôõöøōŏő")
    map("r", "ŕŗř")
    map("s", "śŝşš")
    map("t", "ţťŧ")
    map("u", "ùúûüũūŭůűų")
    map("w", "ŵ")
    map("y", "ýÿŷ")
    map("z", "źżž")
    put('æ', "ae")
    put('œ', "oe")
    put('ß', "ss")
}
