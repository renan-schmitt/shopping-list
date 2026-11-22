package br.com.mentealerta.listadecompras.buildlogic.catalog

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IconsPageTest {
    private val catalog = Catalog(
        sections = listOf(Section("produce", "Hortifrúti", 1, "Leafy green")),
        products = listOf(
            Product("apple", "Maçã", "produce", 10, "fluent:Red apple", "exact"),
            Product("plantain", "Banana-da-terra", "produce", 20, "fluent:Banana", "approx"),
            Product("rice", "Arroz", "produce", 30, "own:rice_package", "exact"),
            Product("papaya", "Mamão", "produce", 40, null, null),
            Product("guava", "Goiaba", "produce", 50, null, null),
        ),
        terms = emptyList(),
        replacements = emptyMap(),
        retiredProductIds = emptySet(),
    )

    private val header = "icon_id,name,priority,products,notes\n"

    @Test
    fun countsExactApproxAndMissingIcons() {
        val coverage = iconCoverage(catalog.products)
        assertEquals(IconCoverage(exact = 2, approx = 1, none = 2), coverage)
        assertEquals(40, coverage.percent(coverage.exact))
    }

    @Test
    fun readsAValidQueue() {
        val csv = header + "papaya,Mamão,1,papaya;guava,Metade aberta\nbanana_bunch,Penca,3,plantain,\n"
        val queue = parseIconQueue(csv, catalog, ownIcons = setOf("rice_package"))
        assertEquals(emptyList(), queue.errors)
        assertEquals(QueuedIcon("papaya", "Mamão", 1, listOf("papaya", "guava"), "Metade aberta"), queue.icons.first())
    }

    @Test
    fun rejectsStaleOrBrokenRows() {
        val csv = header +
            "rice_package,Arroz,1,papaya,\n" +
            "Bad-Id,Ruim,4,apple;missing,\n" +
            "guava,Goiaba,2,guava;papaya,\n" +
            "guava,,2,,\n"
        val errors = parseIconQueue(csv, catalog, ownIcons = setOf("rice_package")).errors
        val expected = listOf(
            "own-icons-queue.csv:2: \"rice_package\" já existe em own-icons/; " +
                "ligue os produtos na planilha e tire da fila",
            "own-icons-queue.csv:3: icon_id \"Bad-Id\" fora do padrão (minúsculas, números e _)",
            "own-icons-queue.csv:3: priority deve ser 1, 2 ou 3",
            "own-icons-queue.csv:3: \"apple\" já tem ícone fiel; tire da fila",
            "own-icons-queue.csv:3: produto \"missing\" não existe ou não está ativo",
            "own-icons-queue.csv:4: produto \"papaya\" já está em outro desenho",
            "own-icons-queue.csv:5: icon_id \"guava\" repetido",
            "own-icons-queue.csv:5: name vazio",
            "own-icons-queue.csv:5: products vazio",
        )
        assertEquals(expected, errors)
    }

    @Test
    fun pageShowsCoverageQueueAndLocalIcons() {
        val queue = listOf(QueuedIcon("papaya", "Mamão & cia", 1, listOf("papaya", "guava"), ""))
        val page = generateIconsPage(catalog, queue)
        assertTrue("<b>5</b>produtos ativos" in page)
        assertTrue("<b>2</b>ícone fiel (40%)" in page)
        assertTrue("<h3>Prioridade 1: 1 desenhos, 2 produtos</h3>" in page)
        assertTrue("Mamão &amp; cia" in page)
        assertTrue("../../catalog/fluent-svg/Red%20apple.svg" in page)
        assertTrue("../../catalog/own-icons/rice_package.svg" in page)
        assertTrue("fila: prioridade 1" in page)
        assertTrue("cdn.jsdelivr" !in page)
        assertEquals(page, generateIconsPage(catalog.copy(), queue))
    }
}
