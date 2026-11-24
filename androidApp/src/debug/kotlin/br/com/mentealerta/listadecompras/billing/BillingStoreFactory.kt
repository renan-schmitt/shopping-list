package br.com.mentealerta.listadecompras.billing

import android.content.Context
import br.com.mentealerta.listadecompras.premium.BillingStore

/**
 * Build debug: loja falsa. O produto só existe para um app enviado ao Play Console (faixa de teste);
 * o build release usa o [PlayBillingStore] (docs/configurar-compras.md).
 */
@Suppress("UnusedParameter")
fun createBillingStore(context: Context, currentActivity: CurrentActivity): BillingStore = DebugBillingStore(context)
