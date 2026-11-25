package br.com.mentealerta.listadecompras.ads

/**
 * Build debug: cada minuto conta como um dia, para ver o aviso do fim da primeira semana e os
 * anúncios de teste sem esperar uma semana. No release, dias do calendário.
 */
fun createAdDays(): AdDays = AdDays { it.epochSeconds / SECONDS_PER_MINUTE }

private const val SECONDS_PER_MINUTE = 60
