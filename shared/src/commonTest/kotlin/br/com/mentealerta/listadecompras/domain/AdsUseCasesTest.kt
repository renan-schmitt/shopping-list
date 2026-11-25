package br.com.mentealerta.listadecompras.domain

import br.com.mentealerta.listadecompras.ads.AdDays
import br.com.mentealerta.listadecompras.ads.AdMoment
import br.com.mentealerta.listadecompras.ads.AdPreferences
import br.com.mentealerta.listadecompras.ads.AdState
import br.com.mentealerta.listadecompras.ads.InterstitialAds
import br.com.mentealerta.listadecompras.catalog.CatalogIcon
import br.com.mentealerta.listadecompras.catalog.CatalogSection
import br.com.mentealerta.listadecompras.premium.PremiumRepository
import br.com.mentealerta.listadecompras.premium.PurchaseResult
import br.com.mentealerta.listadecompras.premium.RestoreResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class AdsUseCasesTest {
    private val preferences = FakeAdPreferences()
    private val premium = FakePremiumRepository()
    private val ads = FakeInterstitialAds()
    private val sections = MutableStateFlow<List<ListSection>>(emptyList())
    private val firstOpen = Instant.parse("2026-11-01T10:00:00Z")
    private var now = firstOpen
    private val clock = object : Clock {
        override fun now(): Instant = now
    }

    /** Dias em UTC: o dia 1 é 1º de novembro. */
    private val useCase = AdsUseCases(
        preferences = preferences,
        features = ObserveFeaturesUseCase(premium),
        list = FakeListRepository(sections),
        ads = ads,
        clock = clock,
        days = AdDays { it.epochSeconds / 1.days.inWholeSeconds },
    )

    @Test
    fun firstOpenStartsTheFirstWeek() = runTest {
        assertFalse(useCase.onAppOpened())

        assertEquals(firstOpen, preferences.current.firstUse)
        now += 2.days
        useCase.onAppOpened()
        assertEquals(firstOpen, preferences.current.firstUse)
    }

    @Test
    fun weekNoticeAppearsFromTheSeventhDay() = runTest {
        useCase.onAppOpened()

        now = firstOpen + 5.days + 13.hours // dia 6, 23h
        assertFalse(useCase.onAppOpened())
        now = firstOpen + 6.days - 9.hours // dia 7, 1h
        assertTrue(useCase.onAppOpened())
    }

    @Test
    fun weekNoticeAppearsOnceOnly() = runTest {
        useCase.onAppOpened()
        now += 6.days

        assertTrue(useCase.onAppOpened())
        useCase.onWeekNoticeShown()

        assertFalse(useCase.onAppOpened())
        now += 3.days
        assertFalse(useCase.onAppOpened())
    }

    @Test
    fun weekNoticeWaitsForAnOpeningWithoutCheckedItems() = runTest {
        useCase.onAppOpened()
        now += 6.days
        sections.value = listOf(section(item("Leite", checked = true)))

        assertFalse(useCase.onAppOpened())

        sections.value = listOf(section(item("Leite")))
        assertTrue(useCase.onAppOpened())
    }

    @Test
    fun weekNoticeLateStillComesBeforeTheAds() = runTest {
        useCase.onAppOpened()
        now += 20.days

        assertFalse(useCase.canShow())
        assertTrue(useCase.onAppOpened())
    }

    @Test
    fun noAdsInTheFirstWeek() = runTest {
        useCase.onAppOpened()
        now += 5.days

        assertFalse(useCase.canShow())
        assertTrue(ads.preloads.isEmpty())
    }

    @Test
    fun adsStartTheDayAfterTheNotice() = runTest {
        startAds()

        assertTrue(useCase.canShow())
    }

    @Test
    fun noAdsOnTheDayOfTheNotice() = runTest {
        useCase.onAppOpened()
        now += 6.days
        useCase.onWeekNoticeShown()

        now += 10.hours
        assertFalse(useCase.canShow())
    }

    @Test
    fun noAdsWithACheckedItem() = runTest {
        startAds()
        sections.value = listOf(section(item("Leite"), item("Café", checked = true)))

        assertFalse(useCase.canShow())
    }

    @Test
    fun noAdsWithTheFullVersion() = runTest {
        startAds()
        premium.isPremium.value = true

        assertFalse(useCase.canShow())
        assertFalse(useCase.onAppOpened())
    }

    @Test
    fun fullVersionSkipsTheWeekNotice() = runTest {
        premium.isPremium.value = true
        useCase.onAppOpened()
        now += 6.days

        assertFalse(useCase.onAppOpened())
    }

    @Test
    fun openingPreloadsAnAdOnceTheyStarted() = runTest {
        startAds()

        useCase.onAppOpened()

        assertEquals(listOf(true), ads.preloads)
    }

    @Test
    fun everyThirdTimeOfEachMoment() = runTest {
        val backFromAdd = (1..6).map { useCase.count(AdMoment.BackFromAdd) }
        val edit = (1..3).map { useCase.count(AdMoment.Edit) }

        assertEquals(listOf(false, false, true, false, false, true), backFromAdd)
        assertEquals(listOf(false, false, true), edit)
    }

    @Test
    fun fullVersionDoesNotCount() = runTest {
        premium.isPremium.value = true

        repeat(3) { assertFalse(useCase.count(AdMoment.BackFromAdd)) }
    }

    @Test
    fun showUsesThePersonalizationAndPreloadsTheNext() = runTest {
        startAds()
        SetPersonalizedAdsUseCase(preferences)(false)

        useCase.show()

        assertEquals(listOf(false), ads.shows)
        assertEquals(listOf(false), ads.preloads)
    }

    @Test
    fun personalizedIsOnByDefault() = runTest {
        assertTrue(ObservePersonalizedAdsUseCase(preferences)().first())
    }

    private suspend fun startAds() {
        useCase.onAppOpened()
        now += 6.days
        useCase.onWeekNoticeShown()
        now += 1.days
        ads.preloads.clear()
    }

    private fun section(vararg items: ListItem) =
        ListSection(CatalogSection("pantry", "Mercearia", 1, CatalogIcon.Fluent("Hot beverage")), items.toList())

    private fun item(name: String, checked: Boolean = false) =
        ListItem(ListItemId.random(), name, productId = null, sectionId = "pantry", checked = checked)

    private operator fun Instant.plus(duration: Duration): Instant = Instant.fromEpochMilliseconds(
        toEpochMilliseconds() + duration.inWholeMilliseconds,
    )
}

private class FakeAdPreferences : AdPreferences {
    private val flow = MutableStateFlow(AdState())
    val current: AdState get() = flow.value

    override val state: Flow<AdState> = flow

    override suspend fun update(transform: (AdState) -> AdState): AdState = flow.updateAndGet(transform)
}

private class FakeInterstitialAds : InterstitialAds {
    val preloads = mutableListOf<Boolean>()
    val shows = mutableListOf<Boolean>()

    override fun preload(personalized: Boolean) {
        preloads += personalized
    }

    override fun show(personalized: Boolean): Boolean {
        shows += personalized
        return true
    }
}

private class FakePremiumRepository : PremiumRepository {
    override val isPremium = MutableStateFlow(false)

    override fun start() = Unit

    override suspend fun price(): String? = null

    override suspend fun buy(): PurchaseResult = PurchaseResult.Failed

    override suspend fun restore(): RestoreResult = RestoreResult.Failed
}

/** Só a leitura da lista: é o que os anúncios usam, para saber se há produto riscado. */
private class FakeListRepository(private val sections: Flow<List<ListSection>>) : ShoppingListRepository {
    override fun observeSections(): Flow<List<ListSection>> = sections

    override suspend fun add(item: ListItem) = error("não usado")

    override suspend fun update(item: ListItem): ListItem? = error("não usado")

    override suspend fun setChecked(id: ListItemId, checked: Boolean): ListItem? = error("não usado")

    override suspend fun remove(id: ListItemId): RemovedItems = error("não usado")

    override suspend fun clear(): RemovedItems = error("não usado")

    override suspend fun restore(removed: RemovedItems) = error("não usado")

    override suspend fun finishShopping(): FinishedShopping? = error("não usado")

    override suspend fun undoFinishShopping(finished: FinishedShopping) = error("não usado")
}
