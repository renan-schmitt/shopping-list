package br.com.mentealerta.listadecompras.buildlogic.catalog

/** Caminho de `docs/prototipos/` até `catalog/`, de onde a página carrega os SVG. */
private const val CATALOG_FROM_PAGE = "../../catalog"
private val LIST_SIZES = listOf(24, 32, 40, 56)
private const val SIZE_SAMPLES = 6

/**
 * Gera `docs/prototipos/icones.html`: a cobertura de ícones do catálogo, a fila do complemento próprio
 * e todos os produtos por seção. Usa os SVG de `catalog/`, então funciona sem internet. A mesma
 * entrada gera sempre a mesma página.
 */
fun generateIconsPage(catalog: Catalog, queue: List<QueuedIcon>): String = buildString {
    val sections = catalog.sections.associateBy { it.id }
    val productsById = catalog.products.associateBy { it.id }
    val queuedBy = queue.flatMap { icon -> icon.productIds.map { it to icon } }.toMap()
    val coverage = iconCoverage(catalog.products)

    append(PAGE_HEAD)
    appendLine("<h1>Ícones do catálogo</h1>")
    appendLine(
        "<p>Cobertura de ícones dos produtos ativos do catálogo: ícones do " +
            "<a href=\"https://github.com/microsoft/fluentui-emoji\">Microsoft Fluent Emoji</a> (licença MIT), " +
            "variação <b>Color</b>, e o complemento próprio no mesmo estilo. Produto sem ícone usa o da seção. " +
            "O ícone <b>sempre acompanha o nome</b>, nunca o substitui.</p>",
    )
    appendLine(
        "<p class=\"nota\">Página gerada por <code>./gradlew :shared:generateIconsPage</code> a partir de " +
            "<code>catalog/</code>; não editar à mão.</p>",
    )
    appendSummary(coverage, queue)
    appendSectionTable(catalog)
    appendSizes(catalog, sections)
    appendQueue(queue, productsById, sections)
    catalog.sections.forEach { section ->
        val products = catalog.products.filter { it.sectionId == section.id }
        if (products.isNotEmpty()) appendSectionGrid(section, products, queuedBy)
    }
    appendLine("</main>\n</body>\n</html>")
}

private fun StringBuilder.appendSummary(coverage: IconCoverage, queue: List<QueuedIcon>) {
    appendLine("<div class=\"resumo\">")
    appendNumber(coverage.total.toString(), "produtos ativos")
    appendNumber(coverage.exact.toString(), "ícone fiel (${coverage.percent(coverage.exact)}%)")
    appendNumber(coverage.approx.toString(), "ícone aproximado (${coverage.percent(coverage.approx)}%)")
    appendNumber(coverage.none.toString(), "sem ícone (${coverage.percent(coverage.none)}%)")
    appendNumber(queue.size.toString(), "desenhos na fila")
    appendLine("</div>")
    appendLine("<div class=\"legenda\">")
    appendLine("  <span class=\"selo bom\">fiel</span> parece o produto na prateleira")
    appendLine("  <span class=\"selo aprox\">aproximado</span> serve, mas não é como o produto aparece")
    appendLine("  <span class=\"selo sem\">sem ícone</span> usa o ícone da seção")
    appendLine("</div>")
}

private fun StringBuilder.appendNumber(value: String, label: String) {
    appendLine("  <div class=\"num\"><b>$value</b>${label.html()}</div>")
}

private fun StringBuilder.appendSectionTable(catalog: Catalog) {
    appendLine("<h2>Por seção</h2>")
    appendLine("<table>")
    appendLine("<tr><th>Seção</th><th>Produtos</th><th>Fiel</th><th>Aproximado</th><th>Sem ícone</th></tr>")
    catalog.sections.forEach { section ->
        val c = iconCoverage(catalog.products.filter { it.sectionId == section.id })
        if (c.total > 0) {
            appendLine(
                "<tr><td>${section.name.html()}</td><td>${c.total}</td><td>${c.exact}</td>" +
                    "<td>${c.approx}</td><td>${c.none}</td></tr>",
            )
        }
    }
    val c = iconCoverage(catalog.products)
    appendLine(
        "<tr class=\"total\"><td>Total</td><td>${c.total}</td><td>${c.exact}</td>" +
            "<td>${c.approx}</td><td>${c.none}</td></tr>",
    )
    appendLine("</table>")
}

private fun StringBuilder.appendSizes(catalog: Catalog, sections: Map<String, Section>) {
    val samples = catalog.products.filter { it.iconFit == "exact" }.distinctBy { it.sectionId }.take(SIZE_SAMPLES)
    appendLine("<h2>No tamanho da lista</h2>")
    appendLine("<p>Na lista o ícone fica em torno de 32–40dp, no fundo claro e no escuro.</p>")
    listOf("claro", "escuro").forEach { theme ->
        LIST_SIZES.forEach { size ->
            append("<div class=\"faixa $theme\"><span>${size}px</span>")
            samples.forEach {
                append("<img src=\"${iconSrc(it, sections)}\" width=\"$size\" height=\"$size\" alt=\"\">")
            }
            appendLine("</div>")
        }
    }
}

private fun StringBuilder.appendQueue(
    queue: List<QueuedIcon>,
    productsById: Map<String, Product>,
    sections: Map<String, Section>,
) {
    appendLine("<h2>Fila do complemento próprio</h2>")
    appendLine(
        "<p>Os desenhos que faltam, de <code>catalog/own-icons-queue.csv</code>. Cada desenho cobre um ou mais " +
            "produtos sem ícone. Prioridade 1: compra de toda semana ou todo mês; 2: compra comum; " +
            "3: ocasional ou regional. A imagem é o ícone da seção, usado até o desenho ficar pronto.</p>",
    )
    queue.groupBy { it.priority }.toSortedMap().forEach { (priority, icons) ->
        val productCount = icons.sumOf { it.productIds.size }
        appendLine("<h3>Prioridade $priority: ${icons.size} desenhos, $productCount produtos</h3>")
        appendLine("<div class=\"grade\">")
        icons.forEach { icon ->
            val products = icon.productIds.map { productsById.getValue(it) }
            val names = products.joinToString(", ") { it.name }
            append(
                "<div class=\"card lacuna\"><img src=\"${iconSrc(
                    products.first(),
                    sections,
                )}\" alt=\"\" loading=\"lazy\">",
            )
            append("<span class=\"pt\">${icon.name.html()}</span>")
            append("<span class=\"en\">${names.html()}</span>")
            if (icon.notes.isNotEmpty()) append("<span class=\"obs\">${icon.notes.html()}</span>")
            appendLine("<span class=\"selo proprio\">prioridade $priority</span></div>")
        }
        appendLine("</div>")
    }
}

private fun StringBuilder.appendSectionGrid(
    section: Section,
    products: List<Product>,
    queuedBy: Map<String, QueuedIcon>,
) {
    appendLine("<h2><img src=\"${fluentSrc(section.fluentIcon)}\" alt=\"\">${section.name.html()}</h2>")
    appendLine("<div class=\"grade\">")
    products.forEach { product ->
        val (seal, label) = when (product.iconFit) {
            "exact" -> "bom" to "fiel"
            "approx" -> "aprox" to "aproximado"
            else -> "sem" to "sem ícone"
        }
        val source = when {
            product.icon == null -> "ícone da seção"
            product.icon.startsWith(OWN_PREFIX) -> "ícone próprio"
            else -> product.icon.removePrefix(FLUENT_PREFIX)
        }
        val src = if (product.icon == null) fluentSrc(section.fluentIcon) else iconSrc(product, emptyMap())
        val lacuna = if (product.icon == null) " lacuna" else ""
        append("<div class=\"card$lacuna\"><img src=\"$src\" alt=\"\" loading=\"lazy\">")
        append("<span class=\"pt\">${product.name.html()}</span><span class=\"en\">${source.html()}</span>")
        append("<span class=\"selo $seal\">$label</span>")
        queuedBy[product.id]?.let { append("<span class=\"selo proprio\">fila: prioridade ${it.priority}</span>") }
        appendLine("</div>")
    }
    appendLine("</div>")
}

/** SVG do ícone do produto ou, sem ícone, o da seção. */
private fun iconSrc(product: Product, sections: Map<String, Section>): String {
    val icon = product.icon
    return when {
        icon == null -> fluentSrc(sections.getValue(product.sectionId).fluentIcon)
        icon.startsWith(
            OWN_PREFIX,
        ) -> "$CATALOG_FROM_PAGE/$OWN_ICONS_DIR/${encodePath(icon.removePrefix(OWN_PREFIX))}.svg"
        else -> fluentSrc(icon.removePrefix(FLUENT_PREFIX))
    }
}

private fun fluentSrc(folder: String): String = "$CATALOG_FROM_PAGE/$FLUENT_SVG_DIR/${encodePath(folder)}.svg"

private fun String.html(): String =
    replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

private val PAGE_HEAD = """
    |<!doctype html>
    |<html lang="pt-BR">
    |<head>
    |<meta charset="utf-8">
    |<meta name="viewport" content="width=device-width, initial-scale=1">
    |<title>Mente Alerta — ícones do catálogo</title>
    |<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Atkinson+Hyperlegible:wght@400;700&family=Bangers&display=swap">
    |<style>
    |  :root { --papel:#FBF3DC; --tinta:#141414; --verde:#2E6B3A; --cinza:#5E5A52; --borda:#E2D5B3; }
    |  * { box-sizing:border-box; }
    |  body { margin:0; padding:32px 16px 64px; background:var(--papel); color:var(--tinta); font-family:'Atkinson Hyperlegible',system-ui,sans-serif; }
    |  main { max-width:1200px; margin:0 auto; }
    |  h1, h2, h3 { font-family:Bangers,Impact,sans-serif; font-weight:400; letter-spacing:.04em; }
    |  h1 { font-size:44px; margin:0 0 8px; }
    |  h2 { font-size:30px; margin:40px 0 12px; display:flex; align-items:center; gap:12px; }
    |  h2 img { width:40px; height:40px; }
    |  h3 { font-size:24px; margin:28px 0 10px; }
    |  p { font-size:18px; line-height:1.5; max-width:820px; }
    |  p.nota { font-size:15px; color:var(--cinza); }
    |  a { color:var(--verde); font-weight:700; }
    |  table { border-collapse:collapse; background:#fff; border:3px solid var(--tinta); font-size:17px; }
    |  th, td { padding:6px 14px; border-bottom:1px solid var(--borda); text-align:right; }
    |  th:first-child, td:first-child { text-align:left; }
    |  tr.total td { font-weight:700; border-top:2px solid var(--tinta); }
    |  .resumo { display:flex; flex-wrap:wrap; gap:16px; margin:20px 0; }
    |  .num { background:#fff; border:3px solid var(--tinta); box-shadow:4px 4px 0 var(--tinta); border-radius:14px; padding:12px 18px; min-width:180px; }
    |  .num b { display:block; font-family:Bangers,Impact,sans-serif; font-weight:400; font-size:40px; letter-spacing:.04em; }
    |  .grade { display:grid; grid-template-columns:repeat(auto-fill,minmax(150px,1fr)); gap:14px; }
    |  .card { background:#fff; border:3px solid var(--tinta); border-radius:14px; box-shadow:3px 3px 0 var(--tinta); padding:14px 10px 10px; text-align:center; display:flex; flex-direction:column; align-items:center; gap:6px; }
    |  .card img { width:72px; height:72px; }
    |  .card .pt { font-weight:700; font-size:18px; line-height:1.2; }
    |  .card .en { font-size:13px; color:var(--cinza); }
    |  .card .obs { font-size:13px; font-style:italic; color:var(--cinza); }
    |  .card.lacuna { background:transparent; border-style:dashed; box-shadow:none; }
    |  .card.lacuna img { opacity:.35; filter:grayscale(1); }
    |  .selo { font-size:12px; font-weight:700; padding:2px 8px; border-radius:999px; border:2px solid currentColor; }
    |  .selo.bom { color:var(--verde); }
    |  .selo.aprox { color:#8A5A00; }
    |  .selo.sem { color:#A8321F; }
    |  .selo.proprio { color:#1F5FA8; }
    |  .legenda { display:flex; flex-wrap:wrap; gap:18px; font-size:16px; align-items:center; }
    |  .faixa { display:flex; flex-wrap:wrap; gap:10px; align-items:center; padding:14px; border-radius:14px; border:3px solid var(--tinta); margin:8px 0; }
    |  .faixa.claro { background:#fff; }
    |  .faixa.escuro { background:#1C1B19; color:#F2EEE6; }
    |  .faixa span { font-size:14px; font-weight:700; min-width:64px; }
    |</style>
    |</head>
    |<body>
    |<main>
    |
""".trimMargin()
