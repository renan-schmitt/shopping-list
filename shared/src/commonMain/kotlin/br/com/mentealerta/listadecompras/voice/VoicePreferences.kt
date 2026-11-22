package br.com.mentealerta.listadecompras.voice

/** O que o app lembra sobre a voz entre uma vez e outra. */
interface VoicePreferences {
    /** Se a explicação do microfone (e do envio do som ao Google) já apareceu (plano §3.3). */
    suspend fun wasNoticeShown(): Boolean

    suspend fun setNoticeShown()
}
