package br.com.mentealerta.listadecompras.voice

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.speech.RecognitionListener
import android.speech.RecognitionSupport
import android.speech.RecognitionSupportCallback
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/** Silêncio que encerra a escuta no modo do TalkBack (plano §3.3, valor provisório). */
private const val SILENCE_LIMIT_MILLIS = 8_000L

/** Alternativas pedidas para cada trecho; fica a que mais bate com o catálogo. */
private const val MAX_ALTERNATIVES = 5

// Códigos do Android 12+ (API 31), repetidos aqui porque o minSdk é 26
private const val ERROR_SERVER_DISCONNECTED = 11
private const val ERROR_LANGUAGE_NOT_SUPPORTED = 12
private const val ERROR_LANGUAGE_UNAVAILABLE = 13

/**
 * [VoiceRecognizer] com o `SpeechRecognizer` do Android, em pt-BR (plano §3.3, #123):
 * - Android 13+: o reconhecedor do aparelho, se ele já tem a voz pt-BR. Se não tem, pede o download
 *   ([prepare], ao abrir a tela) e, até lá, usa o serviço padrão. Se a escuta ainda encontra a voz
 *   faltando, avisa ([VoiceEvent.VoiceMissing]) e pede o download de novo, se não estiver em andamento.
 * - Android 12: o reconhecedor do aparelho, sem como conferir a voz antes. Se faltar, a escuta avisa
 *   ([VoiceEvent.SwitchedToService]) e passa ao serviço padrão daí em diante.
 * - Android 8–11, ou sem reconhecedor no aparelho: o serviço padrão (quase sempre o do Google), que
 *   pode usar a internet.
 *
 * A escolha é refeita a cada [prepare]: quando o download termina, a próxima tela já usa o
 * reconhecedor do aparelho.
 *
 * O reconhecedor encerra cada escuta numa pausa curta; o microfone segue aberto até [stop] (a pessoa
 * soltou o botão). "Sem internet" só sai de erro de rede de verdade, no serviço padrão.
 *
 * O `SpeechRecognizer` só funciona na thread principal: o fluxo roda nela, e [stop] também deve ser
 * chamado nela. Nada do que é ouvido vai para o log.
 */
class AndroidVoiceRecognizer(private val context: Context) : VoiceRecognizer {
    private enum class Engine { ON_DEVICE, SERVICE }

    /** Escolhido na primeira vez e guardado enquanto o app está aberto. */
    private var engine: Engine? = null

    override val isAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context) || hasOnDeviceRecognizer()

    private var stopCurrent: (() -> Unit)? = null

    override suspend fun prepare() {
        engine = withContext(Dispatchers.Main.immediate) { chooseEngine() }
    }

    override suspend fun mayUseInternet(): Boolean = engine() == Engine.SERVICE

    override fun listen(stopOnSilence: Boolean): Flow<VoiceEvent> = callbackFlow {
        val engine = engine()
        val onDevice = engine == Engine.ON_DEVICE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        val recognizer = if (onDevice) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }
        var stopped = false
        var lastHeardAt = SystemClock.elapsedRealtime()
        val intent = recognizeIntent()
            .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, MAX_ALTERNATIVES)

        fun silentTooLong() = SystemClock.elapsedRealtime() - lastHeardAt >= SILENCE_LIMIT_MILLIS

        fun listenAgainOrFinish() {
            if (stopped || (stopOnSilence && silentTooLong())) close() else recognizer.startListening(intent)
        }

        fun finishWith(event: VoiceEvent?) {
            event?.let { trySend(it) }
            close()
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
                        // Pausa sem fala: segue ouvindo enquanto o botão estiver apertado
                        SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> listenAgainOrFinish()

                        ERROR_LANGUAGE_NOT_SUPPORTED, ERROR_LANGUAGE_UNAVAILABLE ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && hasOnDeviceRecognizer()) {
                                // Sem a voz baixada (também no serviço padrão, que pode ser só offline)
                                launch { onDeviceHasPortuguese() }
                                finishWith(VoiceEvent.VoiceMissing)
                            } else if (onDevice) {
                                // Android 12: sem como baixar pelo app; segue pelo serviço padrão
                                this@AndroidVoiceRecognizer.engine = Engine.SERVICE
                                finishWith(VoiceEvent.SwitchedToService)
                            } else {
                                finishWith(VoiceEvent.Unavailable)
                            }

                        SpeechRecognizer.ERROR_NETWORK,
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
                        SpeechRecognizer.ERROR_SERVER,
                        ERROR_SERVER_DISCONNECTED,
                        -> finishWith(if (onDevice) VoiceEvent.Unavailable else VoiceEvent.NoInternet)

                        // Depois de soltar, o serviço às vezes reclama da escuta encerrada
                        else -> finishWith(if (stopped) null else VoiceEvent.Unavailable)
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

    private suspend fun engine(): Engine =
        engine ?: withContext(Dispatchers.Main.immediate) { chooseEngine() }.also { engine = it }

    private suspend fun chooseEngine(): Engine = when {
        !hasOnDeviceRecognizer() -> Engine.SERVICE
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
            if (onDeviceHasPortuguese()) Engine.ON_DEVICE else Engine.SERVICE
        // Android 12: sem como conferir a voz antes; se faltar, a escuta troca para o serviço
        else -> Engine.ON_DEVICE
    }

    private fun hasOnDeviceRecognizer(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)

    /**
     * Confere se o reconhecedor do aparelho já tem a voz pt-BR. Se não tem, mas pode baixar, pede o
     * download (o Android mostra o andamento) e responde `false`: desta vez vai o serviço padrão.
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun onDeviceHasPortuguese(): Boolean {
        val recognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        try {
            val support = suspendCancellableCoroutine<RecognitionSupport?> { continuation ->
                recognizer.checkRecognitionSupport(
                    recognizeIntent(),
                    context.mainExecutor,
                    object : RecognitionSupportCallback {
                        override fun onSupportResult(recognitionSupport: RecognitionSupport) {
                            if (continuation.isActive) continuation.resume(recognitionSupport)
                        }

                        override fun onError(error: Int) {
                            if (continuation.isActive) continuation.resume(null)
                        }
                    },
                )
            } ?: return false
            if (support.installedOnDeviceLanguages.any(::isPortuguese)) return true
            val canDownload = support.supportedOnDeviceLanguages.any(::isPortuguese)
            if (canDownload && support.pendingOnDeviceLanguages.none(::isPortuguese)) {
                recognizer.triggerModelDownload(recognizeIntent())
            }
            return false
        } finally {
            recognizer.destroy()
        }
    }

    private fun recognizeIntent(): Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE, LANGUAGE)
        .putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)

    private fun isPortuguese(language: String): Boolean = language.replace('_', '-').equals(LANGUAGE, ignoreCase = true)

    private fun Bundle.texts(): List<String> =
        getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty().filter { it.isNotBlank() }

    private companion object {
        const val LANGUAGE = "pt-BR"
    }
}
