package br.com.mentealerta.listadecompras.data

import br.com.mentealerta.listadecompras.data.database.PurchaseDao
import br.com.mentealerta.listadecompras.data.database.toDomain
import br.com.mentealerta.listadecompras.domain.Purchase
import br.com.mentealerta.listadecompras.domain.PurchaseHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Instant

/** Histórico de compras no banco local (plano §4.3), gravado pelo [RoomShoppingListRepository.finishShopping]. */
internal class RoomPurchaseHistoryRepository(private val dao: PurchaseDao) : PurchaseHistoryRepository {
    override fun observePurchases(since: Instant?): Flow<List<Purchase>> =
        (if (since == null) dao.observeAll() else dao.observeSince(since))
            .map { purchases -> purchases.map { it.toDomain() } }
}
