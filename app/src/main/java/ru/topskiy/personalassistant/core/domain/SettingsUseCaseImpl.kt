package ru.topskiy.personalassistant.core.domain

import android.content.Context
import kotlinx.coroutines.flow.Flow
import ru.topskiy.personalassistant.core.datastore.InitialSettings
import ru.topskiy.personalassistant.core.datastore.SettingsRepository
import ru.topskiy.personalassistant.core.model.ServiceId
import javax.inject.Inject

/**
 * Реализация [SettingsUseCase]: делегирует все операции в [SettingsRepository].
 */
class SettingsUseCaseImpl @Inject constructor(
    private val repository: SettingsRepository
) : SettingsUseCase {

    override val enabledServicesFlow: Flow<Set<ServiceId>> = repository.enabledServicesFlow
    override val favoriteServiceFlow: Flow<ServiceId?> = repository.favoriteServiceFlow
    override val lastServiceFlow: Flow<ServiceId?> = repository.lastServiceFlow
    override val onboardingDoneFlow: Flow<Boolean> = repository.onboardingDoneFlow
    override val themeFlow: Flow<String> = repository.themeFlow
    override val servicesCatalogListViewFlow: Flow<Boolean> = repository.servicesCatalogListViewFlow
    override val notificationsEnabledFlow: Flow<Boolean> = repository.notificationsEnabledFlow

    override suspend fun getInitialSettings(): Result<InitialSettings> = repository.getInitialSettings()
    override suspend fun setTheme(mode: String): Result<Unit> = repository.setTheme(mode)
    override suspend fun setEnabledServices(set: Set<ServiceId>): Result<Unit> = repository.setEnabledServices(set)
    override suspend fun setFavorite(value: ServiceId?): Result<Unit> = repository.setFavorite(value)
    override suspend fun setLastService(value: ServiceId?): Result<Unit> = repository.setLastService(value)
    override suspend fun setOnboardingDone(done: Boolean): Result<Unit> = repository.setOnboardingDone(done)
    override suspend fun setServicesCatalogListView(listView: Boolean): Result<Unit> =
        repository.setServicesCatalogListView(listView)
    override suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit> =
        repository.setNotificationsEnabled(enabled)
    override suspend fun ensureThemeInitialized(context: Context): Result<Unit> =
        repository.ensureThemeInitialized(context)
    override suspend fun ensureMigrationDone() = repository.ensureMigrationDone()

    override suspend fun completeOnboarding(
        selectedServices: Set<ServiceId>,
        firstService: ServiceId,
        favoriteService: ServiceId?
    ): Result<Unit> {
        repository.setEnabledServices(selectedServices).onFailure { return Result.failure(it) }
        repository.setOnboardingDone(true).onFailure { return Result.failure(it) }
        repository.setLastService(firstService).onFailure { return Result.failure(it) }
        repository.setFavorite(
            if (favoriteService != null && favoriteService in selectedServices) favoriteService else null
        ).onFailure { return Result.failure(it) }
        return Result.success(Unit)
    }
}
