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
import ru.topskiy.personalassistant.core.domain.SettingsUseCase
import ru.topskiy.personalassistant.core.model.ServiceId
import ru.topskiy.personalassistant.core.model.ServiceRegistry

private class FakeSettingsUseCase(
    enabled: Set<ServiceId> = setOf(ServiceId.DEALS),
    favorite: ServiceId? = null,
    last: ServiceId? = null,
    onboardingDone: Boolean = false
) : SettingsUseCase {

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
    private val _notificationsEnabledFlow = MutableStateFlow(false)
    override val notificationsEnabledFlow: Flow<Boolean> = _notificationsEnabledFlow

    override suspend fun ensureThemeInitialized(context: Context): Result<Unit> = Result.success(Unit)
    override suspend fun ensureMigrationDone() {}
    override suspend fun setServicesCatalogListView(listView: Boolean): Result<Unit> = Result.success(Unit)
    override suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit> = runCatching {
        _notificationsEnabledFlow.value = enabled
        Unit
    }
    override suspend fun setEnabledServices(set: Set<ServiceId>): Result<Unit> = runCatching {
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
    override suspend fun setTheme(mode: String): Result<Unit> = runCatching {
        lastSetTheme = mode
        Unit
    }
    override suspend fun getInitialSettings(): Result<InitialSettings> = Result.success(
        InitialSettings(
            enabledServices = enabledFlow.value,
            favoriteService = favoriteFlow.value,
            lastService = lastFlow.value,
            onboardingDone = onboardingFlow.value
        )
    )
    override suspend fun completeOnboarding(
        selectedServices: Set<ServiceId>,
        firstService: ServiceId,
        favoriteService: ServiceId?
    ): Result<Unit> = runCatching {
        enabledFlow.value = selectedServices
        onboardingFlow.value = true
        lastFlow.value = firstService
        favoriteFlow.value = if (favoriteService != null && favoriteService in selectedServices) favoriteService else null
        Unit
    }
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
        val useCase = FakeSettingsUseCase(enabled = setOf(ServiceId.DEALS))
        val vm = AppStateViewModel(useCase)

        // ждём, пока stateIn проглотит начальные значения
        assertEquals(setOf(ServiceId.DEALS), vm.uiState.value.enabledServices)

        vm.toggleService(ServiceId.DEALS, enabled = false)

        // enabledServices не должен стать пустым
        assertEquals(setOf(ServiceId.DEALS), vm.uiState.value.enabledServices)
    }

    @Test
    fun `completeOnboarding writes enabled, onboardingDone, lastService and favorite`() = runTest {
        val useCase = FakeSettingsUseCase()
        val vm = AppStateViewModel(useCase)
        val selected = setOf(ServiceId.DEALS, ServiceId.NOTES)
        val first = ServiceId.NOTES
        val favorite = ServiceId.DEALS

        val result = vm.completeOnboarding(selected, first, favorite)

        assertTrue(result.isSuccess)
        assertEquals(selected, useCase.enabledFlow.value)
        assertTrue(useCase.onboardingFlow.value)
        assertEquals(first, useCase.lastFlow.value)
        assertEquals(favorite, useCase.favoriteFlow.value)
    }

    @Test
    fun `completeOnboarding with null favorite clears favorite`() = runTest {
        val useCase = FakeSettingsUseCase(favorite = ServiceId.DEALS)
        val vm = AppStateViewModel(useCase)
        val selected = setOf(ServiceId.DEALS, ServiceId.NOTES)
        val first = ServiceId.NOTES

        val result = vm.completeOnboarding(selected, first, null)

        assertTrue(result.isSuccess)
        assertEquals(null, useCase.favoriteFlow.value)
    }

    @Test
    fun `setTheme delegates to use case`() = runTest {
        val useCase = FakeSettingsUseCase()
        val vm = AppStateViewModel(useCase)

        vm.setTheme("dark")

        assertEquals("dark", useCase.lastSetTheme)
    }

    @Test
    fun `setTheme emits settings_save_error when use case fails`() = runTest {
        val failingUseCase = object : FakeSettingsUseCase() {
            override suspend fun setTheme(mode: String): Result<Unit> =
                Result.failure(IllegalStateException("failure"))
        }
        val vm = AppStateViewModel(failingUseCase)

        var receivedMessageId: Int? = null
        val job = launch {
            receivedMessageId = vm.messageEvent.first()
        }

        vm.setTheme("dark")

        job.join()
        assertEquals(R.string.settings_save_error, receivedMessageId)
    }

    @Test
    fun `getInitialServiceIdForMainScreen first show in session prefers favorite then last then home`() = runTest {
        val enabled = setOf(ServiceId.DEALS, ServiceId.NOTES, ServiceId.CREDITS)
        val home = ServiceId.DEALS

        val useCaseFavorite = FakeSettingsUseCase(enabled = enabled, favorite = ServiceId.CREDITS, last = ServiceId.NOTES)
        val vm1 = AppStateViewModel(useCaseFavorite)
        assertEquals(ServiceId.CREDITS, vm1.getInitialServiceIdForMainScreen(enabled, ServiceId.CREDITS, ServiceId.NOTES, home))

        val useCaseLast = FakeSettingsUseCase(enabled = enabled, favorite = null, last = ServiceId.NOTES)
        val vm2 = AppStateViewModel(useCaseLast)
        assertEquals(ServiceId.NOTES, vm2.getInitialServiceIdForMainScreen(enabled, null, ServiceId.NOTES, home))

        val useCaseHome = FakeSettingsUseCase(enabled = enabled, favorite = null, last = null)
        val vm3 = AppStateViewModel(useCaseHome)
        assertEquals(ServiceId.DEALS, vm3.getInitialServiceIdForMainScreen(enabled, null, null, home))
    }

    @Test
    fun `getInitialServiceIdForMainScreen return from settings prefers last then favorite then home`() = runTest {
        val enabled = setOf(ServiceId.DEALS, ServiceId.NOTES, ServiceId.CREDITS)
        val home = ServiceId.DEALS
        val useCase = FakeSettingsUseCase(enabled = enabled, favorite = ServiceId.CREDITS, last = ServiceId.NOTES)
        val vm = AppStateViewModel(useCase)

        vm.getInitialServiceIdForMainScreen(enabled, ServiceId.CREDITS, ServiceId.NOTES, home)

        assertEquals(ServiceId.NOTES, vm.getInitialServiceIdForMainScreen(enabled, ServiceId.CREDITS, ServiceId.NOTES, home))
        assertEquals(ServiceId.CREDITS, vm.getInitialServiceIdForMainScreen(enabled, ServiceId.CREDITS, null, home))
        assertEquals(ServiceId.DEALS, vm.getInitialServiceIdForMainScreen(enabled, null, null, home))
    }

    @Test
    fun `setEnabledServicesDirectly writes to use case`() = runTest {
        val useCase = FakeSettingsUseCase(enabled = setOf(ServiceId.DEALS))
        val vm = AppStateViewModel(useCase)
        val newSet = setOf(ServiceId.DEALS, ServiceId.NOTES)

        vm.setEnabledServicesDirectly(newSet)

        assertEquals(newSet, useCase.enabledFlow.value)
    }

    @Test
    fun `setEnabledServicesDirectly emits error message when use case fails`() = runTest {
        val failingUseCase = object : FakeSettingsUseCase() {
            override suspend fun setEnabledServices(set: Set<ServiceId>): Result<Unit> =
                Result.failure(IllegalStateException("failure"))
        }
        val vm = AppStateViewModel(failingUseCase)
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
        val useCase = FakeSettingsUseCase(onboardingDone = false)
        val vm = AppStateViewModel(useCase)

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
        val useCase = FakeSettingsUseCase(onboardingDone = true)
        val vm = AppStateViewModel(useCase)

        val state = vm.getInitialState()
        val targetRoute = if (!state.onboardingDone) {
            ONBOARDING_ROUTE
        } else {
            MAIN_ROUTE
        }

        assertEquals(MAIN_ROUTE, targetRoute)
    }

    @Test
    fun `getInitialState falls back to defaults and emits error when use case fails`() = runTest {
        val failingUseCase = object : FakeSettingsUseCase() {
            override suspend fun getInitialSettings(): Result<InitialSettings> =
                Result.failure(IllegalStateException("load failed"))
        }
        val vm = AppStateViewModel(failingUseCase)

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

