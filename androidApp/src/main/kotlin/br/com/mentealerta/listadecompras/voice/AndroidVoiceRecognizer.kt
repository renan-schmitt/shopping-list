package br.com.mentealerta.listadecompras.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

/** Silêncio que encerra a escuta (plano §3.3, valor provisório: medir no celular de entrada). */
private const val SILENCE_LIMIT_MILLIS = 8_000L

/** Alternativas pedidas para cada trecho; fica a que mais bate com o catálogo. */
private const val MAX_ALTERNATIVES = 5

// Códigos do Android 12+ (API 31), repetidos aqui porque o minSdk é 26
private const val ERROR_SERVER_DISCONNECTED = 11
private const val ERROR_LANGUAGE_UNAVAILABLE = 13

/**
 * [VoiceRecognizer] com o `SpeechRecognizer` do serviço de voz padrão do celular (quase sempre o do
 * Google), em pt-BR e pedindo offline (plano §3.3). Sem a voz baixada, o serviço usa a internet.
 *
 * O serviço encerra cada escuta numa pausa curta. Para a pessoa falar com calma, o microfone reabre
 * sozinho depois de cada trecho, até "Terminei" ([stop]) ou [SILENCE_LIMIT_MILLIS] sem ouvir nada.
 *
 * O `SpeechRecognizer` só funciona na thread principal: o fluxo roda nela, e [stop] também deve ser
 * chamado nela. Nada do que é ouvido vai para o log.
 */
class AndroidVoiceRecognizer(private val context: Context) : VoiceRecognizer {
    override val isAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    private var stopCurrent: (() -> Unit)? = null

    override fun listen(): Flow<VoiceEvent> = callbackFlow {
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        var stopped = false
        var lastHeardAt = SystemClock.elapsedRealtime()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, LANGUAGE)
            .putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, MAX_ALTERNATIVES)

        fun silentTooLong() = SystemClock.elapsedRealtime() - lastHeardAt >= SILENCE_LIMIT_MILLIS

        fun listenAgainOrFinish() {
            if (stopped || silentTooLong()) close() else recognizer.startListening(intent)
        }

        recognizer.setRecognitionListener(
            object : RecognitionListener {
                override fun onPartialResults(partialResults: Bundle) {
                    val text = partialResults.texts().firstOrNull() ?: return
                    lastHeardAt = SystemClock.elapsedRealtime()
                    trySend(VoiceEvent.Partial(text))
                }

                override fun onResults(results: Bundle) {
                    val alternatives = results.texts()
                    if (alternatives.isNotEmpty()) {
                        lastHeardAt = SystemClock.elapsedRealtime()
                        trySend(VoiceEvent.Heard(alternatives))
                    }
                    listenAgainOrFinish()
                }

                override fun onError(error: Int) {
                    when (error) {
                        // Pausa sem fala: reabre, até o silêncio longo
                        SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> listenAgainOrFinish()

                        SpeechRecognizer.ERROR_NETWORK,
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
                        SpeechRecognizer.ERROR_SERVER,
                        ERROR_SERVER_DISCONNECTED,
                        ERROR_LANGUAGE_UNAVAILABLE,
                        -> {
                            trySend(VoiceEvent.NoInternet)
                            close()
                        }

                        else -> {
                            // Depois do "Terminei", o serviço às vezes reclama da escuta encerrada
                            if (!stopped) trySend(VoiceEvent.Unavailable)
                            close()
                        }
                    }
                }

                override fun onReadyForSpeech(params: Bundle?) = Unit

                override fun onBeginningOfSpeech() = Unit

                override fun onRmsChanged(rmsdB: Float) = Unit

                override fun onBufferReceived(buffer: ByteArray?) = Unit

                override fun onEndOfSpeech() = Unit

                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            },
        )
        stopCurrent = {
            stopped = true
            recognizer.stopListening()
        }
        recognizer.startListening(intent)
        awaitClose {
            stopCurrent = null
            recognizer.destroy()
        }
    }.flowOn(Dispatchers.Main.immediate)

    override fun stop() {
        stopCurrent?.invoke()
    }

    private fun Bundle.texts(): List<String> =
        getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty().filter { it.isNotBlank() }

    private companion object {
        const val LANGUAGE = "pt-BR"
    }
}
