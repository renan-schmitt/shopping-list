package br.com.mentealerta.listadecompras.normalization

/**
 * Segunda etapa da normalização (plano §1.3): passa cada palavra do plural para o singular
 * ("paes franceses" → "pao frances", "limoes" → "limao").
 *
 * Recebe o texto já passado pela [normalize] (minúsculas, sem acento). O resultado é só chave de
 * busca, nunca aparece na tela: o importante é que singular e plural caiam na mesma chave, mesmo
 * quando ela não é uma palavra de verdade ("brocolis" → "brocoli"). Hífen separa palavras
 * ("graos-de-bico" → "grao-de-bico"); palavras com dígito ou com menos de 3 letras ficam como estão.
 */
fun singularize(text: String): String = WORD.replace(text) { singularizeWord(it.value) }

private val WORD = Regex("[^ -]+")

private const val MIN_LENGTH = 3

private fun singularizeWord(word: String): String = when {
    word.length < MIN_LENGTH || word.any { it.isDigit() } -> word
    word in INVARIABLE -> word
    else -> IRREGULAR[word] ?: SUFFIX_RULES.firstNotNullOfOrNull { (plural, singular) ->
        if (word.endsWith(plural)) word.dropLast(plural.length) + singular else null
    } ?: word
}

/** Terminam em "s" no singular; sem acento não dá para saber pela regra ("frances" × "tomates"). */
private val INVARIABLE = setOf(
    "lapis", "ananas", "gas", "mes", "pires", "onibus", "virus", "bonus", "simples", "anis",
    "mais", "cais", "dois", "tres", "seis",
    "frances", "ingles", "japones", "chines", "portugues", "holandes",
)

/** Plurais que as regras de [SUFFIX_RULES] errariam. */
private val IRREGULAR = mapOf(
    "maes" to "mae",
    "franceses" to "frances",
    "ingleses" to "ingles",
    "japoneses" to "japones",
    "chineses" to "chines",
    "portugueses" to "portugues",
    "holandeses" to "holandes",
    "meses" to "mes",
    "gases" to "gas",
    "pures" to "pure",
    "pernis" to "pernil",
    "barris" to "barril",
    "funis" to "funil",
    "azuis" to "azul",
    "bois" to "boi",
    "reis" to "rei",
    "uteis" to "util",
    "fosseis" to "fossil",
)

/** Terminação do plural → do singular; vale a primeira que bater, por isso "s" fica por último. */
private val SUFFIX_RULES = listOf(
    "oes" to "ao", // limoes, feijoes, pimentoes
    "aes" to "ao", // paes, alemaes
    "ais" to "al", // cereais, integrais, sais
    "eis" to "el", // pasteis, descartaveis
    "ois" to "ol", // lencois, girassois
    "ns" to "m", // atuns, pudins, vagens, bombons
    "ares" to "ar", // acucares
    "eres" to "er", // colheres, hamburgueres
    "ires" to "ir",
    "ores" to "or", // flores, licores
    "ures" to "ur",
    "zes" to "z", // nozes
    "s" to "", // bananas, tomates, abacaxis, macas, graos
)
