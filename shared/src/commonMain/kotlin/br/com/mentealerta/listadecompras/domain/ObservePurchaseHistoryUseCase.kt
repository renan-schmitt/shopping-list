package br.com.mentealerta.listadecompras.domain

import kotlinx.coroutines.flow.Flow
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
 * "O que já comprei" (#66, plano §1.5): as compras da mais recente para a mais antiga, cada uma com
 * há quantos dias foi feita, contados em dias do calendário no fuso do celular (a compra de ontem às
 * 23h é "ontem" mesmo hoje à 1h). A tela monta a frase ("ontem", "há 12 dias").
 *
 * Grátis, só os últimos [FREE_HISTORY_DAYS] dias (plano §5): de hoje até há 30 dias. O premium
 * (fase 2) passa `historyDays = null` para ver tudo. As compras mais antigas continuam no banco.
 */
class ObservePurchaseHistoryUseCase(
    private val repository: PurchaseHistoryRepository,
    private val clock: Clock,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {
    operator fun invoke(historyDays: Int? = FREE_HISTORY_DAYS): Flow<List<PastPurchase>> {
        val today = clock.now().toLocalDateTime(timeZone).date
        val since = historyDays?.let { today.minus(it, DateTimeUnit.DAY).atStartOfDayIn(timeZone) }
        return repository.observePurchases(since).map { purchases ->
            purchases.map { purchase ->
                val day = purchase.date.toLocalDateTime(timeZone).date
                // Relógio do celular atrasado: a compra "do futuro" fica como de hoje.
                PastPurchase(purchase, daysAgo = day.daysUntil(today).coerceAtLeast(0))
            }
        }
    }

    companion object {
        /** Quantos dias de histórico a versão grátis mostra (plano §5). */
        const val FREE_HISTORY_DAYS = 30
    }
}
