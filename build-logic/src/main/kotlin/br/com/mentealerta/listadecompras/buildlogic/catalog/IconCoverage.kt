package br.com.mentealerta.listadecompras.buildlogic.catalog

/** Quantos produtos têm ícone fiel, aproximado ou nenhum (usam o da seção). */
data class IconCoverage(val exact: Int, val approx: Int, val none: Int) {
    val total: Int get() = exact + approx + none

    /** Percentual inteiro de [count] sobre o [total]. */
    fun percent(count: Int): Int = if (total == 0) 0 else Math.round(count * PERCENT / total.toDouble()).toInt()
}

private const val PERCENT = 100

/** Cobertura dos produtos ativos. */
fun iconCoverage(products: List<Product>): IconCoverage = IconCoverage(
    exact = products.count { it.iconFit == "exact" },
    approx = products.count { it.iconFit == "approx" },
    none = products.count { it.icon == null },
)
