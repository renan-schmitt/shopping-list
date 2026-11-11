package br.com.mentealerta.listadecompras.buildlogic.catalog

import org.gradle.api.Plugin
import org.gradle.api.Project

/** Pasta dos SVG do Fluent baixados, em `catalog/`. */
internal const val FLUENT_SVG_DIR = "fluent-svg"

/** Pasta dos SVG do complemento próprio, em `catalog/`. */
internal const val OWN_ICONS_DIR = "own-icons"

/**
 * Registra `generateCatalog`, que valida os CSV de `catalog/` e gera o catálogo embarcado em
 * `build/generated/catalog/`. Quem aplica o plugin liga [GenerateCatalogTask.outputDir] às fontes.
 * Registra também `downloadFluentIcons`, rodada à mão quando o catálogo ganha ícone Fluent novo.
 */
class CatalogPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val catalogDir = project.rootProject.layout.projectDirectory.dir("catalog")
        project.tasks.register("generateCatalog", GenerateCatalogTask::class.java) {
            group = "catalog"
            description = "Valida a planilha do catálogo e gera o catálogo embarcado"
            sectionsFile.set(catalogDir.file("sections.csv"))
            productsFile.set(catalogDir.file("products.csv"))
            termsFile.set(catalogDir.file("terms.csv"))
            fluentIconsFile.set(catalogDir.file("fluent-icons.txt"))
            ownIcons.from(project.fileTree(catalogDir.dir(OWN_ICONS_DIR)))
            fluentSvgs.from(project.fileTree(catalogDir.dir(FLUENT_SVG_DIR)))
            outputDir.set(project.layout.buildDirectory.dir("generated/catalog/kotlin"))
        }
        project.tasks.register("downloadFluentIcons", DownloadFluentIconsTask::class.java) {
            group = "catalog"
            description = "Baixa os SVG do Fluent Emoji usados no catálogo e atualiza fluent-icons.txt"
            sectionsFile.set(catalogDir.file("sections.csv"))
            productsFile.set(catalogDir.file("products.csv"))
            fluentIconsFile.set(catalogDir.file("fluent-icons.txt"))
            svgDir.set(catalogDir.dir(FLUENT_SVG_DIR))
        }
    }
}
