package br.com.mentealerta.listadecompras.buildlogic.catalog

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.fail

private const val SECTIONS_HEADER = "id,name,order,fluent_icon"
private const val PRODUCTS_HEADER = "id,name,section,order,icon,icon_fit,status,replaced_by,notes"
private const val TERMS_HEADER = "product_id,term,regions,status,ambiguous,evidence,notes"

class CatalogValidatorTest {
    private val sections = listOf(
        "produce,Hortifrúti,1,Leafy green",
        "bakery,Padaria,2,Bread",
        "other,Outros,3,Shopping cart",
    )
    private val products = listOf(
        "banana,Banana,produce,10,fluent:Banana,exact,active,,",
        "apple,Maçã,produce,20,fluent:Red apple,exact,active,,\"Nota, com vírgula\"",
        "french_bread,Pão francês,bakery,10,fluent:Baguette bread,approx,active,,",
        "bread_roll,Pãozinho,bakery,20,,,retired,french_bread,Juntado",
        "papaya,Mamão,produce,30,,,active,,",
    )
    private val terms = listOf(
        "banana,banana-caturra,south,to_confirm,no,,",
        "french_bread,cacetinho,south,confirmed,no,dicionário,",
        "french_bread,pão,,to_confirm,yes,,Ambíguo: fica fora",
        "apple,maçã gala,,rejected,no,,",
    )

    private fun source(
        sections: List<String> = this.sections,
        products: List<String> = this.products,
        terms: List<String> = this.terms,
        ownIcons: Set<String> = emptySet(),
    ) = CatalogSource(
        sectionsCsv = (listOf(SECTIONS_HEADER) + sections).joinToString("\n"),
        productsCsv = (listOf(PRODUCTS_HEADER) + products).joinToString("\n"),
        termsCsv = (listOf(TERMS_HEADER) + terms).joinToString("\n"),
        fluentIcons = setOf("Leafy green", "Bread", "Shopping cart", "Banana", "Red apple", "Baguette bread"),
        ownIcons = ownIcons,
    )

    private fun errorsOf(source: CatalogSource): List<String> =
        when (val result = validateCatalog(source)) {
            is ValidationResult.Invalid -> result.errors
            is ValidationResult.Valid -> fail("Esperava erro, mas a planilha passou")
        }

    private fun assertError(expected: String, source: CatalogSource) {
        val errors = errorsOf(source)
        assertTrue(errors.any { expected in it }, "Esperava \"$expected\" em $errors")
    }

    @Test
    fun validSpreadsheetBuildsTheCatalog() {
        val result = validateCatalog(source())
        val catalog = assertIs<ValidationResult.Valid>(result).catalog
        assertEquals(listOf("produce", "bakery", "other"), catalog.sections.map { it.id })
        assertEquals(listOf("banana", "apple", "papaya", "french_bread"), catalog.products.map { it.id })
        assertEquals(listOf("banana-caturra", "cacetinho"), catalog.terms.map { it.term })
        assertEquals(mapOf("bread_roll" to "french_bread"), catalog.replacements)
        assertEquals(setOf("bread_roll"), catalog.retiredProductIds)
    }

    @Test
    fun sectionErrors() {
        assertError("seção \"produce\" repetida", source(sections = sections + "produce,X,4,Bread"))
        assertError("order 1 repetida", source(sections = listOf(sections[0], "bakery,Padaria,1,Bread", sections[2])))
        assertError("ícone Fluent \"Nope\"", source(sections = listOf("produce,Hortifrúti,1,Nope") + sections.drop(1)))
        assertError("falta a seção \"other\"", source(sections = sections.take(2)))
        assertError("\"other\" (Outros) precisa ser a última", source(sections = sections + "pet,Pet,4,Bread"))
        assertError("id \"Bakery\"", source(sections = listOf(sections[0], "Bakery,Padaria,2,Bread", sections[2])))
    }

    @Test
    fun productErrors() {
        assertError("produto \"banana\" repetido", source(products = products + "banana,Banana 2,produce,90,,,active,,"))
        assertError("seção \"frozen\" não existe", source(products = products + "ice,Gelo,frozen,10,,,active,,"))
        assertError("order 10 já usada por \"banana\"", source(products = products + "kiwi,Kiwi,produce,10,,,active,,"))
        assertError("não é um número", source(products = products + "kiwi,Kiwi,produce,dez,,,active,,"))
        assertError("ícone Fluent \"Kiwi fruit\"", source(products = products + "kiwi,Kiwi,produce,90,fluent:Kiwi fruit,exact,active,,"))
        assertError("ícone próprio \"kiwi\"", source(products = products + "kiwi,Kiwi,produce,90,own:kiwi,exact,active,,"))
        assertError("deve começar com fluent: ou own:", source(products = products + "kiwi,Kiwi,produce,90,Banana,exact,active,,"))
        assertError("icon_fit preenchido sem ícone", source(products = products + "kiwi,Kiwi,produce,90,,exact,active,,"))
        assertError("icon_fit deve ser exact ou approx", source(products = products + "kiwi,Kiwi,produce,90,fluent:Banana,,active,,"))
        assertError("status \"old\"", source(products = products + "kiwi,Kiwi,produce,90,,,old,,"))
        assertError("replaced_by só vale para produto retired", source(products = products + "kiwi,Kiwi,produce,90,,,active,banana,"))
        assertError("replaced_by \"bread_roll\" não é um produto active", source(products = products + "roll,Rosca,bakery,30,,,retired,bread_roll,"))
        assertError("nome \"banana\" já usado por \"banana\"", source(products = products + "banana2,banana,produce,90,,,active,,"))
    }

    @Test
    fun ownIconThatExistsIsAccepted() {
        val result = validateCatalog(source(products = products + "kiwi,Kiwi,produce,90,own:kiwi,exact,active,,", ownIcons = setOf("kiwi")))
        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun termErrors() {
        assertError("produto \"kiwi\" não existe", source(terms = terms + "kiwi,quiuí,,to_confirm,no,,"))
        assertError("deve estar em minúsculas", source(terms = terms + "banana,Banana-Prata,,to_confirm,no,,"))
        assertError("repete o nome de um produto", source(terms = terms + "apple,maçã,,to_confirm,no,,"))
        assertError("região \"southeast\"", source(terms = terms + "banana,prata,southeast,to_confirm,no,,"))
        assertError("status \"ok\"", source(terms = terms + "banana,prata,,ok,no,,"))
        assertError("ambiguous \"maybe\"", source(terms = terms + "banana,prata,,to_confirm,maybe,,"))
        assertError("regionalismo confirmed precisa de evidence", source(terms = terms + "banana,prata,north,confirmed,no,,"))
        assertError("aponta para produto retired", source(terms = terms + "bread_roll,pão de sal,,to_confirm,no,,"))
        assertError("repetido para \"apple\"", source(terms = terms + "apple,maçã gala,,to_confirm,no,,"))
    }

    @Test
    fun termEnteringTheAppForTwoProductsIsAnError() {
        val errors = errorsOf(source(terms = terms + "papaya,cacetinho,,to_confirm,no,,"))
        assertTrue(errors.any { "leva a \"papaya\" e a \"french_bread\"" in it }, "$errors")
    }

    @Test
    fun ambiguousTermWithCandidatesIsFineWhenOnlyOneEntersTheApp() {
        val result = validateCatalog(source(terms = terms + "banana,pão,,rejected,yes,,"))
        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun collectsEveryErrorInsteadOfStoppingAtTheFirst() {
        val errors = errorsOf(
            source(
                products = products + "ice,Gelo,frozen,10,,,active,," + "kiwi,Kiwi,produce,90,,exact,active,,",
                terms = terms + "kiwi2,quiuí,,to_confirm,no,,",
            ),
        )
        assertEquals(3, errors.size, "$errors")
        assertTrue(errors.all { it.matches(Regex("(products|terms)\\.csv:\\d+: .+")) }, "$errors")
    }
}
