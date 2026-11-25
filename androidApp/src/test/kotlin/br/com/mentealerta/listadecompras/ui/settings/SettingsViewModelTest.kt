package br.com.mentealerta.listadecompras.ui.settings

import app.cash.turbine.test
import br.com.mentealerta.listadecompras.domain.ObservePersonalizedAdsUseCase
import br.com.mentealerta.listadecompras.domain.SetPersonalizedAdsUseCase
import br.com.mentealerta.listadecompras.ui.list.FakeAdPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val preferences = FakeAdPreferences()
    private val viewModel by lazy {
        SettingsViewModel(ObservePersonalizedAdsUseCase(preferences), SetPersonalizedAdsUseCase(preferences))
    }

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun personalizedAdsIsOnByDefaultAndCanBeTurnedOff() = runTest {
        viewModel.uiState.test {
            assertEquals(SettingsUiState.Content(personalizedAds = true), awaitItem())

            viewModel.onPersonalizedAdsChange(false)

            assertEquals(SettingsUiState.Content(personalizedAds = false), awaitItem())
            assertFalse(preferences.current.personalized)
        }
    }
}
