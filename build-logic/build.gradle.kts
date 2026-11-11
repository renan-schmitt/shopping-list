plugins {
    `kotlin-dsl`
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    // API do AGP para ligar os ícones gerados às fontes do app; o AGP em si vem do projeto que aplica
    compileOnly(libs.android.gradle.api)
    // Svg2Vector: o conversor SVG → VectorDrawable do Vector Asset do Android Studio
    implementation(libs.android.tools.sdk.common)
    testImplementation(kotlin("test-junit"))
}

gradlePlugin {
    plugins {
        register("catalog") {
            id = "listadecompras.catalog"
            implementationClass = "br.com.mentealerta.listadecompras.buildlogic.catalog.CatalogPlugin"
        }
        register("catalogIcons") {
            id = "listadecompras.catalog-icons"
            implementationClass = "br.com.mentealerta.listadecompras.buildlogic.catalog.CatalogIconsPlugin"
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
