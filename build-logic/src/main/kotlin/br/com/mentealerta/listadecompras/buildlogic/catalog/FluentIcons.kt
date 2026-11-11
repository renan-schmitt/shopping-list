package br.com.mentealerta.listadecompras.buildlogic.catalog

/**
 * Commit fixo do [Fluent Emoji](https://github.com/microsoft/fluentui-emoji) de onde vêm os SVG.
 * Para atualizar: trocar aqui e rodar `./gradlew :shared:downloadFluentIcons`.
 */
const val FLUENT_COMMIT = "1ffb34c752ecf5d402f04cfb4b392c77f57c54bc"

private val COLOR_SVG = Regex("assets/([^/]+)/Color/[^/]+\\.svg")

/** Ícone com tom de pele: a variação Color fica dentro de cada tom; usamos o padrão. */
private val DEFAULT_TONE_COLOR_SVG = Regex("assets/([^/]+)/Default/Color/[^/]+\\.svg")

/**
 * Pasta do ícone → caminho do SVG da variação Color, a partir dos caminhos da árvore do repositório.
 * Ícone com tom de pele usa o tom `Default`.
 */
fun selectColorSvgPaths(treePaths: List<String>): Map<String, String> {
    val result = sortedMapOf<String, String>()
    for (path in treePaths) {
        COLOR_SVG.matchEntire(path)?.let { result.putIfAbsent(it.groupValues[1], path) }
    }
    for (path in treePaths) {
        DEFAULT_TONE_COLOR_SVG.matchEntire(path)?.let { result.putIfAbsent(it.groupValues[1], path) }
    }
    return result
}

/** Conteúdo de `catalog/fluent-icons.txt`: as pastas com SVG Color no [commit]. */
fun fluentIconsFileContent(commit: String, folders: Collection<String>): String = buildString {
    appendLine("# Pastas de microsoft/fluentui-emoji/assets com SVG da variação Color.")
    appendLine("# Commit: $commit")
    appendLine("# Gerado por ./gradlew :shared:downloadFluentIcons; não editar à mão.")
    folders.sorted().forEach { appendLine(it) }
}

/** Lê `catalog/fluent-icons.txt`, sem comentários e linhas vazias. */
fun parseFluentIconsFile(lines: List<String>): Set<String> =
    lines.map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }.toSet()

/** Ícones Fluent que vão para o app: os das seções e os dos produtos ativos. */
fun referencedFluentIcons(sectionsCsv: String, productsCsv: String): Set<String> {
    val sections = parseCsv("sections.csv", sectionsCsv, SECTION_COLUMNS).rows
        .map { it["fluent_icon"] }
    val products = parseCsv("products.csv", productsCsv, PRODUCT_COLUMNS).rows
        .filter { it["status"] == "active" && it["icon"].startsWith(FLUENT_PREFIX) }
        .map { it["icon"].removePrefix(FLUENT_PREFIX) }
    return (sections + products).filter { it.isNotEmpty() }.toSortedSet()
}

const val FLUENT_PREFIX = "fluent:"
const val OWN_PREFIX = "own:"
