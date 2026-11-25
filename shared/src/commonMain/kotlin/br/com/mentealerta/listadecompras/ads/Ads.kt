package br.com.mentealerta.listadecompras.ads

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * Momentos de pausa contados (plano §3.7): o anúncio vem a cada [EVERY] vezes, cada momento com o seu
 * contador. Os outros momentos (depois do **Terminei** e do **Jogar a lista fora**) não são contados.
 */
enum class AdMoment {
    /** Voltar da tela de adicionar para a lista. */
    BackFromAdd,

    /** Sair da edição de um produto com algo mudado. */
    Edit,
    ;

    companion object {
        const val EVERY = 3
    }
}

/** O que o app lembra sobre os anúncios entre uma vez e outra. */
data class AdState(
    /** Primeira vez que o app abriu: começa a primeira semana, sem anúncios. */
    val firstUse: Instant? = null,
    /** Quando apareceu o aviso do fim da primeira semana; os anúncios começam no dia seguinte. */
    val weekNoticeShownAt: Instant? = null,
    /** Quantas vezes cada [AdMoment] aconteceu desde o último anúncio dele (de 0 a [AdMoment.EVERY] - 1). */
    val counts: Map<AdMoment, Int> = emptyMap(),
    /** Chave "Anúncios pelo meu interesse" dos Ajustes; desligada, só anúncios não personalizados. */
    val personalized: Boolean = true,
)

/** Onde o [AdState] fica guardado. */
interface AdPreferences {
    val state: Flow<AdState>

    /** Muda o estado guardado e devolve como ficou. */
    suspend fun update(transform: (AdState) -> AdState): AdState
}

/**
 * Anúncio de tela cheia (intersticial) da plataforma. Nada da lista vai para o anúncio (plano §4.5):
 * ele só recebe se pode ser personalizado.
 */
interface InterstitialAds {
    /**
     * Deixa um anúncio pronto para mostrar na hora, sem espera. Na primeira vez, pede o consentimento
     * onde a lei exige. Sem internet ou sem anúncio, não faz nada.
     */
    fun preload(personalized: Boolean)

    /**
     * Mostra o anúncio pronto; `false` se não havia nenhum. Um anúncio pronto com outra
     * personalização (a chave dos Ajustes mudou depois) é jogado fora, sem aparecer.
     */
    fun show(personalized: Boolean): Boolean
}

/** Em que dia cai um instante, para contar a primeira semana. */
fun interface AdDays {
    fun dayOf(instant: Instant): Long

    companion object {
        /** Dias do calendário, no fuso do celular. */
        val Calendar = AdDays { it.toLocalDateTime(TimeZone.currentSystemDefault()).date.toEpochDays() }
    }
}
