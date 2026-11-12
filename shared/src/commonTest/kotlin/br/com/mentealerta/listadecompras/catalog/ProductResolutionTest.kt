package br.com.mentealerta.listadecompras.catalog

import br.com.mentealerta.listadecompras.normalization.searchKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Texto → produto canônico pelo nome ou por sinônimo e regionalismo, com o catálogo real (#42). */
class ProductResolutionTest {
    private val index = CatalogIndex(EmbeddedCatalog.catalog)

    private fun resolvedId(text: String): String? = index.resolve(text)?.product?.id

    @Test
    fun everyTermOfTheCatalogResolvesToItsProduct() {
        val wrong = index.catalog.terms.filter { term ->
            listOf(term.term, term.term.uppercase(), searchKey(term.term)).any { resolvedId(it) != term.productId }
        }
        assertEquals(emptyList(), wrong)
    }

    @Test
    fun everyProductResolvesByItsNameWithoutTerm() {
        val wrong = index.catalog.products.filter { index.resolve(it.name) != ProductMatch(it, term = null) }
        assertEquals(emptyList(), wrong)
    }

    @Test
    fun regionalismsFromAcrossBrazilLeadToTheSameProduct() {
        assertEquals("cassava", resolvedId("aipim"))
        assertEquals("cassava", resolvedId("Macaxeira"))
        assertEquals("tangerine", resolvedId("tangerina"))
        assertEquals("french_bread", resolvedId("Cacetinho"))
        assertEquals("french_bread", resolvedId("pão de sal"))
        assertEquals("pumpkin", resolvedId("jerimum"))
        assertEquals(resolvedId("bolo de aipim"), resolvedId("Bolo de Macaxeira"))
        assertEquals("cassava_cake", resolvedId("bolo de macaxeira"))
    }

    @Test
    fun resolvesInPluralWithoutAccentsAndInUppercase() {
        assertEquals("cassava", resolvedId("MACAXEIRAS"))
        assertEquals("french_bread", resolvedId("paes de sal"))
        assertEquals("tangerine", resolvedId("Tangerinas"))
    }

    @Test
    fun tellsWhichTermWasUsed() {
        val match = index.resolve("Macaxeiras")
        assertEquals("cassava", match?.product?.id)
        assertEquals("macaxeira", match?.term?.term)
        assertNull(index.resolve("mandioca")?.term)
    }

    @Test
    fun ambiguousTermsAwaitingReviewDoNotResolve() {
        assertNull(index.resolve("bergamota"))
        assertNull(index.resolve("bolacha"))
    }

    @Test
    fun unknownOrBlankTextDoesNotResolve() {
        assertNull(index.resolve("xyzzy"))
        assertNull(index.resolve(""))
        assertNull(index.resolve("   "))
    }

    @Test
    fun nameWinsOverTermOfAnotherProductWithTheSameKey() {
        val small = CatalogIndex(
            Catalog(
                sections = listOf(section),
                products = listOf(product("a", "Mexerica"), product("b", "Bergamota")),
                terms = listOf(CatalogTerm("bergamota", "a", setOf(Region.SOUTH))),
                replacements = emptyMap(),
                retiredProductIds = emptySet(),
            ),
        )
        assertEquals(ProductMatch(product("b", "Bergamota"), term = null), small.resolve("bergamotas"))
        assertEquals(listOf("b", "a"), small.findByKey("bergamota").map { it.id })
    }

    private val section = CatalogSection("produce", "Hortifrúti", 1, CatalogIcon.Fluent("Leafy green"))

    private fun product(id: String, name: String) = CatalogProduct(id, name, "produce", 1, null, null)
}
