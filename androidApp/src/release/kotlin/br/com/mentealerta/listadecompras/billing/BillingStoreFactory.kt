package br.com.mentealerta.listadecompras.billing

import android.content.Context
import br.com.mentealerta.listadecompras.premium.BillingStore

/** Build release: a loja de verdade (Google Play Billing). No debug, uma loja falsa. */
fun createBillingStore(context: Context, currentActivity: CurrentActivity): BillingStore =
    PlayBillingStore(context, currentActivity)
