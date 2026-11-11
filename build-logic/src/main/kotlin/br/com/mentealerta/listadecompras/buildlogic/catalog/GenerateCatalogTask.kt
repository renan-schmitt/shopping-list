package br.com.mentealerta.listadecompras.buildlogic.catalog

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/** Valida a planilha mestre (os CSV de `catalog/`) e gera o código Kotlin do catálogo embarcado. */
@CacheableTask
abstract class GenerateCatalogTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sectionsFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val productsFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val termsFile: RegularFileProperty

    /** Lista das pastas do Fluent Emoji com SVG Color (`catalog/fluent-icons.txt`). */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val fluentIconsFile: RegularFileProperty

    /** SVG do complemento próprio; o nome do arquivo sem extensão é o `id` usado em `own:<id>`. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val ownIcons: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val source = CatalogSource(
            sectionsCsv = sectionsFile.get().asFile.readText(),
            productsCsv = productsFile.get().asFile.readText(),
            termsCsv = termsFile.get().asFile.readText(),
            fluentIcons = fluentIconsFile.get().asFile.readLines()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") }
                .toSet(),
            ownIcons = ownIcons.files.filter { it.extension == "svg" }.map { it.nameWithoutExtension }.toSet(),
        )
        when (val result = validateCatalog(source)) {
            is ValidationResult.Invalid -> throw GradleException(
                "Planilha do catálogo com ${result.errors.size} erro(s):\n" +
                    result.errors.joinToString("\n") { "  - catalog/$it" },
            )
            is ValidationResult.Valid -> {
                val dir = outputDir.get().asFile
                dir.deleteRecursively()
                val file = dir.resolve(CATALOG_PACKAGE.replace('.', '/')).resolve("EmbeddedCatalog.kt")
                file.parentFile.mkdirs()
                file.writeText(generateCatalogCode(result.catalog))
            }
        }
    }
}
