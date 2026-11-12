package br.com.mentealerta.listadecompras.normalization

/** Chave de busca de um texto: todas as etapas da normalização em ordem ("Pães Franceses" → "pao frances"). */
fun searchKey(text: String): String = singularize(normalize(text))
