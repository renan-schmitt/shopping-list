package br.com.mentealerta.listadecompras.ui.catalog

import androidx.annotation.DrawableRes
import br.com.mentealerta.listadecompras.catalog.CatalogIcon
import br.com.mentealerta.listadecompras.catalog.CatalogProduct
import br.com.mentealerta.listadecompras.catalog.CatalogSection

/**
 * Recurso drawable do ícone, convertido dos SVG de `catalog/` no build (#22). `null` só se o SVG
 * não existir, o que a validação da planilha já impede.
 */
@DrawableRes
fun CatalogIcon.drawableRes(): Int? = when (this) {
    is CatalogIcon.Fluent -> CatalogIconResources.fluent(name)
    is CatalogIcon.Own -> CatalogIconResources.own(id)
}

/** Ícone do produto ou, quando ele não tem ícone próprio, o da [section] (plano §4.4). */
@DrawableRes
fun CatalogProduct.iconRes(section: CatalogSection): Int = icon?.drawableRes()
    ?: checkNotNull(section.icon.drawableRes()) { "Seção \"${section.id}\" sem ícone convertido" }
