import br.com.mentealerta.listadecompras.buildlogic.catalog.GenerateCatalogTask

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    id("listadecompras.catalog")
}

kotlin {
    jvmToolchain(21)

    // O Room gera o `actual` do construtor do banco (`expect object`) em cada plataforma
    compilerOptions { freeCompilerArgs.add("-Xexpect-actual-classes") }

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
        commonMain.dependencies {
            api(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
            api(libs.datastore.preferences.core)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.koin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
        // Os testes de host do Android rodam na JVM do computador: o SQLite embarcado precisa da
        // biblioteca nativa de desktop (o AAR só traz a do Android)
        getByName("androidHostTest").dependencies {
            implementation(libs.sqlite.bundled.jvm)
        }
    }
}

// Banco local (plano §4.3): o schema de cada versão fica em shared/schemas, no git, para
// conferir e testar as migrations
room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    listOf("kspAndroid", "kspIosArm64", "kspIosSimulatorArm64").forEach { add(it, libs.room.compiler) }
}

// A validação da planilha, os testes e o lint do gerador também rodam no check (e no build do CI)
tasks.named("check") {
    dependsOn("generateCatalog", gradle.includedBuild("build-logic").task(":check"))
}
