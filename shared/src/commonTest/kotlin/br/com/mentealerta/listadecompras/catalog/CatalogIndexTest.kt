package br.com.mentealerta.listadecompras.catalog

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CatalogIndexTest {
    private val index = CatalogIndex(EmbeddedCatalog.catalog)

    @Test
    fun findsEveryProductByItsOwnName() {
        val catalog = index.catalog
        assertTrue(catalog.products.all { it in index.findByKey(it.name) })
    }

    @Test
    fun findsByNameIgnoringCaseAndSurroundingSpaces() {
        assertEquals(listOf("apple"), index.findByKey("  MAÇÃ ").map { it.id })
    }

    @Test
    fun findsBySynonymsAndRegionalisms() {
        assertEquals(listOf("cassava"), index.findByKey("aipim").map { it.id })
        assertEquals(listOf("cassava"), index.findByKey("Macaxeira").map { it.id })
        assertEquals(listOf("banana"), index.findByKey("banana-caturra").map { it.id })
    }

    @Test
    fun unknownTextFindsNothing() {
        assertTrue(index.findByKey("xyzzy").isEmpty())
    }

    @Test
    fun looksUpProductsSectionsAndTermsById() {
        assertEquals("Maçã", index.product("apple")?.name)
        assertNull(index.product("does_not_exist"))
        assertEquals("Hortifrúti", index.section("produce")?.name)
        assertEquals(setOf("aipim", "macaxeira"), index.termsOf("cassava").map { it.term }.toSet())
    }

    @Test
    fun productsInSectionKeepTheCatalogOrder() {
        val produce = index.productsIn("produce")
        assertTrue(produce.isNotEmpty())
        assertTrue(produce.all { it.sectionId == "produce" })
        assertEquals(produce.sortedBy { it.order }, produce)
        assertTrue(index.productsIn("does_not_exist").isEmpty())
    }

    @Test
    fun retiredIdLeadsToTheReplacementProduct() {
        val small = CatalogIndex(
            Catalog(
                sections = listOf(section),
                products = listOf(product("cassava", "Mandioca")),
                terms = emptyList(),
                replacements = mapOf("aipim_old" to "cassava"),
                retiredProductIds = setOf("aipim_old"),
            ),
        )
        assertEquals("cassava", small.product("aipim_old")?.id)
    }

    @Test
    fun keyCollisionReturnsEveryProductOnce() {
        val small = CatalogIndex(
            Catalog(
                sections = listOf(section),
                products = listOf(product("a", "Pão"), product("b", "Pao")),
                terms = listOf(CatalogTerm("pão", "a", emptySet())),
                replacements = emptyMap(),
                retiredProductIds = emptySet(),
            ),
            key = { it.lowercase().replace('ã', 'a') },
        )
        assertEquals(listOf("a", "b"), small.findByKey("PAO").map { it.id })
    }

    private val section = CatalogSection("produce", "Hortifrúti", 1, CatalogIcon.Fluent("Leafy green"))

    private fun product(id: String, name: String) = CatalogProduct(id, name, "produce", 1, null, null)
}
