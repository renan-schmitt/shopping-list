plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.roborazzi)
    // Ícones do catálogo: SVG de catalog/ → VectorDrawable (#22)
    id("listadecompras.catalog-icons")
}

kotlin {
    jvmToolchain(21)
}

/** IDs de teste do Google: anúncios de verdade, sem cobrança nem risco para a conta do AdMob. */
val admobTestAppId = "ca-app-pub-3940256099942544~3347511713"
val admobTestInterstitialId = "ca-app-pub-3940256099942544/1033173712"

fun com.android.build.api.dsl.ApplicationBuildType.admobIds(appId: String, interstitialId: String) {
    manifestPlaceholders["admobAppId"] = appId
    buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"$interstitialId\"")
}

android {
    namespace = "br.com.mentealerta.listadecompras"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "br.com.mentealerta.listadecompras"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        // Anúncios (plano §3.7, #89): o debug sempre usa os IDs de teste do Google; o release usa os
        // do AdMob em gradle.properties (docs/configurar-anuncios.md) e, sem eles, os de teste.
        debug {
            admobIds(appId = admobTestAppId, interstitialId = admobTestInterstitialId)
        }
        release {
            isMinifyEnabled = false
            admobIds(
                appId = providers.gradleProperty("admob.appId").getOrElse(admobTestAppId),
                interstitialId = providers.gradleProperty("admob.interstitialId").getOrElse(admobTestInterstitialId),
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    androidResources {
        // Lista de idiomas montada a partir das pastas values-*; values/ é pt-BR
        // (res/resources.properties). Plano §4.2, traduções.
        generateLocaleConfig = true
    }

    testOptions {
        unitTests {
            // Robolectric precisa dos recursos (fontes, cores) nos testes de screenshot.
            isIncludeAndroidResources = true
            all {
                // Robolectric com SDK 36 acessa internals do JDK 21.
                it.jvmArgs("--add-opens=java.base/jdk.internal.access=ALL-UNNAMED")
            }
        }
    }
}

roborazzi {
    outputDir.set(file("src/test/screenshots"))
}

dependencies {
    implementation(project(":shared"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.animation.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    // Versão completa: compra única direto pelo Google Play Billing (plano §4.3, #86)
    implementation(libs.billing.ktx)
    // Anúncios da versão grátis e o consentimento onde a lei exige (plano §3.7, #89)
    implementation(libs.play.services.ads)
    implementation(libs.ump)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)
    // Testes ponta a ponta abrem o banco de verdade na JVM do computador: o SQLite embarcado precisa da
    // biblioteca nativa de desktop (o AAR só traz a do Android)
    testImplementation(libs.sqlite.bundled.jvm)
}
