package br.com.mentealerta.listadecompras.buildlogic.catalog

import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

private const val TREE_URL = "https://api.github.com/repos/microsoft/fluentui-emoji/git/trees/%s?recursive=1"
private const val RAW_URL = "https://raw.githubusercontent.com/microsoft/fluentui-emoji/%s/%s"
private const val HTTP_OK = 200
private val TIMEOUT: Duration = Duration.ofSeconds(30)

/**
 * Baixa do [FLUENT_COMMIT] os SVG Color dos ícones Fluent usados no catálogo para `catalog/fluent-svg/`,
 * apaga os que ninguém usa e regrava `catalog/fluent-icons.txt`. Roda à mão, nunca no build: o build
 * usa só o que está no git e funciona sem internet.
 */
@UntrackedTask(because = "Baixa da internet e grava em catalog/, que fica no git")
abstract class DownloadFluentIconsTask : DefaultTask() {
    @get:Internal
    abstract val sectionsFile: RegularFileProperty

    @get:Internal
    abstract val productsFile: RegularFileProperty

    @get:Internal
    abstract val fluentIconsFile: RegularFileProperty

    @get:Internal
    abstract val svgDir: DirectoryProperty

    @TaskAction
    fun download() {
        val client = HttpClient.newBuilder().connectTimeout(TIMEOUT).followRedirects(HttpClient.Redirect.NORMAL).build()
        val available = selectColorSvgPaths(treePaths(client))
        fluentIconsFile.get().asFile.writeText(fluentIconsFileContent(FLUENT_COMMIT, available.keys))

        val used = referencedFluentIcons(sectionsFile.get().asFile.readText(), productsFile.get().asFile.readText())
        val missing = used - available.keys
        if (missing.isNotEmpty()) {
            throw GradleException("Ícones Fluent sem SVG Color no commit $FLUENT_COMMIT: ${missing.joinToString()}")
        }

        val dir = svgDir.get().asFile.apply { mkdirs() }
        dir.listFiles { f -> f.extension == "svg" && f.nameWithoutExtension !in used }?.forEach {
            it.delete()
            logger.lifecycle("Apagado (sem uso): ${it.name}")
        }
        used.filterNot { dir.resolve("$it.svg").exists() }.forEach { icon ->
            val url = RAW_URL.format(FLUENT_COMMIT, encodePath(available.getValue(icon)))
            dir.resolve("$icon.svg").writeBytes(get(client, url))
            logger.lifecycle("Baixado: $icon")
        }
        logger.lifecycle("${used.size} ícones Fluent em catalog/$FLUENT_SVG_DIR/")
    }

    private fun treePaths(client: HttpClient): List<String> {
        val json = JsonSlurper().parse(get(client, TREE_URL.format(FLUENT_COMMIT))) as Map<*, *>
        if (json["truncated"] == true) throw GradleException("Árvore do Fluent Emoji veio incompleta")
        return (json["tree"] as List<*>).map { (it as Map<*, *>)["path"] as String }
    }

    private fun get(client: HttpClient, url: String): ByteArray {
        val request = HttpRequest.newBuilder(URI(url)).timeout(TIMEOUT).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())
        if (response.statusCode() != HTTP_OK) throw GradleException("HTTP ${response.statusCode()} em $url")
        return response.body()
    }
}

/** Codifica cada parte do caminho para URL (`Red apple` → `Red%20apple`). */
internal fun encodePath(path: String): String =
    path.split('/').joinToString("/") { URLEncoder.encode(it, Charsets.UTF_8).replace("+", "%20") }
