package br.com.mentealerta.listadecompras.domain

import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

/**
 * O histórico de compras ("O que já comprei"), gravado pelo "Terminei" e guardado só no celular. A
 * implementação fica em `data`; quem usa recebe esta interface pelo Koin.
 */
interface PurchaseHistoryRepository {
    /**
     * Compras feitas a partir de [since] (`null`: todas), da mais recente para a mais antiga, com os
     * produtos na ordem em que estavam. Emite de novo a cada mudança.
     */
    fun observePurchases(since: Instant?): Flow<List<Purchase>>
}
