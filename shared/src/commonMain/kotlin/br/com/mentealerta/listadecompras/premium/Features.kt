package br.com.mentealerta.listadecompras.premium

import br.com.mentealerta.listadecompras.domain.ObservePurchaseHistoryUseCase

/**
 * O que está liberado agora: ponto único para as telas e integrações perguntarem por uma
 * funcionalidade, e não por "é premium?". Quem tem a versão completa ganha tudo de [Feature], de hoje
 * e do futuro (plano §5).
 */
class Features private constructor(private val unlocked: Set<Feature>) {
    fun isUnlocked(feature: Feature): Boolean = feature in unlocked

    /** Quantos dias de "O que já comprei" mostrar; `null` é tudo. */
    val historyDays: Int?
        get() = if (isUnlocked(Feature.FullHistory)) null else ObservePurchaseHistoryUseCase.FREE_HISTORY_DAYS

    override fun equals(other: Any?): Boolean = other is Features && other.unlocked == unlocked

    override fun hashCode(): Int = unlocked.hashCode()

    override fun toString(): String = "Features($unlocked)"

    companion object {
        /** Versão grátis: nada de [Feature] liberado. */
        val Free = Features(emptySet())

        /** Versão completa: tudo de [Feature]. */
        val Complete = Features(Feature.entries.toSet())

        fun of(isPremium: Boolean): Features = if (isPremium) Complete else Free
    }
}
