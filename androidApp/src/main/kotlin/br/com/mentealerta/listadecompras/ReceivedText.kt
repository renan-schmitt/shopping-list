package br.com.mentealerta.listadecompras

import android.content.Intent

/**
 * A lista que chegou pelo "Compartilhar" do Android (#135): o texto de um [Intent.ACTION_SEND] de
 * texto. `null` para os outros intents, para texto vazio e quando o app é reaberto pelos recentes
 * (o Android entrega de novo o intent antigo). O texto nunca vai para log, crash ou analytics.
 */
fun Intent.receivedText(): String? {
    if (action != Intent.ACTION_SEND || type?.startsWith("text/") != true) return null
    if (flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0) return null
    return getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()?.takeIf { it.isNotBlank() }
}
