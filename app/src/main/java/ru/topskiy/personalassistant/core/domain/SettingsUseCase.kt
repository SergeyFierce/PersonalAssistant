package ru.topskiy.personalassistant.core.domain

import android.content.Context
import kotlinx.coroutines.flow.Flow
import ru.topskiy.personalassistant.core.datastore.InitialSettings
import ru.topskiy.personalassistant.core.model.ServiceId

/**
 * Use case настроек приложения: инкапсулирует операции над настройками.
 * ViewModel и Application зависят от этого интерфейса, а не от репозитория напрямую.
 */
interface SettingsUseCase {

    val enabledServicesFlow: Flow<Set<ServiceId>>
    val favoriteServiceFlow: Flow<ServiceId?>
    val lastServiceFlow: Flow<ServiceId?>
    val onboardingDoneFlow: Flow<Boolean>
    val themeFlow: Flow<String>
    val servicesCatalogListViewFlow: Flow<Boolean>
    val notificationsEnabledFlow: Flow<Boolean>

    suspend fun getInitialSettings(): Result<InitialSettings>
    suspend fun setTheme(mode: String): Result<Unit>
    suspend fun setEnabledServices(set: Set<ServiceId>): Result<Unit>
    suspend fun setFavorite(value: ServiceId?): Result<Unit>
    suspend fun setLastService(value: ServiceId?): Result<Unit>
    suspend fun setOnboardingDone(done: Boolean): Result<Unit>
    suspend fun setServicesCatalogListView(listView: Boolean): Result<Unit>
    suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit>
    suspend fun ensureThemeInitialized(context: Context): Result<Unit>
    suspend fun ensureMigrationDone()

    /** Сохраняет результат онбординга: включённые сервисы, первый сервис, избранный (если в выборе). */
    suspend fun completeOnboarding(
        selectedServices: Set<ServiceId>,
        firstService: ServiceId,
        favoriteService: ServiceId? = null
    ): Result<Unit>
}
