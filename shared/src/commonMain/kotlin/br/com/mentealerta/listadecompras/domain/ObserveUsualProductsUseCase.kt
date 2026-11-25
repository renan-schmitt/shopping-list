package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.catalog.CatalogIndex
import br.com.mentealerta.listadecompras.prediction.PurchasePattern
import br.com.mentealerta.listadecompras.prediction.purchasePatterns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/** Produto de costume, com o [name] que a pessoa usa para ele: o termo preferido ou o da última compra. */
data class UsualProduct(val pattern: PurchasePattern, val name: String)

/**
 * "Você costuma comprar" na tela de adicionar (plano §1.2 e §3.4, #65): os produtos que a pessoa
 * compra com regularidade e ainda não estão na lista, riscados ou não. Vale a mesma regra do "Está
 * faltando?" para confiar no padrão (3 compras, intervalo de 2 dias ou mais) e para parar de sugerir
 * ([PurchasePattern.isActive]), mas o produto não precisa estar faltando: aqui a pessoa está
 * escolhendo. Por isso o "Não" do "Está faltando?" não esconde nada aqui.
 *
 * Vem do mais perto de acabar (em relação ao intervalo de cada um) para o menos, no máximo [LIMIT].
 * O nome é o termo preferido da pessoa ([PreferredTermRepository], #44) ou, sem ele, o da última
 * compra.
 */
class ObserveUsualProductsUseCase(
    private val history: PurchaseHistoryRepository,
    private val list: ShoppingListRepository,
    private val preferredTerms: PreferredTermRepository,
    private val index: CatalogIndex,
    private val clock: Clock,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {
    operator fun invoke(): Flow<List<UsualProduct>> = combine(
        history.observePurchases(since = null),
        list.observeSections(),
    ) { purchases, sections ->
        val today = clock.now().toLocalDateTime(timeZone).date
        val onList = sections.flatMap { it.items }.mapTo(HashSet()) { productKey(it.productId, it.name, index) }
        val patterns = purchasePatterns(purchases, index, timeZone)
            .filter { it.key !in onList && it.isActive(today) }
            .sortedWith(compareByDescending<PurchasePattern> { it.overdueRatio(today) }.thenBy { it.name })
            .take(LIMIT)
        val terms = preferredTerms.preferredTerms(patterns.mapNotNull { it.productId })
        patterns.map { UsualProduct(it, it.productId?.let(terms::get) ?: it.name) }
    }

    companion object {
        /** Quantos produtos aparecem, para a tela não virar uma lista longa com a fonte grande. */
        const val LIMIT = 8
    }
}
