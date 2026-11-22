package br.com.mentealerta.listadecompras.ui.add

import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.voice.VoiceEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Falar os produtos (#60, #123): segurar para falar, explicação, pedido de microfone, escuta e recados. */
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
        return fixture.viewModel().apply { onSpeakPress(hasMicrophone = true) }
    }

    @Test
    fun noticeComesFirstAndOnlyOnce() {
        val viewModel = fixture.viewModel()

        viewModel.onSpeakPress(hasMicrophone = true)
        assertEquals(VoiceUi.Notice, viewModel.uiState.value.voice)
        viewModel.onSpeakRelease(short = false)
        assertEquals(0, voice.listens)

        viewModel.onNoticeAnswer(accepted = true, hasMicrophone = true)
        assertTrue(fixture.voicePreferences.noticeShown)
        assertNull(viewModel.uiState.value.voice)

        viewModel.onSpeakPress(hasMicrophone = true)
        assertEquals(VoiceUi.Listening(), viewModel.uiState.value.voice)
        assertEquals(1, voice.listens)
    }

    @Test
    fun onDeviceRecognizerSkipsTheNotice() {
        voice.mayUseInternet = false
        val viewModel = fixture.viewModel()

        viewModel.onSpeakPress(hasMicrophone = true)

        assertEquals(VoiceUi.Listening(), viewModel.uiState.value.voice)
        assertFalse(fixture.voicePreferences.noticeShown)
    }

    @Test
    fun decliningTheNoticeAsksAgainNextTime() {
        val viewModel = fixture.viewModel()
        viewModel.onSpeakPress(hasMicrophone = true)

        viewModel.onNoticeAnswer(accepted = false, hasMicrophone = true)
        assertNull(viewModel.uiState.value.voice)
        assertFalse(fixture.voicePreferences.noticeShown)

        viewModel.onSpeakPress(hasMicrophone = true)
        assertEquals(VoiceUi.Notice, viewModel.uiState.value.voice)
    }

    @Test
    fun withoutMicrophoneAsksTheAndroidPermission() {
        val viewModel = fixture.viewModel()
        viewModel.onSpeakPress(hasMicrophone = false)
        viewModel.onNoticeAnswer(accepted = true, hasMicrophone = false)
        assertEquals(VoiceUi.AskingPermission, viewModel.uiState.value.voice)

        viewModel.onMicrophonePermission(granted = false)
        assertEquals(VoiceUi.Message.NO_MICROPHONE, viewModel.uiState.value.voice)

        viewModel.onSpeakPress(hasMicrophone = false)
        assertEquals(VoiceUi.AskingPermission, viewModel.uiState.value.voice)
        viewModel.onMicrophonePermission(granted = true)
        assertNull(viewModel.uiState.value.voice)
        assertEquals(0, voice.listens)

        viewModel.onSpeakPress(hasMicrophone = true)
        assertEquals(VoiceUi.Listening(), viewModel.uiState.value.voice)
    }

    @Test
    fun holdingListensUntilReleasedWithoutTheSilenceLimit() {
        val viewModel = listening()
        assertEquals(false, voice.lastStopOnSilence)
        voice.say(VoiceEvent.Heard(listOf("banana")))

        viewModel.onSpeakRelease(short = false)

        assertEquals(listOf("banana"), fixture.added.map { it.productId })
    }

    @Test
    fun aQuickTapRemindsToHoldAndAddsNothing() {
        val viewModel = listening()
        voice.say(VoiceEvent.Heard(listOf("banana")))

        viewModel.onSpeakRelease(short = true)

        assertEquals(VoiceUi.Message.HOLD_TO_SPEAK, viewModel.uiState.value.voice)
        assertTrue(fixture.added.isEmpty())
    }

    @Test
    fun talkBackTapsToOpenAndTapsToClose() {
        fixture.voicePreferences.noticeShown = true
        val viewModel = fixture.viewModel()

        viewModel.onSpeakPress(hasMicrophone = true, byToggle = true)
        assertEquals(VoiceUi.Listening(), viewModel.uiState.value.voice)
        assertEquals(true, voice.lastStopOnSilence)
        voice.say(VoiceEvent.Heard(listOf("maçã")))

        viewModel.onSpeakPress(hasMicrophone = true, byToggle = true)
        assertEquals(listOf("apple"), fixture.added.map { it.productId })
        assertEquals(1, voice.listens)
    }

    @Test
    fun leavingTheAppClosesTheMicrophoneOpenedByTalkBack() {
        fixture.voicePreferences.noticeShown = true
        val viewModel = fixture.viewModel()
        viewModel.onSpeakPress(hasMicrophone = true, byToggle = true)
        voice.say(VoiceEvent.Heard(listOf("banana")))

        viewModel.onSpeakRelease()

        assertEquals(listOf("banana"), fixture.added.map { it.productId })
    }

    @Test
    fun withoutTheVoiceOnTheDeviceSwitchesToTheServiceAndExplainsFirst() {
        voice.mayUseInternet = false
        val viewModel = fixture.viewModel()
        viewModel.onSpeakPress(hasMicrophone = true)

        voice.mayUseInternet = true
        voice.say(VoiceEvent.SwitchedToService)
        voice.finish()

        assertEquals(VoiceUi.Notice, viewModel.uiState.value.voice)
        assertEquals(1, voice.listens)
    }

    @Test
    fun withoutTheVoiceOnTheDeviceKeepsListeningIfAlreadyExplained() {
        voice.mayUseInternet = false
        val viewModel = listening()

        voice.say(VoiceEvent.SwitchedToService)
        voice.finish()

        assertEquals(VoiceUi.Listening(), viewModel.uiState.value.voice)
        assertEquals(2, voice.listens)
        voice.say(VoiceEvent.Heard(listOf("banana")))
        viewModel.onSpeakRelease(short = false)
        assertEquals(listOf("banana"), fixture.added.map { it.productId })
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
        voice.say(VoiceEvent.Heard(listOf("bananas e maçã")), VoiceEvent.Heard(listOf("duas bergamotas")))

        viewModel.onSpeakRelease(short = false)

        assertEquals(listOf("banana", "apple", null), fixture.added.map { it.productId })
        assertEquals(Quantity(2.0), fixture.added.last().quantity)
        val state = viewModel.uiState.value
        assertNull(state.voice)
        assertEquals(3, state.lastAdded?.count)
    }

    @Test
    fun everythingSaidAppearsInAddedLastSpokenFirst() = runTest {
        val viewModel = listening()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.added.collect {} }
        voice.say(VoiceEvent.Heard(listOf("bananas e maçã")))

        viewModel.onSpeakRelease(short = false)

        assertEquals(listOf("maçã", "bananas"), viewModel.added.value.map { it.name })
    }

    @Test
    fun whatIsAlreadyOnTheListDoesNotEnterAgain() {
        val viewModel = fixture.viewModel()
        viewModel.onQueryChange("banana")
        viewModel.onAdd(viewModel.uiState.value.found.first())
        val banana = fixture.items.single()
        fixture.voicePreferences.noticeShown = true
        viewModel.onSpeakPress(hasMicrophone = true)
        voice.say(VoiceEvent.Heard(listOf("bananas e maçã")))

        viewModel.onSpeakRelease()

        assertEquals(listOf("banana", "apple"), fixture.items.map { it.productId })
        assertEquals(banana, fixture.items.first())
        assertEquals(AddedUi("maçã", count = 1, alreadyOnList = listOf("Banana")), viewModel.uiState.value.lastAdded)
    }

    @Test
    fun repeatedInTheSameSpeechEntersOnce() {
        val viewModel = listening()
        voice.say(VoiceEvent.Heard(listOf("banana maçã e banana")))

        viewModel.onSpeakRelease()

        assertEquals(listOf("banana", "apple"), fixture.items.map { it.productId })
        assertEquals(2, viewModel.uiState.value.lastAdded?.count)
    }

    @Test
    fun checkedItemDoesNotCountAsAlreadyOnTheList() = runTest {
        val viewModel = fixture.viewModel()
        viewModel.onQueryChange("banana")
        viewModel.onAdd(viewModel.uiState.value.found.first())
        fixture.check(fixture.items.single().id)
        fixture.voicePreferences.noticeShown = true
        viewModel.onSpeakPress(hasMicrophone = true)
        voice.say(VoiceEvent.Heard(listOf("banana")))

        viewModel.onSpeakRelease()

        assertEquals(2, fixture.items.size)
        assertEquals(emptyList<String>(), viewModel.uiState.value.lastAdded?.alreadyOnList)
    }

    @Test
    fun everythingAlreadyOnTheListOnlySaysSo() {
        val viewModel = fixture.viewModel()
        viewModel.onQueryChange("banana")
        viewModel.onAdd(viewModel.uiState.value.found.first())
        fixture.voicePreferences.noticeShown = true
        viewModel.onSpeakPress(hasMicrophone = true)
        voice.say(VoiceEvent.Heard(listOf("banana")))

        viewModel.onSpeakRelease()

        assertEquals(1, fixture.items.size)
        assertEquals(AddedUi("", count = 0, alreadyOnList = listOf("Banana")), viewModel.uiState.value.lastAdded)
        assertNull(viewModel.uiState.value.voice)
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
        viewModel.onSpeakRelease(short = false)

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
    fun openingPreparesTheVoiceBeforeAnyTouch() {
        val viewModel = fixture.viewModel()
        assertEquals(0, voice.prepares)

        viewModel.onOpen()

        assertEquals(1, voice.prepares)
        assertEquals(0, voice.listens)
    }

    @Test
    fun voiceStillDownloadingSuggestsWriting() {
        val viewModel = listening()
        voice.say(VoiceEvent.VoiceMissing)
        voice.finish()

        assertEquals(VoiceUi.Message.DOWNLOADING_VOICE, viewModel.uiState.value.voice)
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
