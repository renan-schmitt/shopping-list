package br.com.mentealerta.listadecompras.ui.catalog

import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.catalog.CatalogIcon
import br.com.mentealerta.listadecompras.catalog.CatalogProduct
import br.com.mentealerta.listadecompras.catalog.CatalogSection
import br.com.mentealerta.listadecompras.catalog.IconFit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CatalogIconsTest {
    private val produce = CatalogSection("produce", "Hortifrúti", 1, CatalogIcon.Fluent("Leafy green"))

    private fun product(icon: CatalogIcon?) =
        CatalogProduct("banana", "Banana", "produce", 10, icon, if (icon == null) null else IconFit.EXACT)

    @Test
    fun productWithIconUsesItsOwnIcon() {
        assertEquals(R.drawable.catalog_fluent_banana, product(CatalogIcon.Fluent("Banana")).iconRes(produce))
    }

    @Test
    fun productWithoutIconUsesTheSectionIcon() {
        assertEquals(R.drawable.catalog_fluent_leafy_green, product(null).iconRes(produce))
    }

    @Test
    fun iconWithoutSvgHasNoResource() {
        assertNull(CatalogIcon.Fluent("Não existe").drawableRes())
        assertNull(CatalogIcon.Own("nao_existe").drawableRes())
    }

    @Test
    fun skinToneIconUsesTheDefaultTone() {
        assertEquals(R.drawable.catalog_fluent_nail_polish, CatalogIcon.Fluent("Nail polish").drawableRes())
    }
}
