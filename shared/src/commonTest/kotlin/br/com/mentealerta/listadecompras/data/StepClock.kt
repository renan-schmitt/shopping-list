package br.com.mentealerta.listadecompras.data

import kotlin.time.Clock
import kotlin.time.Instant

/** Relógio que anda um segundo a cada leitura: cada item entra depois do anterior. */
internal class StepClock : Clock {
    private var seconds = 0L

    override fun now(): Instant = Instant.fromEpochSeconds(++seconds)
}
