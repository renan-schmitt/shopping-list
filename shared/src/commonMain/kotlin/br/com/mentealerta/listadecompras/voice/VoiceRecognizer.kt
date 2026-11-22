package br.com.mentealerta.listadecompras.voice

import kotlinx.coroutines.flow.Flow

/** O que o reconhecedor de voz informa enquanto a pessoa fala (plano §3.3). */
sealed interface VoiceEvent {
    /** Texto aparecendo enquanto a pessoa fala, ainda sem a pausa. */
    data class Partial(val text: String) : VoiceEvent

    /**
     * Um trecho entendido, entre duas pausas, com até 5 [alternatives] da mais provável para a menos.
     * Cada trecho é separado dos outros por conta própria.
     */
    data class Heard(val alternatives: List<String>) : VoiceEvent

    /** O serviço de voz padrão precisou da internet e ela não está lá (erro de rede de verdade). */
    data object NoInternet : VoiceEvent

    /**
     * O reconhecedor do aparelho não tem a voz pt-BR: daqui em diante a escuta usa o serviço padrão,
     * que pode enviar o som ao Google (#123). Nada foi ouvido nesta vez.
     */
    data object SwitchedToService : VoiceEvent

    /** O microfone ou o serviço de voz não funcionaram (ocupado, sem permissão, outro erro). */
    data object Unavailable : VoiceEvent
}

/**
 * Reconhecimento de voz em pt-BR (plano §3.3). No Android 12+, o reconhecedor do próprio celular,
 * sem internet; sem ele, o serviço de voz padrão, que pode enviar o som ao Google (#123).
 *
 * O texto reconhecido nunca vai para logs, crashes ou analytics (plano §4.5).
 */
interface VoiceRecognizer {
    /** `false` quando o celular não tem serviço de voz: o botão "Falar" nem aparece. */
    val isAvailable: Boolean

    /**
     * `true` quando a escuta vai pelo serviço de voz padrão, que pode enviar o som ao Google: só
     * então aparece o aviso antes do microfone (#123).
     */
    suspend fun mayUseInternet(): Boolean

    /**
     * Abre o microfone e escuta, seguindo depois de cada pausa, até [stop]. Com [stopOnSilence]
     * (quem usa o TalkBack toca para abrir e para fechar), um silêncio longo também fecha. O fluxo
     * termina quando a escuta acaba; cancelar a coleta fecha o microfone na hora.
     */
    fun listen(stopOnSilence: Boolean = false): Flow<VoiceEvent>

    /** Soltar o botão ou "Terminei": fecha o microfone depois de entregar o último trecho. */
    fun stop()
}
