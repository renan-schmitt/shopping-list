plugins {
    `kotlin-dsl`
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    testImplementation(kotlin("test-junit"))
}

gradlePlugin {
    plugins {
        register("catalog") {
            id = "listadecompras.catalog"
            implementationClass = "br.com.mentealerta.listadecompras.buildlogic.catalog.CatalogPlugin"
        }
    }
}

// Mesmo padrão de estilo e análise estática do app (ver build.gradle.kts da raiz)
ktlint {
    version.set(libs.versions.ktlint.engine)
}

detekt {
    buildUponDefaultConfig.set(true)
    config.setFrom(file("../config/detekt/detekt.yml"))
    source.setFrom("src")
}

tasks.register("lint") {
    group = "verification"
    description = "Roda ktlint e detekt."
    dependsOn("ktlintCheck", "detekt")
}
