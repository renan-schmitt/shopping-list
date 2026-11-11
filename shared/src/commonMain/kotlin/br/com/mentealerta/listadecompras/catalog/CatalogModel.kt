package br.com.mentealerta.listadecompras.catalog

/**
 * Catálogo de produtos embarcado no app, gerado da planilha mestre em `catalog/`
 * (ver `catalog/README.md`). Só dados: os textos de nome e termo são pt-BR, e quem monta frase é a UI.
 */
data class Catalog(
    /** Seções na ordem típica do mercado; "Outros" ([CatalogSection.OTHER_ID]) é sempre a última. */
    val sections: List<CatalogSection>,
    /** Produtos ativos, na ordem da seção e depois na ordem dentro da seção. */
    val products: List<CatalogProduct>,
    /** Sinônimos e regionalismos que entram no app. */
    val terms: List<CatalogTerm>,
    /** Produto antigo → produto que o absorveu, para levar o histórico junto. */
    val replacements: Map<String, String>,
    /** `id` de todos os produtos antigos, reservados para sempre. */
    val retiredProductIds: Set<String>,
)

data class CatalogSection(val id: String, val name: String, val order: Int, val icon: CatalogIcon) {
    companion object {
        /** Seção dos produtos que o app não reconhece. */
        const val OTHER_ID = "other"
    }
}

data class CatalogProduct(
    val id: String,
    val name: String,
    val sectionId: String,
    /** Posição dentro da seção. */
    val order: Int,
    /** `null` usa o ícone da seção. */
    val icon: CatalogIcon?,
    /** `null` quando não há ícone próprio. */
    val iconFit: IconFit?,
)

sealed interface CatalogIcon {
    /** Pasta do ícone em `microsoft/fluentui-emoji`, variação Color. */
    data class Fluent(val name: String) : CatalogIcon

    /** Ícone do complemento próprio. */
    data class Own(val id: String) : CatalogIcon
}

enum class IconFit {
    /** O ícone parece o produto. */
    EXACT,

    /** Aproximado, ex.: baguete no lugar do pãozinho. */
    APPROX,
}

data class CatalogTerm(
    /** Em minúsculas e com acento, como na planilha. */
    val term: String,
    val productId: String,
    /** Vazio = Brasil todo. */
    val regions: Set<Region>,
)

enum class Region {
    NORTH,
    NORTHEAST,
    CENTRAL_SOUTHEAST,
    SOUTH,
}
