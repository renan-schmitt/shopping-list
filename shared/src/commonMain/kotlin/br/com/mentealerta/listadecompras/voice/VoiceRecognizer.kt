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

    /** Sem internet e sem a voz baixada para usar sem internet. */
    data object NoInternet : VoiceEvent

    /** O microfone ou o serviço de voz não funcionaram (ocupado, sem permissão, outro erro). */
    data object Unavailable : VoiceEvent
}

/**
 * Reconhecimento de voz em pt-BR, offline quando o celular tiver a voz baixada (plano §3.3). Cada
 * plataforma tem o seu; no Android, o `SpeechRecognizer`.
 *
 * O texto reconhecido nunca vai para logs, crashes ou analytics (plano §4.5).
 */
interface VoiceRecognizer {
    /** `false` quando o celular não tem serviço de voz: o botão "Falar" nem aparece. */
    val isAvailable: Boolean

    /**
     * Abre o microfone e escuta, reabrindo-o sozinho a cada pausa, até [stop] ou um silêncio longo.
     * O fluxo termina quando a escuta acaba; cancelar a coleta fecha o microfone na hora.
     */
    fun listen(): Flow<VoiceEvent>

    /** "Terminei": fecha o microfone depois de entregar o último trecho. */
    fun stop()
}
