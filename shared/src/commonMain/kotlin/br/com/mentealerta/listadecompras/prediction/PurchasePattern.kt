package br.com.mentealerta.listadecompras.prediction

import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.domain.Purchase
import br.com.mentealerta.listadecompras.domain.PurchasedItem
import br.com.mentealerta.listadecompras.domain.productKey
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

/**
 * De quanto em quanto tempo a pessoa compra um produto (plano §3.4, #62): só a frequência, sem a
 * quantidade, no MVP. Montado por [purchasePatterns] a partir do histórico.
 *
 * [key] junta os nomes do mesmo produto ([productKey]); [productId] e [name] são os da compra mais
 * recente, o nome que a pessoa usa ("Faz 12 dias que você comprou aipim"). [intervalDays] é a mediana
 * dos últimos intervalos e pode ter meio dia (mediana de 2 ou 4 intervalos).
 */
data class PurchasePattern(
    val key: String,
    val productId: String?,
    val name: String,
    val lastPurchase: LocalDate,
    val intervalDays: Double,
) {
    /** Dias do calendário desde a última compra; com o relógio do celular atrasado, 0. */
    fun daysSinceLastPurchase(today: LocalDate): Int = lastPurchase.daysUntil(today).coerceAtLeast(0)

    /** Quanto o produto está atrasado: 1 é o intervalo de costume; serve para pôr o mais atrasado antes. */
    fun overdueRatio(today: LocalDate): Double = daysSinceLastPurchase(today) / intervalDays

    /**
     * Se a pessoa ainda compra o produto em [today]: não passou de [STOP_FACTOR] × o intervalo desde a
     * última compra. Depois disso ela deixou de usar, e o produto só volta quando ela comprar.
     */
    fun isActive(today: LocalDate): Boolean = daysSinceLastPurchase(today) <= intervalDays * STOP_FACTOR

    /**
     * Se o produto está faltando em [today]: passou de 1,2 × o intervalo (folga de 20%) desde a última
     * compra e ainda não de [STOP_FACTOR] × (aí a pessoa deixou de usar; volta quando ela comprar).
     *
     * Um "Não" em [dismissedOn] esconde o produto até passar mais um intervalo inteiro, contado do
     * "Não"; um "Não" de antes da última compra não vale mais (comprar recomeça a contagem).
     */
    fun isOverdue(today: LocalDate, dismissedOn: LocalDate? = null): Boolean {
        val days = daysSinceLastPurchase(today)
        // dias > 1,2 × intervalo, em quintos para não cair em erro de arredondamento.
        val overdue = days * FIFTHS > intervalDays * OVERDUE_FIFTHS && isActive(today)
        val dismissal = dismissedOn?.takeIf { it >= lastPurchase }
        return overdue && (dismissal == null || dismissal.daysUntil(today) >= intervalDays)
    }

    companion object {
        /** Compras do produto (em dias diferentes) antes da primeira sugestão. */
        const val MIN_PURCHASES = 3

        /** Quantos intervalos, dos mais recentes, entram na mediana. */
        const val MAX_INTERVALS = 5

        /** Intervalo mais curto que gera sugestão: pão de todo dia não é sugerido. */
        const val MIN_INTERVAL_DAYS = 2

        /** Depois de tantos intervalos sem comprar, para de sugerir. */
        const val STOP_FACTOR = 3

        /** Folga antes de o produto estar faltando, 20% a mais que o intervalo: 1,2 = 6/5. */
        private const val OVERDUE_FIFTHS = 6
        private const val FIFTHS = 5
    }
}

/**
 * O padrão de compra de cada produto do histórico ([purchases], em qualquer ordem), contado em dias
 * do calendário no fuso do celular ([timeZone]): duas compras do mesmo produto no mesmo dia valem
 * uma. Ficam de fora os produtos com menos de [PurchasePattern.MIN_PURCHASES] compras e os de
 * intervalo menor que [PurchasePattern.MIN_INTERVAL_DAYS] dias.
 */
fun purchasePatterns(purchases: List<Purchase>, index: CatalogIndex, timeZone: TimeZone): List<PurchasePattern> {
    val byProduct = LinkedHashMap<String, MutableList<DatedItem>>()
    purchases.sortedBy { it.date }.forEach { purchase ->
        val day = purchase.date.toLocalDateTime(timeZone).date
        purchase.items.forEach { item ->
            byProduct.getOrPut(productKey(item.productId, item.name, index)) {
                mutableListOf()
            }.add(DatedItem(day, item))
        }
    }
    return byProduct.mapNotNull { (key, items) -> pattern(key, items) }
}

/** Produto comprado com o dia da compra. */
private class DatedItem(val day: LocalDate, val item: PurchasedItem)

/** O padrão do produto [key] a partir das compras dele, da mais antiga para a mais recente. */
private fun pattern(key: String, items: List<DatedItem>): PurchasePattern? {
    val days = items.map { it.day }.distinct()
    if (days.size < PurchasePattern.MIN_PURCHASES) return null
    val interval = days.zipWithNext { a, b -> a.daysUntil(b) }.takeLast(PurchasePattern.MAX_INTERVALS).median()
    if (interval < PurchasePattern.MIN_INTERVAL_DAYS) return null
    val latest = items.last().item
    return PurchasePattern(key, latest.productId, latest.name, days.last(), interval)
}

/** Mediana: o do meio ou, com número par, a média dos dois do meio. */
private fun List<Int>.median(): Double {
    val sorted = sorted()
    val middle = sorted.size / 2
    return if (sorted.size % 2 == 1) sorted[middle].toDouble() else (sorted[middle - 1] + sorted[middle]) / 2.0
}
