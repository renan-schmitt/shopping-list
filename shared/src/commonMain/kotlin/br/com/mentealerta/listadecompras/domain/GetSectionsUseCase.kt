package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.catalog.CatalogRepository
import br.com.mentealerta.listadecompras.catalog.CatalogSection

/** Seções do mercado na ordem típica, com "Outros" por último. */
class GetSectionsUseCase(private val catalogRepository: CatalogRepository) {
    operator fun invoke(): List<CatalogSection> = catalogRepository.catalog().sections
}
