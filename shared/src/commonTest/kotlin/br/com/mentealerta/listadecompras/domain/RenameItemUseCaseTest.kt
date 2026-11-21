package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.catalog.CatalogSection
import br.com.mentealerta.listadecompras.catalog.EmbeddedCatalog
import br.com.mentealerta.listadecompras.normalization.ItemTextParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/** Trocar o nome na edição (#55): o nome fica como a pessoa escreveu; outro produto conhecido muda o produto. */
class RenameItemUseCaseTest {
    private val renameItem = RenameItemUseCase(ItemTextParser(CatalogIndex(EmbeddedCatalog.catalog)))
    private val milk = ListItem.create(
        name = "leite",
        productId = "milk",
        sectionId = "deli_dairy",
        detail = "desnatado",
    )

    @Test
    fun anotherKnownProductChangesProductAndSection() {
        val renamed = renameItem(milk, " Pão francês ")
        assertEquals("Pão francês", renamed.name)
        assertEquals("french_bread", renamed.productId)
        assertEquals("bakery", renamed.sectionId)
        assertEquals(milk.id, renamed.id)
        assertEquals("desnatado", renamed.detail)
    }

    @Test
    fun sameProductKeepsTheSectionThePersonChose() {
        val moved = milk.copy(sectionId = CatalogSection.OTHER_ID)
        val renamed = renameItem(moved, "leite de caixinha")
        assertEquals("leite de caixinha", renamed.name)
        assertEquals("milk", renamed.productId)
        assertEquals(CatalogSection.OTHER_ID, renamed.sectionId)
    }

    @Test
    fun unknownNameKeepsProductAndSection() {
        val renamed = renameItem(milk, "xyzzy")
        assertEquals("xyzzy", renamed.name)
        assertEquals("milk", renamed.productId)
        assertEquals("deli_dairy", renamed.sectionId)
    }

    @Test
    fun blankOrSameNameChangesNothing() {
        assertSame(milk, renameItem(milk, "   "))
        assertSame(milk, renameItem(milk, " leite "))
    }
}
