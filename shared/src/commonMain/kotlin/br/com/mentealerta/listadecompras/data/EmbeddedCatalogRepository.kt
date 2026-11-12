package br.com.mentealerta.listadecompras.data

import br.com.mentealerta.listadecompras.catalog.Catalog
import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.CatalogRepository
import br.com.mentealerta.listadecompras.catalog.EmbeddedCatalog

/**
 * Catálogo embarcado, gerado no build a partir de `catalog/` (plano §4.3).
 * O índice é montado uma vez, no primeiro uso.
 */
internal class EmbeddedCatalogRepository : CatalogRepository {
    private val index by lazy { CatalogIndex(EmbeddedCatalog.catalog) }

    override fun catalog(): Catalog = EmbeddedCatalog.catalog

    override fun index(): CatalogIndex = index
}
