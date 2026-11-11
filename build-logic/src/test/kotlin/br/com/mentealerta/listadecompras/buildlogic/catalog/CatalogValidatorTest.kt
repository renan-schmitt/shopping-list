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

    private fun errorsOf(source: CatalogSource): List<String> = when (val result = validateCatalog(source)) {
        is ValidationResult.Invalid -> result.errors
        is ValidationResult.Valid -> fail("Esperava erro, mas a planilha passou")
    }

    private fun assertProductError(expected: String, row: String) =
        assertError(expected, source(products = products + row))

    private fun assertTermError(expected: String, row: String) = assertError(expected, source(terms = terms + row))

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
        val badIcon = listOf("produce,Hortifrúti,1,Nope") + sections.drop(1)
        assertError("ícone Fluent \"Nope\"", source(sections = badIcon))
        assertError("falta a seção \"other\"", source(sections = sections.take(2)))
        assertError("\"other\" (Outros) precisa ser a última", source(sections = sections + "pet,Pet,4,Bread"))
        assertError("id \"Bakery\"", source(sections = listOf(sections[0], "Bakery,Padaria,2,Bread", sections[2])))
    }

    @Test
    fun productErrors() {
        assertProductError("produto \"banana\" repetido", "banana,Banana 2,produce,90,,,active,,")
        assertProductError("seção \"frozen\" não existe", "ice,Gelo,frozen,10,,,active,,")
        assertProductError("order 10 já usada por \"banana\"", "kiwi,Kiwi,produce,10,,,active,,")
        assertProductError("não é um número", "kiwi,Kiwi,produce,dez,,,active,,")
        assertProductError("ícone Fluent \"Kiwi fruit\"", "kiwi,Kiwi,produce,90,fluent:Kiwi fruit,exact,active,,")
        assertProductError("ícone próprio \"kiwi\"", "kiwi,Kiwi,produce,90,own:kiwi,exact,active,,")
        assertProductError("deve começar com fluent: ou own:", "kiwi,Kiwi,produce,90,Banana,exact,active,,")
        assertProductError("icon_fit preenchido sem ícone", "kiwi,Kiwi,produce,90,,exact,active,,")
        assertProductError("icon_fit deve ser exact ou approx", "kiwi,Kiwi,produce,90,fluent:Banana,,active,,")
        assertProductError("status \"old\"", "kiwi,Kiwi,produce,90,,,old,,")
        assertProductError("replaced_by só vale para produto retired", "kiwi,Kiwi,produce,90,,,active,banana,")
        assertProductError(
            "replaced_by \"bread_roll\" não é um produto active",
            "roll,Rosca,bakery,30,,,retired,bread_roll,",
        )
        assertProductError("nome \"banana\" já usado por \"banana\"", "banana2,banana,produce,90,,,active,,")
    }

    @Test
    fun ownIconThatExistsIsAccepted() {
        val kiwi = "kiwi,Kiwi,produce,90,own:kiwi,exact,active,,"
        val result = validateCatalog(source(products = products + kiwi, ownIcons = setOf("kiwi")))
        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun termErrors() {
        assertTermError("produto \"kiwi\" não existe", "kiwi,quiuí,,to_confirm,no,,")
        assertTermError("deve estar em minúsculas", "banana,Banana-Prata,,to_confirm,no,,")
        assertTermError("repete o nome de um produto", "apple,maçã,,to_confirm,no,,")
        assertTermError("região \"southeast\"", "banana,prata,southeast,to_confirm,no,,")
        assertTermError("status \"ok\"", "banana,prata,,ok,no,,")
        assertTermError("ambiguous \"maybe\"", "banana,prata,,to_confirm,maybe,,")
        assertTermError("regionalismo confirmed precisa de evidence", "banana,prata,north,confirmed,no,,")
        assertTermError("aponta para produto retired", "bread_roll,pão de sal,,to_confirm,no,,")
        assertTermError("repetido para \"apple\"", "apple,maçã gala,,to_confirm,no,,")
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
