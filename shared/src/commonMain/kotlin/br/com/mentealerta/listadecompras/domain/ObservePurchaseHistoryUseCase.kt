package br.com.mentealerta.listadecompras.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/** Compra do histórico com há quantos dias ela foi feita: 0 é hoje, 1 é ontem. */
data class PastPurchase(val purchase: Purchase, val daysAgo: Int)

/**
 * O que "O que já comprei" mostra: as [purchases] da mais recente para a mais antiga e se há compras
 * mais antigas guardadas que a versão grátis não mostra ([hasOlder], #90).
 */
data class PurchaseHistory(val purchases: List<PastPurchase>, val hasOlder: Boolean)

/**
 * "O que já comprei" (#66, plano §1.5): as compras da mais recente para a mais antiga, cada uma com
 * há quantos dias foi feita, contados em dias do calendário no fuso do celular (a compra de ontem às
 * 23h é "ontem" mesmo hoje à 1h). A tela monta a frase ("ontem", "há 12 dias").
 *
 * Grátis, só os últimos [FREE_HISTORY_DAYS] dias (plano §5): de hoje até há 30 dias; as compras mais
 * antigas continuam no banco. A versão completa vê tudo, na hora em que a compra é liberada (#90).
 */
class ObservePurchaseHistoryUseCase(
    private val repository: PurchaseHistoryRepository,
    private val observeFeatures: ObserveFeaturesUseCase,
    private val clock: Clock,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<PurchaseHistory> = observeFeatures()
        .map { it.historyDays }
        .flatMapLatest(::observe)

    private fun observe(historyDays: Int?): Flow<PurchaseHistory> {
        val today = clock.now().toLocalDateTime(timeZone).date
        val since = historyDays?.let { today.minus(it, DateTimeUnit.DAY).atStartOfDayIn(timeZone) }
        val purchases = repository.observePurchases(since).map { purchases ->
            purchases.map { purchase ->
                val day = purchase.date.toLocalDateTime(timeZone).date
                // Relógio do celular atrasado: a compra "do futuro" fica como de hoje.
                PastPurchase(purchase, daysAgo = day.daysUntil(today).coerceAtLeast(0))
            }
        }
        val hasOlder = since?.let(repository::observeHasPurchasesBefore) ?: flowOf(false)
        return combine(purchases, hasOlder, ::PurchaseHistory)
    }

    companion object {
        /** Quantos dias de histórico a versão grátis mostra (plano §5). */
        const val FREE_HISTORY_DAYS = 30
    }
}
