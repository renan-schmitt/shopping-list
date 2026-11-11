import br.com.mentealerta.listadecompras.buildlogic.catalog.GenerateCatalogTask

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    id("listadecompras.catalog")
}

kotlin {
    jvmToolchain(21)

    android {
        namespace = "br.com.mentealerta.listadecompras.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        withHostTest {}
    }

    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        // Catálogo embarcado gerado (e validado) a partir de catalog/*.csv
        commonMain {
            kotlin.srcDir(tasks.named<GenerateCatalogTask>("generateCatalog").flatMap { it.outputDir })
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

// A validação da planilha e os testes do gerador também rodam no check (e no build do CI)
tasks.named("check") {
    dependsOn("generateCatalog", gradle.includedBuild("build-logic").task(":test"))
}
