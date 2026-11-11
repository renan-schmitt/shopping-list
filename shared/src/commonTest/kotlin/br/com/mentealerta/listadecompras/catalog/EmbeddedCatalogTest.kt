package br.com.mentealerta.listadecompras.catalog

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmbeddedCatalogTest {
    private val catalog = EmbeddedCatalog.catalog

    @Test
    fun sectionsAreInOrderWithOtherLast() {
        assertEquals(catalog.sections.sortedBy { it.order }, catalog.sections)
        assertEquals(CatalogSection.OTHER_ID, catalog.sections.last().id)
    }

    @Test
    fun everyProductPointsToAnExistingSection() {
        val sectionIds = catalog.sections.map { it.id }.toSet()
        assertTrue(catalog.products.isNotEmpty())
        assertTrue(catalog.products.all { it.sectionId in sectionIds })
    }

    @Test
    fun productIdsAreUniqueAndNotRetired() {
        val ids = catalog.products.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        assertTrue(ids.none { it in catalog.retiredProductIds })
    }

    @Test
    fun productsWithIconHaveIconFit() {
        assertTrue(catalog.products.all { (it.icon == null) == (it.iconFit == null) })
    }

    @Test
    fun everyTermPointsToAnActiveProductAndIsUnique() {
        val productIds = catalog.products.map { it.id }.toSet()
        val terms = catalog.terms.map { it.term }
        assertTrue(catalog.terms.isNotEmpty())
        assertTrue(catalog.terms.all { it.productId in productIds })
        assertEquals(terms.size, terms.toSet().size)
    }

    @Test
    fun replacementsPointToActiveProducts() {
        val productIds = catalog.products.map { it.id }.toSet()
        assertTrue(catalog.replacements.all { (old, new) -> old in catalog.retiredProductIds && new in productIds })
    }

    @Test
    fun keepsAccentsAndRegionsFromTheSpreadsheet() {
        assertEquals("Maçã", catalog.products.single { it.id == "apple" }.name)
        val caturra = catalog.terms.single { it.term == "banana-caturra" }
        assertEquals("banana", caturra.productId)
        assertEquals(setOf(Region.SOUTH), caturra.regions)
    }
}
