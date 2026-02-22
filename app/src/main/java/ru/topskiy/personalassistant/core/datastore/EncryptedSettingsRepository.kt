package ru.topskiy.personalassistant.core.datastore

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import ru.topskiy.personalassistant.core.model.ServiceId

private const val ENCRYPTED_PREFS_FILE = "encrypted_settings"
private const val TAG = "EncryptedSettingsRepository"

private const val KEY_ENABLED_SERVICES = "enabled_services"
private const val KEY_FAVORITE_SERVICE = "favorite_service"
private const val KEY_LAST_SERVICE = "last_service"
private const val KEY_ONBOARDING_DONE = "onboarding_done"
private const val KEY_THEME = "theme"
private const val KEY_SERVICES_CATALOG_LIST_VIEW = "services_catalog_list_view"
private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"

private fun String.toServiceIdOrNull(): ServiceId? = try {
    ServiceId.valueOf(this)
} catch (_: IllegalArgumentException) {
    null
}

private fun Set<String>.toServiceIdSet(): Set<ServiceId> = mapNotNull { it.toServiceIdOrNull() }.toSet()

private fun createEncryptedPrefs(context: Context): android.content.SharedPreferences {
    val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    return EncryptedSharedPreferences.create(
        ENCRYPTED_PREFS_FILE,
        masterKey,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}

/**
 * Реализация [SettingsRepository] на базе EncryptedSharedPreferences.
 * Настройки хранятся в зашифрованном виде. При первом запуске выполняется миграция
 * из обычного DataStore (если он был), затем используются только зашифрованные данные.
 *
 * Конструктор с [SharedPreferences] — для тестов (in-memory или без шифрования).
 */
class EncryptedSettingsRepository internal constructor(
    private val prefs: android.content.SharedPreferences,
    private val legacyDataStore: androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>
) : SettingsRepository {

    constructor(
        context: Context,
        legacyDataStore: androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>
    ) : this(createEncryptedPrefs(context), legacyDataStore)
    private val migrationMutex = Mutex()

    init {
        refreshAllFlows()
        prefs.registerOnSharedPreferenceChangeListener { _, _ -> refreshAllFlows() }
    }

    /**
     * Ленивая миграция: при первом вызове переносит данные из DataStore в EncryptedSharedPreferences
     * на [Dispatchers.IO], затем обновляет потоки. Повторные вызовы — no-op. Вызывать до первого
     * использования данных (например, из Application перед ensureThemeInitialized и из getInitialSettings).
     */
    override suspend fun ensureMigrationDone() {
        migrationMutex.withLock {
            if (prefs.getBoolean(MIGRATION_DONE_KEY, false)) return
            withContext(Dispatchers.IO) {
                try {
                    migrateDataStoreToEncryptedIfNeeded(legacyDataStore, prefs)
                } catch (e: Exception) {
                    Log.e(TAG, "Migration from DataStore failed, starting with empty encrypted prefs", e)
                    prefs.edit().putBoolean(MIGRATION_DONE_KEY, true).apply()
                }
                refreshAllFlows()
            }
        }
    }

    private val _enabledServicesFlow = MutableStateFlow(readEnabledServices())
    private val _favoriteServiceFlow = MutableStateFlow(readFavoriteService())
    private val _lastServiceFlow = MutableStateFlow(readLastService())
    private val _onboardingDoneFlow = MutableStateFlow(readOnboardingDone())
    private val _themeFlow = MutableStateFlow(readTheme())
    private val _servicesCatalogListViewFlow = MutableStateFlow(readServicesCatalogListView())
    private val _notificationsEnabledFlow = MutableStateFlow(readNotificationsEnabled())

    override val enabledServicesFlow: Flow<Set<ServiceId>> = _enabledServicesFlow.asStateFlow()
    override val favoriteServiceFlow: Flow<ServiceId?> = _favoriteServiceFlow.asStateFlow()
    override val lastServiceFlow: Flow<ServiceId?> = _lastServiceFlow.asStateFlow()
    override val onboardingDoneFlow: Flow<Boolean> = _onboardingDoneFlow.asStateFlow()
    override val themeFlow: Flow<String> = _themeFlow.asStateFlow()
    override val servicesCatalogListViewFlow: Flow<Boolean> = _servicesCatalogListViewFlow.asStateFlow()
    override val notificationsEnabledFlow: Flow<Boolean> = _notificationsEnabledFlow.asStateFlow()

    private fun refreshAllFlows() {
        _enabledServicesFlow.value = readEnabledServices()
        _favoriteServiceFlow.value = readFavoriteService()
        _lastServiceFlow.value = readLastService()
        _onboardingDoneFlow.value = readOnboardingDone()
        _themeFlow.value = readTheme()
        _servicesCatalogListViewFlow.value = readServicesCatalogListView()
        _notificationsEnabledFlow.value = readNotificationsEnabled()
    }

    private fun readEnabledServices(): Set<ServiceId> {
        val raw = prefs.getStringSet(KEY_ENABLED_SERVICES, null)
        return when {
            raw == null || raw.isEmpty() -> setOf(ServiceId.DEALS)
            else -> raw.toServiceIdSet().ifEmpty { setOf(ServiceId.DEALS) }
        }
    }

    private fun readFavoriteService(): ServiceId? =
        prefs.getString(KEY_FAVORITE_SERVICE, null)?.toServiceIdOrNull()

    private fun readLastService(): ServiceId? =
        prefs.getString(KEY_LAST_SERVICE, null)?.toServiceIdOrNull()

    private fun readOnboardingDone(): Boolean = prefs.getBoolean(KEY_ONBOARDING_DONE, false)

    private fun readTheme(): String = prefs.getString(KEY_THEME, "light") ?: "light"

    private fun readServicesCatalogListView(): Boolean = prefs.getBoolean(KEY_SERVICES_CATALOG_LIST_VIEW, true)

    private fun readNotificationsEnabled(): Boolean = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, false)

    override suspend fun setServicesCatalogListView(listView: Boolean): Result<Unit> = runCatching {
        prefs.edit().putBoolean(KEY_SERVICES_CATALOG_LIST_VIEW, listView).apply()
        _servicesCatalogListViewFlow.value = listView
        Unit
    }.also { it.onFailure { e -> Log.e(TAG, "setServicesCatalogListView failed", e) } }

    override suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit> = runCatching {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        _notificationsEnabledFlow.value = enabled
        Unit
    }.also { it.onFailure { e -> Log.e(TAG, "setNotificationsEnabled failed", e) } }

    override suspend fun setEnabledServices(set: Set<ServiceId>): Result<Unit> = runCatching {
        if (set.isEmpty()) return@runCatching
        prefs.edit()
            .putStringSet(KEY_ENABLED_SERVICES, set.map { it.name }.toSet())
            .apply()
        val currentFavorite = readFavoriteService()
        if (currentFavorite != null && currentFavorite !in set) {
            prefs.edit().remove(KEY_FAVORITE_SERVICE).apply()
            _favoriteServiceFlow.value = null
        }
        _enabledServicesFlow.value = set
        Unit
    }.also { it.onFailure { e -> Log.e(TAG, "setEnabledServices failed", e) } }

    override suspend fun setFavorite(value: ServiceId?): Result<Unit> = runCatching {
        val enabled = readEnabledServices()
        when {
            value == null -> prefs.edit().remove(KEY_FAVORITE_SERVICE).apply()
            value in enabled -> prefs.edit().putString(KEY_FAVORITE_SERVICE, value.name).apply()
            else -> prefs.edit().remove(KEY_FAVORITE_SERVICE).apply()
        }
        _favoriteServiceFlow.value = if (value in enabled) value else null
        Unit
    }.also { it.onFailure { e -> Log.e(TAG, "setFavorite failed", e) } }

    override suspend fun setLastService(value: ServiceId?): Result<Unit> = runCatching {
        prefs.edit().apply {
            if (value != null) putString(KEY_LAST_SERVICE, value.name)
            else remove(KEY_LAST_SERVICE)
        }.apply()
        _lastServiceFlow.value = value
        Unit
    }.also { it.onFailure { e -> Log.e(TAG, "setLastService failed", e) } }

    override suspend fun setOnboardingDone(done: Boolean): Result<Unit> = runCatching {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, done).apply()
        _onboardingDoneFlow.value = done
        Unit
    }.also { it.onFailure { e -> Log.e(TAG, "setOnboardingDone failed", e) } }

    override suspend fun setTheme(mode: String): Result<Unit> = runCatching {
        if (mode !in listOf("dark", "light")) return@runCatching
        prefs.edit().putString(KEY_THEME, mode).apply()
        _themeFlow.value = mode
        Unit
    }.also { it.onFailure { e -> Log.e(TAG, "setTheme failed", e) } }

    override suspend fun ensureThemeInitialized(context: Context): Result<Unit> = runCatching {
        if (prefs.contains(KEY_THEME)) return@runCatching Unit
        val isDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        val mode = if (isDark) "dark" else "light"
        prefs.edit().putString(KEY_THEME, mode).apply()
        _themeFlow.value = mode
        Unit
    }.also { it.onFailure { e -> Log.e(TAG, "ensureThemeInitialized failed", e) } }

    override suspend fun getInitialSettings(): Result<InitialSettings> = runCatching {
        ensureMigrationDone()
        InitialSettings(
            enabledServices = readEnabledServices(),
            favoriteService = readFavoriteService(),
            lastService = readLastService(),
            onboardingDone = readOnboardingDone()
        )
    }.also { it.onFailure { e -> Log.e(TAG, "getInitialSettings failed", e) } }
}
