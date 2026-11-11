package br.com.mentealerta.listadecompras.buildlogic.catalog

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Para o app Android: em cada variante, registra `generate<Variante>CatalogIcons`, que converte os SVG
 * de `catalog/fluent-svg/` e `catalog/own-icons/` em VectorDrawable e liga os recursos e o Kotlin
 * gerados às fontes da variante. Os SVG são conferidos contra a planilha pelo `generateCatalog`.
 */
class CatalogIconsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.withPlugin("com.android.application") {
            val catalogDir = project.rootProject.layout.projectDirectory.dir("catalog")
            val components = project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
            components.onVariants { variant ->
                val name = "generate${variant.name.replaceFirstChar { it.uppercase() }}CatalogIcons"
                val task = project.tasks.register(name, GenerateCatalogIconsTask::class.java) {
                    group = "catalog"
                    description = "Converte os ícones do catálogo em VectorDrawable (${variant.name})"
                    fluentSvgs.from(project.fileTree(catalogDir.dir(FLUENT_SVG_DIR)))
                    ownSvgs.from(project.fileTree(catalogDir.dir(OWN_ICONS_DIR)))
                    packageName.set(variant.namespace.map { "$it.ui.catalog" })
                    rPackage.set(variant.namespace)
                }
                variant.sources.res?.addGeneratedSourceDirectory(task, GenerateCatalogIconsTask::resDir)
                variant.sources.kotlin?.addGeneratedSourceDirectory(task, GenerateCatalogIconsTask::kotlinDir)
            }
        }
    }
}
