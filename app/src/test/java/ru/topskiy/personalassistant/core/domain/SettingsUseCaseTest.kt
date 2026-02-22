package ru.topskiy.personalassistant.core.domain

import android.content.Context
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import ru.topskiy.personalassistant.core.datastore.InitialSettings
import ru.topskiy.personalassistant.core.datastore.SettingsRepository
import ru.topskiy.personalassistant.core.model.ServiceId
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsUseCaseTest {

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
        override suspend fun setTheme(mode: String): Result<Unit> = Result.success(Unit)
        override suspend fun getInitialSettings(): Result<InitialSettings> = Result.success(
            InitialSettings(
                enabledServices = enabledFlow.value,
                favoriteService = favoriteFlow.value,
                lastService = lastFlow.value,
                onboardingDone = onboardingFlow.value
            )
        )
    }

    @Test
    fun `getInitialSettings returns repository data`() = runTest {
        val repo = FakeSettingsRepository(
            enabled = setOf(ServiceId.DEALS, ServiceId.NOTES),
            favorite = ServiceId.NOTES,
            last = ServiceId.DEALS,
            onboardingDone = true
        )
        val useCase = SettingsUseCaseImpl(repo)

        val result = useCase.getInitialSettings()

        assertTrue(result.isSuccess)
        val s = result.getOrThrow()
        assertEquals(setOf(ServiceId.DEALS, ServiceId.NOTES), s.enabledServices)
        assertEquals(ServiceId.NOTES, s.favoriteService)
        assertEquals(ServiceId.DEALS, s.lastService)
        assertTrue(s.onboardingDone)
    }

    @Test
    fun `completeOnboarding writes enabled last favorite and onboarding`() = runTest {
        val repo = FakeSettingsRepository()
        val useCase = SettingsUseCaseImpl(repo)
        val selected = setOf(ServiceId.DEALS, ServiceId.NOTES)
        val first = ServiceId.NOTES
        val favorite = ServiceId.DEALS

        val result = useCase.completeOnboarding(selected, first, favorite)

        assertTrue(result.isSuccess)
        assertEquals(selected, repo.enabledFlow.value)
        assertTrue(repo.onboardingFlow.value)
        assertEquals(first, repo.lastFlow.value)
        assertEquals(favorite, repo.favoriteFlow.value)
    }

    @Test
    fun `completeOnboarding with null favorite clears favorite`() = runTest {
        val repo = FakeSettingsRepository(favorite = ServiceId.DEALS)
        val useCase = SettingsUseCaseImpl(repo)

        useCase.completeOnboarding(setOf(ServiceId.DEALS, ServiceId.NOTES), ServiceId.NOTES, null)

        assertNull(repo.favoriteFlow.value)
    }

    @Test
    fun `completeOnboarding fails on first repository failure`() = runTest {
        val repo = object : FakeSettingsRepository() {
            override suspend fun setEnabledServices(set: Set<ServiceId>): Result<Unit> =
                Result.failure(IllegalStateException("fail"))
        }
        val useCase = SettingsUseCaseImpl(repo)

        val result = useCase.completeOnboarding(
            setOf(ServiceId.DEALS),
            ServiceId.DEALS,
            null
        )

        assertFalse(result.isSuccess)
        assertEquals(setOf(ServiceId.DEALS), repo.enabledFlow.value)
        assertFalse(repo.onboardingFlow.value)
    }

    @Test
    fun `flows delegate to repository`() = runTest {
        val repo = FakeSettingsRepository(
            enabled = setOf(ServiceId.CREDITS),
            favorite = ServiceId.CREDITS,
            onboardingDone = true
        )
        val useCase = SettingsUseCaseImpl(repo)

        assertEquals(setOf(ServiceId.CREDITS), useCase.enabledServicesFlow.first())
        assertEquals(ServiceId.CREDITS, useCase.favoriteServiceFlow.first())
        assertTrue(useCase.onboardingDoneFlow.first())
    }
}
