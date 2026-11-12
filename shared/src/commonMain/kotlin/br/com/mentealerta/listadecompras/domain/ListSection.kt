package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.catalog.CatalogSection

/** Seção do mercado com os itens da lista que estão nela, na ordem em que entraram. */
data class ListSection(val section: CatalogSection, val items: List<ListItem>)
