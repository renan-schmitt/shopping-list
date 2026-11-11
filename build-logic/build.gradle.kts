plugins {
    `kotlin-dsl`
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
