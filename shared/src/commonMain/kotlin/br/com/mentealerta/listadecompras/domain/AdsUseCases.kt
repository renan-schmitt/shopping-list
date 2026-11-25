package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.ads.AdDays
import br.com.mentealerta.listadecompras.ads.AdMoment
import br.com.mentealerta.listadecompras.ads.AdPreferences
import br.com.mentealerta.listadecompras.ads.AdState
import br.com.mentealerta.listadecompras.ads.InterstitialAds
import br.com.mentealerta.listadecompras.premium.Feature
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Anúncios da versão grátis (plano §3.7, #89): intersticiais nos momentos de pausa, nunca durante a
 * compra (com produto riscado), nunca na primeira semana e nunca com a versão completa.
 *
 * A primeira semana conta da primeira vez que o app abre. A partir do 7º dia, na primeira vez que a
 * pessoa abre o app sem produto riscado, aparece o aviso "A partir de amanhã…", uma vez só; os
 * anúncios começam no dia seguinte ao aviso, para o "amanhã" ser verdade mesmo se a pessoa ficou dias
 * sem abrir. O limite de 1 a cada 5 minutos e 5 por dia fica no bloco do AdMob, não aqui.
 */
class AdsUseCases(
    private val preferences: AdPreferences,
    private val features: ObserveFeaturesUseCase,
    private val list: ShoppingListRepository,
    private val ads: InterstitialAds,
    private val clock: Clock,
    private val days: AdDays,
) {
    /**
     * O app abriu (ou voltou para a frente): começa a contar a primeira semana e, se já pode haver
     * anúncio, deixa um pronto. Devolve se o aviso do fim da primeira semana deve aparecer agora; quem
     * mostra chama [onWeekNoticeShown].
     */
    suspend fun onAppOpened(): Boolean {
        val now = clock.now()
        val state = preferences.update { if (it.firstUse == null) it.copy(firstUse = now) else it }
        if (hasNoAds()) return false
        if (adsStarted(state, now)) {
            ads.preload(state.personalized)
            return false
        }
        return weekNoticeDue(state, now) && !isShopping()
    }

    /** O aviso do fim da primeira semana apareceu: não aparece de novo, e os anúncios começam amanhã. */
    suspend fun onWeekNoticeShown() {
        val now = clock.now()
        preferences.update { if (it.weekNoticeShownAt == null) it.copy(weekNoticeShownAt = now) else it }
    }

    /**
     * Conta o [moment] e devolve se chegou a vez do anúncio (a cada [AdMoment.EVERY]). Na vez, o
     * contador volta a zero mesmo que o anúncio não possa aparecer: é "a cada 3", ao pé da letra.
     */
    suspend fun count(moment: AdMoment): Boolean {
        if (hasNoAds()) return false
        var due = false
        preferences.update { state ->
            val count = (state.counts[moment] ?: 0) + 1
            due = count >= AdMoment.EVERY
            state.copy(counts = state.counts + (moment to if (due) 0 else count))
        }
        return due
    }

    /**
     * Se um anúncio pode aparecer agora: versão grátis, depois da primeira semana e sem produto
     * riscado. Conferir antes do **Desfazer**: os riscados que ele devolve não são "estar comprando".
     */
    suspend fun canShow(): Boolean {
        if (hasNoAds()) return false
        return adsStarted(preferences.state.first(), clock.now()) && !isShopping()
    }

    /** Mostra o anúncio pronto, se houver, e já deixa o próximo carregando. */
    suspend fun show() {
        val personalized = preferences.state.first().personalized
        ads.show(personalized)
        ads.preload(personalized)
    }

    private fun hasNoAds(): Boolean = features.current().isUnlocked(Feature.NoAds)

    private fun weekNoticeDue(state: AdState, now: Instant): Boolean {
        val firstUse = state.firstUse ?: return false
        return state.weekNoticeShownAt == null && days.dayOf(now) - days.dayOf(firstUse) >= WEEK_NOTICE_DAY - 1
    }

    private suspend fun isShopping(): Boolean =
        list.observeSections().first().any { section -> section.items.any { it.checked } }

    private fun adsStarted(state: AdState, now: Instant): Boolean =
        state.weekNoticeShownAt?.let { days.dayOf(now) > days.dayOf(it) } == true

    companion object {
        /** Dia do aviso do fim da primeira semana, contando a primeira vez que o app abriu como dia 1. */
        const val WEEK_NOTICE_DAY = 7
    }
}

/** Chave "Anúncios pelo meu interesse" dos Ajustes (plano §3.7); ligada por padrão. */
class ObservePersonalizedAdsUseCase(private val preferences: AdPreferences) {
    operator fun invoke(): Flow<Boolean> = preferences.state.map { it.personalized }.distinctUntilChanged()
}

/**
 * Liga ou desliga a chave "Anúncios pelo meu interesse". Desligada, o app pede só anúncios não
 * personalizados; um anúncio que já estava pronto com a outra escolha não aparece.
 */
class SetPersonalizedAdsUseCase(private val preferences: AdPreferences) {
    suspend operator fun invoke(personalized: Boolean) {
        preferences.update { it.copy(personalized = personalized) }
    }
}
