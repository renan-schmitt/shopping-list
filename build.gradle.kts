import dev.detekt.gradle.extensions.DetektExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
}

// Estilo e análise estática (plano §4.6): ktlint + detekt em todos os módulos.
// `./gradlew lint` roda tudo (com o Android Lint); `check` e `build` também.
val ktlintVersion = libs.versions.ktlint.engine.get()
val composeRulesKtlint = libs.compose.rules.ktlint
val detektConfig = rootProject.file("config/detekt/detekt.yml")

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "dev.detekt")

    extensions.configure<KtlintExtension> {
        version.set(ktlintVersion)
        // Código gerado (catálogo embarcado) fica de fora
        filter { exclude { it.file.path.contains("/build/") } }
    }
    dependencies { add("ktlintRuleset", composeRulesKtlint) }

    extensions.configure<DetektExtension> {
        buildUponDefaultConfig.set(true)
        config.setFrom(detektConfig)
        // Todos os source sets (commonMain, androidMain, iosMain, test, debug...)
        source.setFrom("src")
    }

    val lintTasks = listOf("ktlintCheck", "detekt")
    tasks.matching { it.name == "lint" }.configureEach { dependsOn(lintTasks) }
    afterEvaluate {
        if (tasks.findByName("lint") == null) {
            tasks.register("lint") {
                group = "verification"
                description = "Roda ktlint e detekt."
                dependsOn(lintTasks)
            }
        }
    }
}

// O build-logic (gerador do catálogo) é um included build: entra no lint pela raiz
tasks.register("lint") {
    group = "verification"
    description = "Roda ktlint e detekt no build-logic."
    dependsOn(gradle.includedBuild("build-logic").task(":lint"))
}
