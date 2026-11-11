package br.com.mentealerta.listadecompras.buildlogic.catalog

import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CatalogIconConverterTest {
    @Test
    fun fluentFolderBecomesAResourceName() {
        assertEquals("catalog_fluent_red_apple", fluentResourceName("Red apple"))
        assertEquals("catalog_fluent_high_heeled_shoe", fluentResourceName("High-heeled shoe"))
        assertEquals("catalog_fluent_man_s_shoe", fluentResourceName("Man’s shoe"))
        assertEquals("catalog_fluent_pinata", fluentResourceName("Piñata"))
        assertEquals("catalog_fluent_1st_place_medal", fluentResourceName("1st place medal"))
    }

    @Test
    fun ownIdMustBeAValidResourceName() {
        assertEquals("catalog_own_rice_package", ownResourceName("rice_package"))
        assertFailsWith<IllegalArgumentException> { ownResourceName("Pacote de arroz") }
    }

    @Test
    fun clashingResourceNamesAreAnError() {
        val icons = listOf(
            IconSource("High-heeled shoe", "catalog_fluent_high_heeled_shoe", File("High-heeled shoe.svg")),
            IconSource("High heeled shoe", "catalog_fluent_high_heeled_shoe", File("High heeled shoe.svg")),
        )
        val error = assertFailsWith<IllegalArgumentException> { checkResourceNames(icons) }
        assertTrue("catalog_fluent_high_heeled_shoe" in error.message.orEmpty())
    }

    @Test
    fun convertsSvgAndIgnoresFilterMessages() {
        val svg = createTempDirectory().toFile().resolve("dot.svg")
        svg.writeText(
            """
            <svg width="32" height="32" viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg">
              <g filter="url(#f)"><circle cx="16" cy="16" r="8" fill="#FF0000"/></g>
              <defs><filter id="f"><feGaussianBlur stdDeviation="1"/></filter></defs>
            </svg>
            """.trimIndent(),
        )
        val converted = convertSvg(svg)
        assertTrue("<vector" in converted.vectorXml)
        assertTrue("android:fillColor=\"#FF0000\"" in converted.vectorXml, converted.vectorXml)
        assertEquals(emptyList(), converted.warnings)
    }

    @Test
    fun generatedCodeMapsEachIconToItsDrawable() {
        val code = generateIconResourcesCode(
            packageName = "app.ui.catalog",
            rPackage = "app",
            fluent = listOf(
                IconSource("Red apple", "catalog_fluent_red_apple", File("Red apple.svg")),
                IconSource("Banana", "catalog_fluent_banana", File("Banana.svg")),
            ),
            own = emptyList(),
        )
        assertTrue("package app.ui.catalog" in code)
        assertTrue("import app.R" in code)
        val banana = code.indexOf("\"Banana\" to R.drawable.catalog_fluent_banana,")
        val apple = code.indexOf("\"Red apple\" to R.drawable.catalog_fluent_red_apple,")
        assertTrue(banana in 0 until apple, code)
        assertTrue("private val OWN: Map<String, Int> = emptyMap()" in code)
    }
}
