package br.com.mentealerta.listadecompras.catalog

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/** Busca aproximada com ranking (#43). */
class CatalogSearchTest {
    private val index = CatalogIndex(EmbeddedCatalog.catalog)

    private fun firstId(text: String): String? = index.search(text).firstOrNull()?.product?.id

    private fun ids(text: String, counts: Map<String, Int> = emptyMap()) =
        index.search(text, counts).map { it.product.id }

    @Test
    fun findsWithoutAccentsInPluralAndUppercase() {
        assertEquals("apple", firstId("maca"))
        assertEquals("apple", firstId("MAÇÃS"))
        assertEquals("french_bread", firstId("paes franceses"))
    }

    @Test
    fun findsWithTypingErrors() {
        assertEquals("beans", firstId("fejao"))
        assertEquals("rice", firstId("arros"))
        assertEquals("banana", firstId("bananna"))
        assertEquals("apple", firstId("mcaa"))
        assertEquals("cassava", firstId("macaxiera"))
    }

    @Test
    fun findsWhileTyping() {
        assertEquals("cassava", firstId("macax"))
        assertTrue("rice" in ids("arr"))
        assertTrue("ground_beef" in ids("carne moi"))
        assertTrue("ground_beef" in ids("moid"))
    }

    @Test
    fun findsBySynonymAndRegionalismAndTellsWhichTerm() {
        val result = index.search("aipim").first()
        assertEquals("cassava", result.product.id)
        assertEquals("aipim", result.term?.term)
        assertEquals(MatchKind.EXACT, result.kind)
        assertEquals("french_bread", firstId("Cacetinho"))
    }

    @Test
    fun exactComesBeforePrefixAndPrefixBeforeApprox() {
        val results = index.search("arroz")
        assertEquals("rice", results.first().product.id)
        assertEquals(MatchKind.EXACT, results.first().kind)
        assertEquals(results.map { it.kind }.sorted(), results.map { it.kind })
        assertTrue(results.any { it.kind == MatchKind.PREFIX })
    }

    @Test
    fun eachProductAppearsOnce() {
        listOf("maca", "aipim", "feijao", "pao", "leite").forEach { text ->
            val found = ids(text)
            assertEquals(found.distinct(), found, text)
        }
    }

    @Test
    fun mostBoughtBreaksTies() {
        val withoutHistory = ids("feijao")
        assertEquals("beans", withoutHistory.first())
        assertTrue(withoutHistory.indexOf("black_beans") > 1)
        val withHistory = ids("feijao", mapOf("black_beans" to 5, "carioca_beans" to 2))
        assertEquals(listOf("beans", "black_beans", "carioca_beans"), withHistory.take(3))
    }

    @Test
    fun shortTextDoesNotAcceptErrors() {
        assertTrue(index.search("pa").all { it.kind != MatchKind.APPROX })
        assertTrue(index.search("qz").isEmpty())
    }

    @Test
    fun unknownOrBlankTextFindsNothing() {
        assertEquals(emptyList(), index.search("xyzzy"))
        assertEquals(emptyList(), index.search(""))
        assertEquals(emptyList(), index.search("   "))
    }

    @Test
    fun respectsTheLimit() {
        assertEquals(3, index.search("a", limit = 3).size)
        assertEquals(emptyList(), index.search("arroz", limit = 0))
        assertTrue(index.search("a").size <= CatalogIndex.DEFAULT_SEARCH_LIMIT)
    }

    @Test
    fun rankingRulesOnASmallCatalog() {
        val small = CatalogIndex(
            Catalog(
                sections = listOf(section),
                products = listOf(
                    product("melon", "Melão"),
                    product("melancia", "Melancia"),
                    product("mel", "Mel"),
                    product("bread", "Pão de mel"),
                ),
                terms = listOf(CatalogTerm("melaço", "mel", emptySet())),
                replacements = emptyMap(),
                retiredProductIds = emptySet(),
            ),
        )
        fun found(text: String, counts: Map<String, Int> = emptyMap()) =
            small.search(text, counts).map { it.product.id to it.kind }

        assertEquals(
            listOf(
                "mel" to MatchKind.EXACT,
                "melon" to MatchKind.PREFIX,
                "melancia" to MatchKind.PREFIX,
                "bread" to MatchKind.WORD_PREFIX,
            ),
            found("mel"),
        )
        assertEquals("melancia", found("mel", mapOf("melancia" to 3)).getOrNull(1)?.first)
        assertEquals(
            listOf(
                "melon" to MatchKind.PREFIX,
                "melancia" to MatchKind.PREFIX,
                "mel" to MatchKind.PREFIX,
                "bread" to MatchKind.APPROX,
            ),
            found("mela"),
        )
    }

    @Test
    fun searchIsFastOnTheWholeCatalog() {
        val texts = buildList {
            addAll(listOf("a", "ma", "maca", "fejao", "arros", "macaxiera", "carne moi", "xyzzy", "bananna"))
            index.catalog.products.forEach { add(it.name.take(4)) }
        }.take(SEARCHES)
        repeat(WARM_UP) { texts.forEach { index.search(it) } }

        val times = texts.map { text ->
            val mark = TimeSource.Monotonic.markNow()
            index.search(text)
            mark.elapsedNow()
        }
        val median = times.sorted()[times.size / 2]
        assertTrue(median < MAX_MEDIAN, "Mediana de $median por busca, acima de $MAX_MEDIAN")
    }

    private val section = CatalogSection("produce", "Hortifrúti", 1, CatalogIcon.Fluent("Leafy green"))

    private fun product(id: String, name: String) = CatalogProduct(id, name, "produce", 1, null, null)

    private companion object {
        const val SEARCHES = 200
        const val WARM_UP = 3

        /** 10× abaixo dos 50 ms pedidos, para sobrar folga em aparelho de entrada. */
        val MAX_MEDIAN: Duration = 5.milliseconds
    }
}
