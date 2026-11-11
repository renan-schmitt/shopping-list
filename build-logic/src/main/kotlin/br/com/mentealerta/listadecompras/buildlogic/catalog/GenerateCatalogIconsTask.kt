package br.com.mentealerta.listadecompras.buildlogic.catalog

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Converte os SVG dos ícones do catálogo (Fluent e complemento próprio) em VectorDrawable e gera o
 * Kotlin que liga cada ícone da planilha ao seu `R.drawable` (`CatalogIconResources`).
 */
@CacheableTask
abstract class GenerateCatalogIconsTask : DefaultTask() {
    /** SVG de `catalog/fluent-svg/`; o nome do arquivo sem extensão é a pasta do Fluent. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val fluentSvgs: ConfigurableFileCollection

    /** SVG de `catalog/own-icons/`; o nome do arquivo sem extensão é o `id` usado em `own:<id>`. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val ownSvgs: ConfigurableFileCollection

    /** Pacote do Kotlin gerado. */
    @get:Input
    abstract val packageName: Property<String>

    /** Pacote do `R` do app (o `namespace`). */
    @get:Input
    abstract val rPackage: Property<String>

    /** Pasta de recursos gerada (com `drawable/` dentro). */
    @get:OutputDirectory
    abstract val resDir: DirectoryProperty

    @get:OutputDirectory
    abstract val kotlinDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val fluent = fluentSvgs.svgFiles().map {
            IconSource(it.nameWithoutExtension, fluentResourceName(it.nameWithoutExtension), it)
        }
        val own = ownSvgs.svgFiles().map {
            IconSource(it.nameWithoutExtension, ownResourceName(it.nameWithoutExtension), it)
        }
        checkResourceNames(fluent + own)

        val drawableDir = resDir.get().asFile.apply { deleteRecursively() }.resolve("drawable").apply { mkdirs() }
        for (icon in fluent + own) {
            val converted = convertSvg(icon.svg)
            converted.warnings.forEach { logger.warn("${icon.svg.name}: $it") }
            drawableDir.resolve("${icon.resourceName}.xml").writeText(converted.vectorXml)
        }

        val kotlin = kotlinDir.get().asFile.apply { deleteRecursively() }
        val file = kotlin.resolve(packageName.get().replace('.', '/')).resolve("CatalogIconResources.kt")
        file.parentFile.mkdirs()
        file.writeText(generateIconResourcesCode(packageName.get(), rPackage.get(), fluent, own))
    }

    private fun ConfigurableFileCollection.svgFiles() = files.filter { it.extension == "svg" }.sortedBy { it.name }
}
