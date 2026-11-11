package br.com.mentealerta.listadecompras.buildlogic.catalog

import com.android.ide.common.vectordrawable.Svg2Vector
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.Normalizer

private const val FLUENT_RESOURCE_PREFIX = "catalog_fluent_"
private const val OWN_RESOURCE_PREFIX = "catalog_own_"
private val OWN_ID = Regex("[a-z][a-z0-9_]*")
private val NON_ALPHANUMERIC = Regex("[^a-z0-9]+")
private val COMBINING_MARKS = Regex("\\p{M}+")

/**
 * Filtros (desfoque dos reflexos, sombra interna) não existem no VectorDrawable. O Svg2Vector descarta
 * só o filtro e mantém a forma e o degradê, e a diferença no ícone é mínima (#22).
 */
private val IGNORED_MESSAGE = Regex("<(filter|fe[A-Za-z]+)> is not supported")

/** Um ícone do catálogo pronto para converter: o que a planilha usa e o nome do recurso Android. */
data class IconSource(
    /** Como na planilha, sem o prefixo: a pasta do Fluent ou o `id` do ícone próprio. */
    val key: String,
    val resourceName: String,
    val svg: File,
)

/** Nome do recurso de um ícone Fluent: `Red apple` → `catalog_fluent_red_apple`. */
fun fluentResourceName(folder: String): String {
    val ascii = Normalizer.normalize(folder, Normalizer.Form.NFD).replace(COMBINING_MARKS, "")
    return FLUENT_RESOURCE_PREFIX + ascii.lowercase().replace(NON_ALPHANUMERIC, "_").trim('_')
}

/** Nome do recurso de um ícone próprio; o `id` já vem em minúsculas e `_`. */
fun ownResourceName(id: String): String {
    require(OWN_ID.matches(id)) { "Ícone próprio \"$id.svg\": o nome deve ter só minúsculas, números e _" }
    return OWN_RESOURCE_PREFIX + id
}

/** Erro se dois ícones caírem no mesmo nome de recurso. */
fun checkResourceNames(icons: List<IconSource>) {
    val clashes = icons.groupBy { it.resourceName }.filterValues { it.size > 1 }
    require(clashes.isEmpty()) {
        clashes.entries.joinToString("\n", "Ícones com o mesmo nome de recurso:\n") { (name, list) ->
            "  - $name: ${list.joinToString { it.svg.name }}"
        }
    }
}

/** Resultado da conversão: o XML do VectorDrawable e os avisos que importam. */
data class ConvertedIcon(val vectorXml: String, val warnings: List<String>)

/** Converte um SVG para VectorDrawable com o mesmo conversor do Vector Asset do Android Studio. */
fun convertSvg(svg: File): ConvertedIcon {
    val out = ByteArrayOutputStream()
    val log = Svg2Vector.parseSvgToXml(svg.toPath(), out).orEmpty()
    val xml = out.toString(Charsets.UTF_8)
    require("<vector" in xml) { "${svg.name}: conversão falhou\n$log" }
    val warnings = log.lines().map { it.trim() }.filter { it.isNotEmpty() && !IGNORED_MESSAGE.containsMatchIn(it) }
    return ConvertedIcon(xml, warnings)
}

/**
 * Código Kotlin que liga cada ícone da planilha ao seu `R.drawable`, no pacote [packageName] do app
 * cujo `R` fica em [rPackage]. A saída depende só das listas: mesmos ícones, mesmo arquivo.
 */
fun generateIconResourcesCode(
    packageName: String,
    rPackage: String,
    fluent: List<IconSource>,
    own: List<IconSource>,
): String = buildString {
    appendLine("// Gerado por :build-logic a partir dos SVG de catalog/. Não editar à mão.")
    appendLine("package $packageName")
    appendLine()
    appendLine("import $rPackage.R")
    appendLine()
    appendLine("/** Recursos drawable dos ícones do catálogo, convertidos dos SVG. */")
    appendLine("internal object CatalogIconResources {")
    appendMap("FLUENT", fluent)
    appendMap("OWN", own)
    appendLine()
    appendLine("    /** Pasta do Fluent → recurso; `null` se não houver SVG. */")
    appendLine("    fun fluent(name: String): Int? = FLUENT[name]")
    appendLine()
    appendLine("    /** `id` do ícone próprio → recurso; `null` se não houver SVG. */")
    appendLine("    fun own(id: String): Int? = OWN[id]")
    appendLine()
    appendLine("    /** Todos os ícones, como na planilha (`fluent:<pasta>`, `own:<id>`), para a amostra de debug. */")
    appendLine("    val all: List<Pair<String, Int>>")
    appendLine("        get() = FLUENT.map { (k, v) -> \"$FLUENT_PREFIX\$k\" to v } +")
    appendLine("            OWN.map { (k, v) -> \"$OWN_PREFIX\$k\" to v }")
    appendLine("}")
}

private fun StringBuilder.appendMap(name: String, icons: List<IconSource>) {
    val sorted = icons.sortedBy { it.key }
    if (sorted.isEmpty()) {
        appendLine("    private val $name: Map<String, Int> = emptyMap()")
        return
    }
    appendLine("    private val $name: Map<String, Int> = mapOf(")
    sorted.forEach { appendLine("        ${str(it.key)} to R.drawable.${it.resourceName},") }
    appendLine("    )")
}
