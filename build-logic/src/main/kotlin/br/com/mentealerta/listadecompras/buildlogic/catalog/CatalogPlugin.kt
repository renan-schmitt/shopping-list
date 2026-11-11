package br.com.mentealerta.listadecompras.buildlogic.catalog

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Registra `generateCatalog`, que valida os CSV de `catalog/` e gera o catálogo embarcado em
 * `build/generated/catalog/`. Quem aplica o plugin liga [GenerateCatalogTask.outputDir] às fontes.
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
            ownIcons.from(project.fileTree(catalogDir.dir("own-icons")))
            outputDir.set(project.layout.buildDirectory.dir("generated/catalog/kotlin"))
        }
    }
}
