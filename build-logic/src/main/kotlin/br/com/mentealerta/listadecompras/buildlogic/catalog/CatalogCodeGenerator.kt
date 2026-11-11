package br.com.mentealerta.listadecompras.buildlogic.catalog

/** Pacote do código gerado e dos modelos do catálogo no `shared`. */
const val CATALOG_PACKAGE = "br.com.mentealerta.listadecompras.catalog"

/**
 * Itens por função gerada. Uma função só com o catálogo inteiro (~1.500 produtos no futuro)
 * passaria do limite de 64 KB de bytecode por método da JVM.
 */
private const val CHUNK_SIZE = 100

/**
 * Gera o código Kotlin do catálogo embarcado (`EmbeddedCatalog`), usando os modelos de
 * `shared/.../catalog/CatalogModel.kt`. A saída depende só do [catalog]: mesma planilha, mesmo arquivo.
 */
fun generateCatalogCode(catalog: Catalog): String = buildString {
    appendLine("// Gerado por :build-logic a partir dos CSV de catalog/. Não editar à mão.")
    appendLine("package $CATALOG_PACKAGE")
    appendLine()
    appendLine("/** Catálogo embarcado, gerado da planilha mestre em `catalog/`. */")
    appendLine("internal object EmbeddedCatalog {")
    appendLine("    val catalog: Catalog by lazy {")
    appendLine("        Catalog(")
    appendLine("            sections = sections(),")
    appendLine("            products = ${chunkCalls("products", catalog.products.size)},")
    appendLine("            terms = ${chunkCalls("terms", catalog.terms.size)},")
    appendLine("            replacements = ${mapOf(catalog.replacements)},")
    appendLine("            retiredProductIds = ${setOf(catalog.retiredProductIds.sorted())},")
    appendLine("        )")
    appendLine("    }")
    appendFunction("sections", "CatalogSection", catalog.sections.map(::section))
    catalog.products.chunked(CHUNK_SIZE).forEachIndexed { i, chunk ->
        appendFunction("products$i", "CatalogProduct", chunk.map(::product))
    }
    catalog.terms.chunked(CHUNK_SIZE).forEachIndexed { i, chunk ->
        appendFunction("terms$i", "CatalogTerm", chunk.map(::term))
    }
    appendLine("}")
}

private fun chunkCalls(name: String, size: Int): String {
    val chunks = (size + CHUNK_SIZE - 1) / CHUNK_SIZE
    return if (chunks == 0) "emptyList()" else (0 until chunks).joinToString(" + ") { "$name$it()" }
}

private fun StringBuilder.appendFunction(name: String, type: String, items: List<String>) {
    appendLine()
    appendLine("    private fun $name(): List<$type> = listOf(")
    items.forEach { appendLine("        $it,") }
    appendLine("    )")
}

private fun section(s: Section) =
    "CatalogSection(${str(s.id)}, ${str(s.name)}, ${s.order}, CatalogIcon.Fluent(${str(s.fluentIcon)}))"

private fun product(p: Product) = "CatalogProduct(${str(p.id)}, ${str(p.name)}, ${str(p.sectionId)}, ${p.order}, " +
    "${icon(p.icon)}, ${iconFit(p.iconFit)})"

private fun term(t: Term): String {
    val regions = t.regions.joinToString(", ") { "Region.${it.uppercase()}" }
    return "CatalogTerm(${str(t.term)}, ${str(t.productId)}, setOf($regions))"
}

private fun icon(icon: String?) = when {
    icon == null -> "null"
    icon.startsWith("fluent:") -> "CatalogIcon.Fluent(${str(icon.removePrefix("fluent:"))})"
    else -> "CatalogIcon.Own(${str(icon.removePrefix("own:"))})"
}

private fun iconFit(fit: String?) = fit?.let { "IconFit.${it.uppercase()}" } ?: "null"

private fun mapOf(map: Map<String, String>): String = if (map.isEmpty()) {
    "emptyMap()"
} else {
    map.entries.joinToString(", ", "mapOf(", ")") { "${str(it.key)} to ${str(it.value)}" }
}

private fun setOf(items: List<String>): String =
    if (items.isEmpty()) "emptySet()" else items.joinToString(", ", "setOf(", ")") { str(it) }

/** Literal de string Kotlin, com escape. */
internal fun str(value: String): String = buildString {
    append('"')
    for (c in value) {
        when (c) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '$' -> append("\\$")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> append(c)
        }
    }
    append('"')
}
