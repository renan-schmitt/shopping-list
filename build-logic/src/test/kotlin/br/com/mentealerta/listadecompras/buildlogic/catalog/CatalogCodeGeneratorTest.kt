package br.com.mentealerta.listadecompras.buildlogic.catalog

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CatalogCodeGeneratorTest {
    private val catalog = Catalog(
        sections = listOf(Section("produce", "Hortifrúti", 1, "Leafy green")),
        products = (1..250).map { Product("p$it", "Produto \"$it\" \$x", "produce", it * 10, null, null) } +
            Product("apple", "Maçã", "produce", 9999, "fluent:Red apple", "exact") +
            Product("papaya", "Mamão", "produce", 10000, "own:papaya", "approx"),
        terms = listOf(Term("bergamota", "p1", listOf("south", "central_southeast"))),
        replacements = mapOf("old" to "p1"),
        retiredProductIds = setOf("old", "older"),
    )

    @Test
    fun splitsProductsIntoChunksToStayUnderTheJvmMethodLimit() {
        val code = generateCatalogCode(catalog)
        assertTrue("products = products0() + products1() + products2()," in code)
        assertTrue("private fun products2(): List<CatalogProduct>" in code)
        assertTrue("terms = terms0()," in code)
    }

    @Test
    fun writesIconsRegionsAndEscapedStrings() {
        val code = generateCatalogCode(catalog)
        val apple = "CatalogProduct(\"apple\", \"Maçã\", \"produce\", 9999, " +
            "CatalogIcon.Fluent(\"Red apple\"), IconFit.EXACT)"
        assertTrue(apple in code)
        assertTrue("CatalogIcon.Own(\"papaya\"), IconFit.APPROX)" in code)
        assertTrue("CatalogProduct(\"p1\", \"Produto \\\"1\\\" \\\$x\", \"produce\", 10, null, null)" in code)
        assertTrue("CatalogTerm(\"bergamota\", \"p1\", setOf(Region.SOUTH, Region.CENTRAL_SOUTHEAST))" in code)
        assertTrue("replacements = mapOf(\"old\" to \"p1\")," in code)
        assertTrue("retiredProductIds = setOf(\"old\", \"older\")," in code)
    }

    @Test
    fun sameCatalogGivesTheSameCode() {
        assertEquals(generateCatalogCode(catalog), generateCatalogCode(catalog.copy()))
    }

    @Test
    fun emptyListsUseEmptyCollections() {
        val empty = catalog.copy(terms = emptyList(), replacements = emptyMap(), retiredProductIds = emptySet())
        val code = generateCatalogCode(empty)
        assertTrue("terms = emptyList()," in code)
        assertTrue("replacements = emptyMap()," in code)
        assertTrue("retiredProductIds = emptySet()," in code)
    }
}
