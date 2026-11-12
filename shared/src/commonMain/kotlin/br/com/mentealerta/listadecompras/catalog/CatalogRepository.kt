package br.com.mentealerta.listadecompras.catalog

/** Acesso ao catálogo de produtos. A implementação fica em `data`; quem usa recebe esta interface pelo Koin. */
interface CatalogRepository {
    fun catalog(): Catalog

    /** Índice em memória do catálogo, para achar produto por `id`, seção, nome ou sinônimo. */
    fun index(): CatalogIndex
}
