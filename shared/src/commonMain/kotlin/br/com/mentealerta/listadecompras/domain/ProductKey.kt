package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.normalization.searchKey

/**
 * Chave que junta os nomes do mesmo produto, na escolha de seção (#56) e na previsão (#63): o `id`
 * do produto do catálogo (um `id` antigo vira o do produto que o absorveu), qualquer que seja o nome
 * usado ("aipim" e "mandioca" juntos), ou, para produto desconhecido, a chave de busca do nome
 * ("Sabões de coco" = "sabão de coco").
 */
fun productKey(productId: String?, name: String, index: CatalogIndex): String =
    productId?.let { index.product(it)?.id ?: it } ?: searchKey(name)
