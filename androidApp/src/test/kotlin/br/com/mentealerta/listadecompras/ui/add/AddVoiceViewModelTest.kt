package br.com.mentealerta.listadecompras.ui.add

import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.voice.VoiceEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Falar os produtos (#60): explicação, pedido de microfone, escuta e recados. */
@OptIn(ExperimentalCoroutinesApi::class)
class AddVoiceViewModelTest {
    private val fixture = AddFixture()
    private val voice = fixture.voice

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun listening(): AddViewModel {
        fixture.voicePreferences.noticeShown = true
        return fixture.viewModel().apply { onSpeak(hasMicrophone = true) }
    }

    @Test
    fun noticeComesFirstAndOnlyOnce() {
        val viewModel = fixture.viewModel()

        viewModel.onSpeak(hasMicrophone = true)
        assertEquals(VoiceUi.Notice, viewModel.uiState.value.voice)
        assertEquals(0, voice.listens)

        viewModel.onAcceptNotice(hasMicrophone = true)
        assertTrue(fixture.voicePreferences.noticeShown)
        assertEquals(VoiceUi.Listening(), viewModel.uiState.value.voice)
        voice.finish()

        viewModel.onSpeak(hasMicrophone = true)
        assertEquals(VoiceUi.Listening(), viewModel.uiState.value.voice)
        assertEquals(2, voice.listens)
    }

    @Test
    fun decliningTheNoticeAsksAgainNextTime() {
        val viewModel = fixture.viewModel()
        viewModel.onSpeak(hasMicrophone = true)

        viewModel.onDeclineNotice()
        assertNull(viewModel.uiState.value.voice)
        assertFalse(fixture.voicePreferences.noticeShown)

        viewModel.onSpeak(hasMicrophone = true)
        assertEquals(VoiceUi.Notice, viewModel.uiState.value.voice)
    }

    @Test
    fun withoutMicrophoneAsksTheAndroidPermission() {
        val viewModel = fixture.viewModel()
        viewModel.onSpeak(hasMicrophone = false)
        viewModel.onAcceptNotice(hasMicrophone = false)
        assertEquals(VoiceUi.AskingPermission, viewModel.uiState.value.voice)

        viewModel.onMicrophonePermission(granted = false)
        assertEquals(VoiceUi.Message.NO_MICROPHONE, viewModel.uiState.value.voice)
        assertEquals(0, voice.listens)

        viewModel.onSpeak(hasMicrophone = false)
        viewModel.onMicrophonePermission(granted = true)
        assertEquals(VoiceUi.Listening(), viewModel.uiState.value.voice)
    }

    @Test
    fun showsWhatIsHeardWhileListening() {
        val viewModel = listening()

        voice.say(VoiceEvent.Partial("arroz fei"))
        assertEquals(VoiceUi.Listening(partial = "arroz fei"), viewModel.uiState.value.voice)

        voice.say(VoiceEvent.Heard(listOf("arroz feijão", "arroz e feijão")), VoiceEvent.Partial("2"))
        assertEquals(VoiceUi.Listening(heard = listOf("arroz feijão"), partial = "2"), viewModel.uiState.value.voice)
    }

    @Test
    fun everythingSaidEntersTheListWhenDone() {
        val viewModel = listening()
        voice.say(VoiceEvent.Heard(listOf("bananas e maçã")), VoiceEvent.Heard(listOf("duas bananas")))

        viewModel.onStopListening()

        assertEquals(listOf("banana", "apple", "banana"), fixture.added.map { it.productId })
        assertEquals(Quantity(2.0), fixture.added.last().quantity)
        val state = viewModel.uiState.value
        assertNull(state.voice)
        assertEquals(3, state.lastAdded?.count)
    }

    @Test
    fun oneProductUsesItsNameInTheNotice() {
        val viewModel = listening()
        voice.say(VoiceEvent.Heard(listOf("maçã")))
        voice.finish()

        assertEquals(AddedUi("maçã"), viewModel.uiState.value.lastAdded)
    }

    @Test
    fun picksTheAlternativeThatMatchesTheCatalog() {
        val viewModel = listening()
        voice.say(VoiceEvent.Heard(listOf("ba nana", "banana")))
        viewModel.onStopListening()

        assertEquals(listOf("banana"), fixture.added.map { it.productId })
    }

    @Test
    fun nothingHeardAsksToSpeakAgain() {
        val viewModel = listening()
        voice.finish()

        assertEquals(VoiceUi.Message.NOT_UNDERSTOOD, viewModel.uiState.value.voice)
        assertTrue(fixture.added.isEmpty())
    }

    @Test
    fun withoutInternetSuggestsWriting() {
        val viewModel = listening()
        voice.say(VoiceEvent.NoInternet)
        voice.finish()

        assertEquals(VoiceUi.Message.NO_INTERNET, viewModel.uiState.value.voice)
    }

    @Test
    fun whatWasHeardBeforeAProblemStillEnters() {
        val viewModel = listening()
        voice.say(VoiceEvent.Heard(listOf("banana")), VoiceEvent.Unavailable)
        voice.finish()

        assertEquals(1, fixture.added.size)
        assertEquals(VoiceUi.Message.UNAVAILABLE, viewModel.uiState.value.voice)
    }

    @Test
    fun typingClearsTheMessage() {
        val viewModel = listening()
        voice.finish()

        viewModel.onQueryChange("b")
        assertNull(viewModel.uiState.value.voice)
    }

    @Test
    fun closingStopsListeningWithoutAdding() {
        val viewModel = listening()
        voice.say(VoiceEvent.Heard(listOf("banana")))

        viewModel.onClose()
        voice.finish()

        assertEquals(AddUiState(), viewModel.uiState.value)
        assertTrue(fixture.added.isEmpty())
    }
}
