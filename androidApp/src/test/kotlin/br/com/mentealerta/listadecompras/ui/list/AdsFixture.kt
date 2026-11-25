package br.com.mentealerta.listadecompras.ui.list

import br.com.mentealerta.listadecompras.ads.AdDays
import br.com.mentealerta.listadecompras.ads.AdPreferences
import br.com.mentealerta.listadecompras.ads.AdState
import br.com.mentealerta.listadecompras.ads.InterstitialAds
import br.com.mentealerta.listadecompras.domain.AdsUseCases
import br.com.mentealerta.listadecompras.domain.ObserveFeaturesUseCase
import br.com.mentealerta.listadecompras.domain.ShoppingListRepository
import br.com.mentealerta.listadecompras.premium.PremiumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/**
 * Anúncios (#89) com preferências e anúncio falsos e um relógio que a gente avança. A lista
 * ([list]) diz se há produto riscado; [premium] diz se a versão completa foi comprada.
 */
class AdsFixture(premium: PremiumRepository, list: ShoppingListRepository) {
    val preferences = FakeAdPreferences()
    val ads = FakeInterstitialAds()
    var now: Instant = Instant.parse("2026-11-01T10:00:00Z")
    private val clock = object : Clock {
        override fun now(): Instant = now
    }

    val useCases = AdsUseCases(
        preferences = preferences,
        features = ObserveFeaturesUseCase(premium),
        list = list,
        ads = ads,
        clock = clock,
        days = AdDays { it.epochSeconds / 1.days.inWholeSeconds },
    )

    /** O app abriu pela primeira vez há [elapsed]. */
    fun firstOpenedAgo(elapsed: Duration) {
        preferences.set(AdState(firstUse = now - elapsed))
    }

    /** O aviso do fim da primeira semana já apareceu ontem: os anúncios já começaram. */
    fun startAds() {
        preferences.set(AdState(firstUse = now - 8.days, weekNoticeShownAt = now - 1.days))
    }
}

class FakeAdPreferences : AdPreferences {
    private val flow = MutableStateFlow(AdState())
    val current: AdState get() = flow.value

    override val state: Flow<AdState> = flow

    override suspend fun update(transform: (AdState) -> AdState): AdState = flow.updateAndGet(transform)

    fun set(state: AdState) {
        flow.value = state
    }
}

/** Sempre tem anúncio pronto; guarda o que foi pedido. */
class FakeInterstitialAds : InterstitialAds {
    val preloads = mutableListOf<Boolean>()
    val shows = mutableListOf<Boolean>()

    override fun preload(personalized: Boolean) {
        preloads += personalized
    }

    override fun show(personalized: Boolean): Boolean {
        shows += personalized
        return true
    }
}
