package br.com.mentealerta.listadecompras.buildlogic.catalog

import java.util.Locale

/** Conteúdo dos arquivos da planilha mestre (`catalog/`) e o que existe de ícones. */
data class CatalogSource(
    val sectionsCsv: String,
    val productsCsv: String,
    val termsCsv: String,
    /** Pastas do Fluent Emoji que têm SVG da variação Color. */
    val fluentIcons: Set<String>,
    /** `id` dos ícones do complemento próprio. */
    val ownIcons: Set<String>,
)

data class Section(val id: String, val name: String, val order: Int, val fluentIcon: String)

data class Product(
    val id: String,
    val name: String,
    val sectionId: String,
    val order: Int,
    /** `fluent:<pasta>` ou `own:<id>`; `null` usa o ícone da seção. */
    val icon: String?,
    /** `exact` ou `approx`; `null` quando não há ícone. */
    val iconFit: String?,
)

data class Term(val term: String, val productId: String, val regions: List<String>)

/** Catálogo já validado, só com o que vai para o app. */
data class Catalog(
    /** Na ordem do mercado. */
    val sections: List<Section>,
    /** Só os ativos, na ordem da seção e depois na ordem dentro da seção. */
    val products: List<Product>,
    /** Só os termos que entram no app (ver `catalog/README.md`), na ordem do arquivo. */
    val terms: List<Term>,
    /** Produto antigo → produto que o absorveu. */
    val replacements: Map<String, String>,
    /** Todos os produtos antigos, com ou sem substituto. */
    val retiredProductIds: Set<String>,
)

sealed interface ValidationResult {
    data class Valid(val catalog: Catalog) : ValidationResult

    data class Invalid(val errors: List<String>) : ValidationResult
}

private const val SECTIONS = "sections.csv"
private const val PRODUCTS = "products.csv"
private const val TERMS = "terms.csv"
private const val OTHER_SECTION = "other"

private val SECTION_COLUMNS = listOf("id", "name", "order", "fluent_icon")
private val PRODUCT_COLUMNS =
    listOf("id", "name", "section", "order", "icon", "icon_fit", "status", "replaced_by", "notes")
private val TERM_COLUMNS = listOf("product_id", "term", "regions", "status", "ambiguous", "evidence", "notes")

private val ID_PATTERN = Regex("[a-z][a-z0-9_]*")
private val ICON_FITS = setOf("exact", "approx")
private val PRODUCT_STATUSES = setOf("active", "retired")
private val TERM_STATUSES = setOf("to_confirm", "confirmed", "rejected")
private val AMBIGUOUS_VALUES = setOf("yes", "no")
private val REGIONS = setOf("north", "northeast", "central_southeast", "south")
private val PT_BR = Locale.forLanguageTag("pt-BR")

/**
 * Valida a planilha mestre com as regras de `catalog/README.md` e devolve o catálogo que vai
 * para o app. Junta todos os erros em vez de parar no primeiro.
 */
fun validateCatalog(source: CatalogSource): ValidationResult {
    val errors = mutableListOf<String>()
    val sectionRows = parseCsv(SECTIONS, source.sectionsCsv, SECTION_COLUMNS).also { errors += it.errors }.rows
    val productRows = parseCsv(PRODUCTS, source.productsCsv, PRODUCT_COLUMNS).also { errors += it.errors }.rows
    val termRows = parseCsv(TERMS, source.termsCsv, TERM_COLUMNS).also { errors += it.errors }.rows

    val sections = validateSections(sectionRows, source.fluentIcons, errors)
    val products = validateProducts(productRows, sections, source, errors)
    val terms = validateTerms(termRows, products, errors)

    if (errors.isNotEmpty()) return ValidationResult.Invalid(errors)

    val sectionOrder = sections.associate { it.id to it.order }
    val active = products.filter { it.status == "active" }
    val retired = products.filter { it.status == "retired" }
    return ValidationResult.Valid(
        Catalog(
            sections = sections.sortedBy { it.order },
            products = active
                .sortedWith(compareBy({ sectionOrder.getValue(it.product.sectionId) }, { it.product.order }))
                .map { it.product },
            terms = terms,
            replacements = retired
                .filter { it.replacedBy != null }
                .associate { it.product.id to it.replacedBy!! },
            retiredProductIds = retired.map { it.product.id }.toSet(),
        ),
    )
}

private fun validateSections(rows: List<CsvRow>, fluentIcons: Set<String>, errors: MutableList<String>): List<Section> {
    val seenIds = mutableSetOf<String>()
    val seenOrders = mutableSetOf<Int>()
    val sections = rows.mapNotNull { row ->
        val where = "$SECTIONS:${row.line}"
        val id = row["id"]
        val order = row["order"].toIntOrNull()
        val icon = row["fluent_icon"]
        checkId(id, where, errors)
        if (!seenIds.add(id)) errors += "$where: seção \"$id\" repetida"
        if (row["name"].isEmpty()) errors += "$where: seção \"$id\" sem nome"
        if (order == null) {
            errors += "$where: order \"${row["order"]}\" não é um número"
        } else if (!seenOrders.add(order)) {
            errors += "$where: order $order repetida em outra seção"
        }
        if (icon !in fluentIcons) errors += "$where: ícone Fluent \"$icon\" não existe na variação Color"
        order?.let { Section(id, row["name"], it, icon) }
    }
    val other = sections.find { it.id == OTHER_SECTION }
    if (other == null) {
        errors += "$SECTIONS: falta a seção \"$OTHER_SECTION\" (Outros)"
    } else if (sections.any { it.order > other.order }) {
        errors += "$SECTIONS: a seção \"$OTHER_SECTION\" (Outros) precisa ser a última"
    }
    return sections
}

private data class ProductRow(val line: Int, val product: Product, val status: String, val replacedBy: String?)

private fun validateProducts(
    rows: List<CsvRow>,
    sections: List<Section>,
    source: CatalogSource,
    errors: MutableList<String>,
): List<ProductRow> {
    val sectionIds = sections.map { it.id }.toSet()
    val seenIds = mutableSetOf<String>()
    val seenOrders = mutableMapOf<Pair<String, Int>, String>()
    val products = rows.mapNotNull { row ->
        val where = "$PRODUCTS:${row.line}"
        val id = row["id"]
        val section = row["section"]
        val status = row["status"]
        checkId(id, where, errors)
        if (!seenIds.add(id)) errors += "$where: produto \"$id\" repetido"
        if (row["name"].isEmpty()) errors += "$where: produto \"$id\" sem nome"
        if (section !in sectionIds) errors += "$where: seção \"$section\" não existe em $SECTIONS"
        val order = row["order"].toIntOrNull()
        if (order == null) {
            errors += "$where: order \"${row["order"]}\" não é um número"
        } else if (status == "active") {
            seenOrders.put(section to order, id)?.let {
                errors += "$where: order $order já usada por \"$it\" na seção \"$section\""
            }
        }
        val icon = row["icon"].ifEmpty { null }
        val iconFit = row["icon_fit"].ifEmpty { null }
        checkProductIcon(icon, iconFit, where, source, errors)
        if (status !in PRODUCT_STATUSES) errors += "$where: status \"$status\" deve ser active ou retired"
        order?.let {
            val product = Product(id, row["name"], section, it, icon, iconFit)
            ProductRow(row.line, product, status, row["replaced_by"].ifEmpty { null })
        }
    }
    checkReplacements(products, errors)
    checkDuplicateNames(products, errors)
    return products
}

private fun checkProductIcon(
    icon: String?,
    iconFit: String?,
    where: String,
    source: CatalogSource,
    errors: MutableList<String>,
) {
    if (icon != null) checkIcon(icon, where, source, errors)
    when {
        icon == null && iconFit != null -> errors += "$where: icon_fit preenchido sem ícone"
        icon != null && iconFit !in ICON_FITS -> errors += "$where: icon_fit deve ser exact ou approx"
    }
}

private fun checkReplacements(products: List<ProductRow>, errors: MutableList<String>) {
    val byId = products.associateBy { it.product.id }
    for (row in products) {
        val where = "$PRODUCTS:${row.line}"
        val replacedBy = row.replacedBy ?: continue
        when {
            row.status != "retired" -> errors += "$where: replaced_by só vale para produto retired"
            byId[replacedBy]?.status != "active" ->
                errors += "$where: replaced_by \"$replacedBy\" não é um produto active"
        }
    }
}

private fun checkDuplicateNames(products: List<ProductRow>, errors: MutableList<String>) {
    val seenNames = mutableMapOf<String, String>()
    for (row in products.filter { it.status == "active" }) {
        seenNames.put(row.product.name.lowercase(PT_BR), row.product.id)?.let {
            errors += "$PRODUCTS:${row.line}: nome \"${row.product.name}\" já usado por \"$it\""
        }
    }
}

private fun checkIcon(icon: String, where: String, source: CatalogSource, errors: MutableList<String>) {
    when {
        icon.startsWith("fluent:") -> if (icon.removePrefix("fluent:") !in source.fluentIcons) {
            errors += "$where: ícone Fluent \"${icon.removePrefix("fluent:")}\" não existe na variação Color"
        }
        icon.startsWith("own:") -> if (icon.removePrefix("own:") !in source.ownIcons) {
            errors += "$where: ícone próprio \"${icon.removePrefix("own:")}\" não existe"
        }
        else -> errors += "$where: ícone \"$icon\" deve começar com fluent: ou own:"
    }
}

private data class TermRow(val line: Int, val term: Term, val entersApp: Boolean)

private fun validateTerms(rows: List<CsvRow>, products: List<ProductRow>, errors: MutableList<String>): List<Term> {
    val statusById = products.associate { it.product.id to it.status }
    val activeNames = products.filter { it.status == "active" }.map { it.product.name.lowercase(PT_BR) }.toSet()
    val termRows = rows.map { row ->
        val where = "$TERMS:${row.line}"
        val productId = row["product_id"]
        val term = row["term"]
        val regions = row["regions"].split(';').map { it.trim() }.filter { it.isNotEmpty() }
        if (productId !in statusById) errors += "$where: produto \"$productId\" não existe em $PRODUCTS"
        checkTermText(term, activeNames, where, errors)
        regions.filter { it !in REGIONS }.forEach { errors += "$where: região \"$it\" não existe" }
        val entersApp = checkTermStatus(row, regions, where, errors)
        if (entersApp && statusById[productId] == "retired") {
            errors += "$where: termo \"$term\" entra no app mas aponta para produto retired"
        }
        TermRow(row.line, Term(term, productId, regions), entersApp)
    }
    checkDuplicateTerms(termRows, errors)
    return termRows.filter { it.entersApp }.map { it.term }
}

private fun checkTermText(term: String, activeNames: Set<String>, where: String, errors: MutableList<String>) {
    if (term.isEmpty()) errors += "$where: termo vazio"
    if (term != term.lowercase(PT_BR)) errors += "$where: termo \"$term\" deve estar em minúsculas"
    if (term.lowercase(PT_BR) in activeNames) errors += "$where: termo \"$term\" repete o nome de um produto"
}

/** Confere `status`, `ambiguous` e `evidence`; devolve se o termo entra no app. */
private fun checkTermStatus(row: CsvRow, regions: List<String>, where: String, errors: MutableList<String>): Boolean {
    val status = row["status"]
    val ambiguous = row["ambiguous"]
    if (status !in TERM_STATUSES) errors += "$where: status \"$status\" deve ser to_confirm, confirmed ou rejected"
    if (ambiguous !in AMBIGUOUS_VALUES) errors += "$where: ambiguous \"$ambiguous\" deve ser yes ou no"
    if (status == "confirmed" && regions.isNotEmpty() && row["evidence"].isEmpty()) {
        errors += "$where: regionalismo confirmed precisa de evidence"
    }
    return status == "confirmed" || (status == "to_confirm" && ambiguous == "no")
}

private fun checkDuplicateTerms(termRows: List<TermRow>, errors: MutableList<String>) {
    val seenPairs = mutableSetOf<Pair<String, String>>()
    val seenEntering = mutableMapOf<String, TermRow>()
    for (row in termRows) {
        val where = "$TERMS:${row.line}"
        if (!seenPairs.add(row.term.term to row.term.productId)) {
            errors += "$where: termo \"${row.term.term}\" repetido para \"${row.term.productId}\""
        }
        if (row.entersApp) {
            seenEntering.put(row.term.term, row)?.let {
                errors += "$where: termo \"${row.term.term}\" leva a \"${row.term.productId}\" e a " +
                    "\"${it.term.productId}\" ($TERMS:${it.line}); só um pode entrar no app"
            }
        }
    }
}

private fun checkId(id: String, where: String, errors: MutableList<String>) {
    if (!ID_PATTERN.matches(id)) errors += "$where: id \"$id\" deve ter só minúsculas, números e _"
}
