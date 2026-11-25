package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.prediction.PurchasePattern
import br.com.mentealerta.listadecompras.prediction.purchasePatterns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/** Produto que está faltando, com [daysSinceLastPurchase] para a frase "Faz 12 dias que você comprou café". */
data class OverdueProduct(val pattern: PurchasePattern, val daysSinceLastPurchase: Int)

/**
 * Produtos que estão faltando (plano §3.4, #63), do mais atrasado (em relação ao intervalo de cada
 * um) para o menos atrasado: a base de "Está faltando?" (#64). Usa o histórico inteiro, também na
 * versão grátis (plano §5), e deixa de fora o que já está na lista, riscado ou não, e o que a pessoa
 * dispensou com "Não" até o próximo ciclo ([DismissSuggestionUseCase], #64).
 *
 * Emite de novo quando o histórico, a lista ou os "Não" mudam; a virada do dia só conta na próxima
 * emissão.
 */
class ObserveOverdueProductsUseCase(
    private val history: PurchaseHistoryRepository,
    private val list: ShoppingListRepository,
    private val dismissals: SuggestionDismissalRepository,
    private val index: CatalogIndex,
    private val clock: Clock,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {
    operator fun invoke(): Flow<List<OverdueProduct>> = combine(
        history.observePurchases(since = null),
        list.observeSections(),
        dismissals.observeDismissals(),
    ) { purchases, sections, dismissed ->
        val today = clock.now().toLocalDateTime(timeZone).date
        val onList = sections.flatMap { it.items }.mapTo(HashSet()) { productKey(it.productId, it.name, index) }
        purchasePatterns(purchases, index, timeZone)
            .filter { pattern ->
                val dismissedOn = dismissed[pattern.key]?.toLocalDateTime(timeZone)?.date
                pattern.key !in onList && pattern.isOverdue(today, dismissedOn)
            }
            .sortedWith(compareByDescending<PurchasePattern> { it.overdueRatio(today) }.thenBy { it.name })
            .map { OverdueProduct(it, it.daysSinceLastPurchase(today)) }
    }
}
