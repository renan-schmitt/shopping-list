package br.com.mentealerta.listadecompras.data

import br.com.mentealerta.listadecompras.catalog.Catalog
import br.com.mentealerta.listadecompras.catalog.CatalogRepository
import br.com.mentealerta.listadecompras.catalog.EmbeddedCatalog

/** Catálogo embarcado, gerado no build a partir de `catalog/` (plano §4.3). */
internal class EmbeddedCatalogRepository : CatalogRepository {
    override fun catalog(): Catalog = EmbeddedCatalog.catalog
}
