package br.com.mentealerta.listadecompras.normalization

/**
 * Quantos erros de digitação separam [typed] de [target]: letra trocada, faltando, sobrando ou duas
 * vizinhas invertidas ("mcaa" × "maca" é 1 erro). É a distância de Levenshtein com transposição
 * (Damerau restrita), usada pela busca aproximada.
 *
 * Com [prefix] = `true`, compara [typed] com o começo de [target] que der menos erros, desde que esse
 * começo tenha pelo menos o tamanho de [typed] (ou seja o [target] inteiro): assim "macax" acha
 * "macaxeira" enquanto a pessoa ainda digita, sem que "pao" ache tudo que começa com "pa".
 *
 * [targetStart] compara com [target] a partir dessa posição (uma palavra do meio), sem copiar o texto.
 *
 * Para de contar ao passar de [max] e devolve `max + 1`: é isso que deixa a busca rápida.
 */
fun editDistance(typed: String, target: String, max: Int, prefix: Boolean = false, targetStart: Int = 0): Int {
    val m = typed.length
    val remaining = target.length - targetStart
    // No prefixo, um começo com mais de m + max letras já passa do limite: nem precisa olhar.
    val n = if (prefix) minOf(remaining, m + max) else remaining
    val tooMany = max + 1
    val lengthGap = if (prefix) m - n else kotlin.math.abs(m - n)
    if (lengthGap > max) return tooMany

    var beforePrevious = IntArray(n + 1)
    var previous = IntArray(n + 1) { it }
    var current = IntArray(n + 1)
    for (i in 1..m) {
        current[0] = i
        var rowMin = i
        for (j in 1..n) {
            val cost = if (typed[i - 1] == target[targetStart + j - 1]) 0 else 1
            var value = minOf(previous[j] + 1, current[j - 1] + 1, previous[j - 1] + cost)
            if (i > 1 && j > 1 && isSwap(typed, i, target, targetStart + j)) {
                value = minOf(value, beforePrevious[j - 2] + 1)
            }
            current[j] = value
            if (value < rowMin) rowMin = value
        }
        if (rowMin > max) return tooMany
        val recycled = beforePrevious
        beforePrevious = previous
        previous = current
        current = recycled
    }
    val distance = if (prefix) (minOf(m, n)..n).minOf { previous[it] } else previous[n]
    return minOf(distance, tooMany)
}

/** As duas últimas letras de `typed[..i]` são as duas últimas de `target[..j]` invertidas. */
private fun isSwap(typed: String, i: Int, target: String, j: Int): Boolean =
    typed[i - 1] == target[j - 2] && typed[i - 2] == target[j - 1]
