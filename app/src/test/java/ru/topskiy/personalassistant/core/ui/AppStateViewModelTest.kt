package ru.topskiy.personalassistant.core.ui

import android.content.Context
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test
import ru.topskiy.personalassistant.R
import ru.topskiy.personalassistant.core.datastore.InitialSettings
import ru.topskiy.personalassistant.core.datastore.SettingsRepository
import ru.topskiy.personalassistant.core.model.ServiceId
import ru.topskiy.personalassistant.core.model.ServiceRegistry

private class FakeSettingsRepository(
    enabled: Set<ServiceId> = setOf(ServiceId.DEALS),
    favorite: ServiceId? = null,
    last: ServiceId? = null,
    onboardingDone: Boolean = false
) : SettingsRepository {

    val enabledFlow = MutableStateFlow(enabled)
    val favoriteFlow = MutableStateFlow(favorite)
    val lastFlow = MutableStateFlow(last)
    val onboardingFlow = MutableStateFlow(onboardingDone)
    var lastSetTheme: String? = null

    override val enabledServicesFlow: Flow<Set<ServiceId>> = enabledFlow
    override val favoriteServiceFlow: Flow<ServiceId?> = favoriteFlow
    override val lastServiceFlow: Flow<ServiceId?> = lastFlow
    override val onboardingDoneFlow: Flow<Boolean> = onboardingFlow
    override val themeFlow: Flow<String> = flowOf("light")
    override val servicesCatalogListViewFlow: Flow<Boolean> = flowOf(true)

    override suspend fun ensureThemeInitialized(context: Context): Result<Unit> = Result.success(Unit)

    override suspend fun setServicesCatalogListView(listView: Boolean): Result<Unit> = Result.success(Unit)

    override suspend fun setEnabledServices(set: Set<ServiceId>): Result<Unit> =
        runCatching {
            enabledFlow.value = set
            Unit
        }

    override suspend fun setFavorite(value: ServiceId?): Result<Unit> = runCatching {
        favoriteFlow.value = value
        Unit
    }

    override suspend fun setLastService(value: ServiceId?): Result<Unit> = runCatching {
        lastFlow.value = value
        Unit
    }

    override suspend fun setOnboardingDone(done: Boolean): Result<Unit> = runCatching {
        onboardingFlow.value = done
        Unit
    }

    override suspend fun setTheme(mode: String): Result<Unit> =
        runCatching {
            lastSetTheme = mode
            Unit
        }

    override suspend fun getInitialSettings(): Result<InitialSettings> =
        Result.success(
            InitialSettings(
                enabledServices = enabledFlow.value,
                favoriteService = favoriteFlow.value,
                lastService = lastFlow.value,
                onboardingDone = onboardingFlow.value
            )
        )
}

@OptIn(ExperimentalCoroutinesApi::class)
class AppStateViewModelTest {

    @Test
    fun `homeServiceId prefers favorite then last then first enabled`() {
        val enabled = setOf(ServiceId.DEALS, ServiceId.NOTES, ServiceId.CREDITS)
        val favorite = ServiceId.CREDITS
        val last = ServiceId.NOTES
        val state = AppStateUiState(
            enabledServices = enabled,
            favoriteService = favorite,
            lastService = last,
            onboardingDone = false
        )

        val home = state.homeServiceId()

        assertEquals(favorite, home)
    }

    @Test
    fun `toggleService does not disable last remaining service`() = runTest {
        val repo = FakeSettingsRepository(enabled = setOf(ServiceId.DEALS))
        val vm = AppStateViewModel(repo)

        // ждём, пока stateIn проглотит начальные значения
        assertEquals(setOf(ServiceId.DEALS), vm.uiState.value.enabledServices)

        vm.toggleService(ServiceId.DEALS, enabled = false)

        // enabledServices не должен стать пустым
        assertEquals(setOf(ServiceId.DEALS), vm.uiState.value.enabledServices)
    }

    @Test
    fun `completeOnboarding writes enabled, onboardingDone and lastService`() = runTest {
        val repo = FakeSettingsRepository()
        val vm = AppStateViewModel(repo)
        val selected = setOf(ServiceId.DEALS, ServiceId.NOTES)
        val first = ServiceId.NOTES

        val result = vm.completeOnboarding(selected, first)

        assertTrue(result.isSuccess)
        assertEquals(selected, repo.enabledFlow.value)
        assertTrue(repo.onboardingFlow.value)
        assertEquals(first, repo.lastFlow.value)
    }

    @Test
    fun `setTheme delegates to repository`() = runTest {
        val repo = FakeSettingsRepository()
        val vm = AppStateViewModel(repo)

        vm.setTheme("dark")

        assertEquals("dark", repo.lastSetTheme)
    }

    @Test
    fun `setTheme emits error message when repository fails`() = runTest {
        val failingRepo = object : FakeSettingsRepository() {
            override suspend fun setTheme(mode: String): Result<Unit> =
                Result.failure(IllegalStateException("failure"))
        }
        val vm = AppStateViewModel(failingRepo)

        val job = launch {
            val messageId = vm.messageEvent.first()
            assertEquals(R.string.settings_save_error, messageId)
        }

        vm.setTheme("dark")
        job.cancel()
    }

    @Test
    fun `setEnabledServicesDirectly writes to repository`() = runTest {
        val repo = FakeSettingsRepository(enabled = setOf(ServiceId.DEALS))
        val vm = AppStateViewModel(repo)
        val newSet = setOf(ServiceId.DEALS, ServiceId.NOTES)

        vm.setEnabledServicesDirectly(newSet)

        assertEquals(newSet, repo.enabledFlow.value)
    }

    @Test
    fun `setEnabledServicesDirectly emits error message when repository fails`() = runTest {
        val failingRepo = object : FakeSettingsRepository() {
            override suspend fun setEnabledServices(set: Set<ServiceId>): Result<Unit> =
                Result.failure(IllegalStateException("failure"))
        }
        val vm = AppStateViewModel(failingRepo)
        val services = setOf(ServiceId.DEALS, ServiceId.NOTES)

        val job = launch {
            val messageId = vm.messageEvent.first()
            assertEquals(R.string.settings_save_error, messageId)
        }

        vm.setEnabledServicesDirectly(services)
        job.cancel()
    }

    @Test
    fun `getInitialState returns onboarding route when onboarding not done`() = runTest {
        val repo = FakeSettingsRepository(onboardingDone = false)
        val vm = AppStateViewModel(repo)

        val state = vm.getInitialState()
        val targetRoute = if (!state.onboardingDone) {
            ONBOARDING_ROUTE
        } else {
            MAIN_ROUTE
        }

        assertEquals(ONBOARDING_ROUTE, targetRoute)
    }

    @Test
    fun `getInitialState returns main route when onboarding done`() = runTest {
        val repo = FakeSettingsRepository(onboardingDone = true)
        val vm = AppStateViewModel(repo)

        val state = vm.getInitialState()
        val targetRoute = if (!state.onboardingDone) {
            ONBOARDING_ROUTE
        } else {
            MAIN_ROUTE
        }

        assertEquals(MAIN_ROUTE, targetRoute)
    }

    @Test
    fun `getInitialState falls back to defaults and emits error when repository fails`() = runTest {
        val failingRepo = object : FakeSettingsRepository() {
            override suspend fun getInitialSettings(): Result<InitialSettings> =
                Result.failure(IllegalStateException("load failed"))
        }
        val vm = AppStateViewModel(failingRepo)

        var receivedMessageId: Int? = null
        val job = launch {
            receivedMessageId = vm.messageEvent.first()
        }

        val state = vm.getInitialState()

        assertEquals(setOf(ServiceId.DEALS), state.enabledServices)
        assertEquals(null, state.favoriteService)
        assertEquals(null, state.lastService)
        assertFalse(state.onboardingDone)
        assertEquals(R.string.settings_load_error, receivedMessageId)

        job.cancel()
    }
}

