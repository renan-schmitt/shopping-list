package br.com.mentealerta.listadecompras.ads

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import br.com.mentealerta.listadecompras.billing.CurrentActivity
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

/**
 * Intersticial do AdMob (plano §3.7, #89). Na primeira vez, pergunta ao UMP se precisa pedir
 * consentimento (só onde a lei exige; no Brasil não aparece) e só então sobe o SDK, com classificação
 * máxima G e o vídeo começando sem som. Deixa um anúncio pronto para mostrar sem espera; se não há
 * anúncio pronto na hora, não mostra nada. O limite de frequência fica no bloco [adUnitId], no AdMob.
 *
 * Tudo na thread principal: é onde o SDK espera ser chamado.
 */
class AdMobInterstitialAds(
    private val context: Context,
    private val currentActivity: CurrentActivity,
    private val adUnitId: String,
) : InterstitialAds {
    private val main = Handler(Looper.getMainLooper())
    private var sdk = Sdk.Stopped
    private var ready: Ready? = null
    private var loading = false

    override fun preload(personalized: Boolean) {
        main.post {
            when (sdk) {
                Sdk.Started -> load(personalized)
                Sdk.Starting -> Unit
                Sdk.Stopped -> currentActivity.get()?.let { start(it, personalized) }
            }
        }
    }

    override fun show(personalized: Boolean): Boolean {
        // A chave dos Ajustes mudou depois de carregar: esse anúncio não aparece.
        val current = ready?.takeIf { it.personalized == personalized }
        ready = null
        val activity = currentActivity.get()
        if (current == null || activity == null) return false
        current.ad.show(activity)
        return true
    }

    private fun start(activity: Activity, personalized: Boolean) {
        sdk = Sdk.Starting
        val consent = UserMessagingPlatform.getConsentInformation(activity)
        val afterConsent = {
            if (consent.canRequestAds()) {
                startSdk()
                load(personalized)
            } else {
                // Sem consentimento onde a lei exige: tenta de novo na próxima abertura.
                sdk = Sdk.Stopped
            }
        }
        consent.requestConsentInfoUpdate(
            activity,
            ConsentRequestParameters.Builder().build(),
            { UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { afterConsent() } },
            { afterConsent() },
        )
    }

    private fun startSdk() {
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
                .build(),
        )
        MobileAds.initialize(context)
        MobileAds.setAppMuted(true)
        sdk = Sdk.Started
    }

    private fun load(personalized: Boolean) {
        if (loading || ready?.personalized == personalized) return
        ready = null
        loading = true
        val request = AdRequest.Builder().apply {
            if (!personalized) {
                addNetworkExtrasBundle(AdMobAdapter::class.java, Bundle().apply { putString("npa", "1") })
            }
        }.build()
        InterstitialAd.load(
            context,
            adUnitId,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    loading = false
                    ready = Ready(ad, personalized)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loading = false
                }
            },
        )
    }

    private class Ready(val ad: InterstitialAd, val personalized: Boolean)

    private enum class Sdk { Stopped, Starting, Started }
}
