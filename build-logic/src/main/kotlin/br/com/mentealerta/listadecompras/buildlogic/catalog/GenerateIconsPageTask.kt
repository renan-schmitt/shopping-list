package br.com.mentealerta.listadecompras.buildlogic.catalog

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Confere a fila do complemento próprio (`catalog/own-icons-queue.csv`) e regrava
 * `docs/prototipos/icones.html` com a cobertura de ícones do catálogo. Roda à mão, nunca no build.
 */
abstract class GenerateIconsPageTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sectionsFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val productsFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val termsFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val fluentIconsFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val queueFile: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val ownIcons: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val fluentSvgs: ConfigurableFileCollection

    @get:OutputFile
    abstract val pageFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val ownIconNames = ownIcons.svgNames()
        val source = CatalogSource(
            sectionsCsv = sectionsFile.get().asFile.readText(),
            productsCsv = productsFile.get().asFile.readText(),
            termsCsv = termsFile.get().asFile.readText(),
            fluentIcons = parseFluentIconsFile(fluentIconsFile.get().asFile.readLines()),
            ownIcons = ownIconNames,
            fluentSvgs = fluentSvgs.svgNames(),
        )
        val catalog = when (val result = validateCatalog(source)) {
            is ValidationResult.Invalid -> throw GradleException(
                "Planilha do catálogo com erro; rode ./gradlew :shared:generateCatalog para ver a lista",
            )
            is ValidationResult.Valid -> result.catalog
        }
        val queue = parseIconQueue(queueFile.get().asFile.readText(), catalog, ownIconNames)
        if (queue.errors.isNotEmpty()) {
            throw GradleException(
                "Fila do complemento próprio com ${queue.errors.size} erro(s):\n" +
                    queue.errors.joinToString("\n") { "  - catalog/$it" },
            )
        }
        pageFile.get().asFile.writeText(generateIconsPage(catalog, queue.icons))
    }
}
