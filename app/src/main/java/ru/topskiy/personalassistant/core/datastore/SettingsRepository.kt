package ru.topskiy.personalassistant.core.datastore

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ru.topskiy.personalassistant.core.model.ServiceId

/** Начальное состояние для bootstrap: один раз прочитать из DataStore. */
data class InitialSettings(
    val enabledServices: Set<ServiceId>,
    val favoriteService: ServiceId?,
    val lastService: ServiceId?,
    val onboardingDone: Boolean
)

/** Абстракция репозитория настроек (для внедрения и подмены в тестах). */
interface SettingsRepository {
    val enabledServicesFlow: Flow<Set<ServiceId>>
    val favoriteServiceFlow: Flow<ServiceId?>
    val lastServiceFlow: Flow<ServiceId?>
    val onboardingDoneFlow: Flow<Boolean>
    /** "dark" | "light" — при первом запуске вызывать [ensureThemeInitialized] для однократной инициализации по системной теме. */
    val themeFlow: Flow<String>
    /** true = вид списком, false = вид карточками. */
    val servicesCatalogListViewFlow: Flow<Boolean>
    suspend fun setServicesCatalogListView(listView: Boolean): Result<Unit>
    suspend fun setEnabledServices(set: Set<ServiceId>): Result<Unit>
    suspend fun setFavorite(value: ServiceId?): Result<Unit>
    suspend fun setLastService(value: ServiceId?): Result<Unit>
    suspend fun setOnboardingDone(done: Boolean): Result<Unit>
    suspend fun setTheme(mode: String): Result<Unit>
    /** Однократная инициализация темы при первом запуске: если тема не задана, сохраняет "light" или "dark" по системной теме. */
    suspend fun ensureThemeInitialized(context: Context): Result<Unit>
    suspend fun getInitialSettings(): Result<InitialSettings>
}

private const val DATASTORE_FILE_NAME = "settings"

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_FILE_NAME)

private val ENABLED_SERVICES_KEY = stringSetPreferencesKey("enabled_services")
private val FAVORITE_SERVICE_KEY = stringPreferencesKey("favorite_service")
private val LAST_SERVICE_KEY = stringPreferencesKey("last_service")
private val ONBOARDING_DONE_KEY = booleanPreferencesKey("onboarding_done")
private val THEME_KEY = stringPreferencesKey("theme")
/** true = список, false = сетка (карточки). По умолчанию сетка. */
private val SERVICES_CATALOG_LIST_VIEW_KEY = booleanPreferencesKey("services_catalog_list_view")

private fun String.toServiceIdOrNull(): ServiceId? = try {
    ServiceId.valueOf(this)
} catch (_: IllegalArgumentException) {
    null
}

private fun Set<String>.toServiceIdSet(): Set<ServiceId> = mapNotNull { it.toServiceIdOrNull() }.toSet()

private const val TAG = "DataStoreSettingsRepository"

class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private val dataStoreFlow = dataStore.data
        .catch { e ->
            Log.e(TAG, "DataStore flow error", e)
            emit(emptyPreferences())
        }

    override val enabledServicesFlow: Flow<Set<ServiceId>> = dataStoreFlow.map { preferences ->
        val raw = preferences[ENABLED_SERVICES_KEY]
        when {
            raw == null || raw.isEmpty() -> setOf(ServiceId.DEALS)
            else -> raw.toServiceIdSet().ifEmpty { setOf(ServiceId.DEALS) }
        }
    }

    override val favoriteServiceFlow: Flow<ServiceId?> = dataStoreFlow.map { preferences ->
        preferences[FAVORITE_SERVICE_KEY]?.toServiceIdOrNull()
    }

    override val lastServiceFlow: Flow<ServiceId?> = dataStoreFlow.map { preferences ->
        preferences[LAST_SERVICE_KEY]?.toServiceIdOrNull()
    }

    override val onboardingDoneFlow: Flow<Boolean> = dataStoreFlow.map { preferences ->
        preferences[ONBOARDING_DONE_KEY] ?: false
    }

    override val themeFlow: Flow<String> = dataStoreFlow.map { preferences ->
        preferences[THEME_KEY] ?: "light"
    }

    override val servicesCatalogListViewFlow: Flow<Boolean> = dataStoreFlow.map { preferences ->
        preferences[SERVICES_CATALOG_LIST_VIEW_KEY] ?: true
    }

    override suspend fun setServicesCatalogListView(listView: Boolean): Result<Unit> = runCatching {
        dataStore.edit { preferences ->
            preferences[SERVICES_CATALOG_LIST_VIEW_KEY] = listView
        }
        Unit
    }.also { it.onFailure { e -> Log.e(TAG, "setServicesCatalogListView failed", e) } }

    override suspend fun setEnabledServices(set: Set<ServiceId>): Result<Unit> = runCatching {
        if (set.isEmpty()) return@runCatching
        dataStore.edit { preferences ->
            preferences[ENABLED_SERVICES_KEY] = set.map { it.name }.toSet()
            val currentFavorite = preferences[FAVORITE_SERVICE_KEY]?.toServiceIdOrNull()
            if (currentFavorite != null && currentFavorite !in set) {
                preferences.remove(FAVORITE_SERVICE_KEY)
            }
        }
        Unit
    }.also { it.onFailure { e -> Log.e(TAG, "setEnabledServices failed", e) } }

    override suspend fun setFavorite(value: ServiceId?): Result<Unit> = runCatching {
        dataStore.edit { preferences ->
            val enabledRaw = preferences[ENABLED_SERVICES_KEY]
            val enabled = when {
                enabledRaw == null || enabledRaw.isEmpty() -> setOf(ServiceId.DEALS)
                else -> enabledRaw.toServiceIdSet().ifEmpty { setOf(ServiceId.DEALS) }
            }
            when {
                value == null -> preferences.remove(FAVORITE_SERVICE_KEY)
                value in enabled -> preferences[FAVORITE_SERVICE_KEY] = value.name
                else -> preferences.remove(FAVORITE_SERVICE_KEY)
            }
        }
        Unit
    }.also { it.onFailure { e -> Log.e(TAG, "setFavorite failed", e) } }

    override suspend fun setLastService(value: ServiceId?): Result<Unit> = runCatching {
        dataStore.edit { preferences ->
            if (value != null) {
                preferences[LAST_SERVICE_KEY] = value.name
            } else {
                preferences.remove(LAST_SERVICE_KEY)
            }
        }
        Unit
    }.also { it.onFailure { e -> Log.e(TAG, "setLastService failed", e) } }

    override suspend fun setOnboardingDone(done: Boolean): Result<Unit> = runCatching {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_DONE_KEY] = done
        }
        Unit
    }.also { it.onFailure { e -> Log.e(TAG, "setOnboardingDone failed", e) } }

    override suspend fun setTheme(mode: String): Result<Unit> = runCatching {
        if (mode !in listOf("dark", "light")) return@runCatching
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = mode
        }
        Unit
    }.also { it.onFailure { e -> Log.e(TAG, "setTheme failed", e) } }

    override suspend fun ensureThemeInitialized(context: Context): Result<Unit> = runCatching {
        val prefs = dataStore.data.first()
        if (prefs[THEME_KEY] != null) return@runCatching Unit
        val isDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = if (isDark) "dark" else "light"
        }
        Unit
    }.also { it.onFailure { e -> Log.e(TAG, "ensureThemeInitialized failed", e) } }

    override suspend fun getInitialSettings(): Result<InitialSettings> = runCatching {
        val prefs = dataStore.data.first()
        val enabledRaw = prefs[ENABLED_SERVICES_KEY]
        val enabledServices = when {
            enabledRaw == null || enabledRaw.isEmpty() -> setOf(ServiceId.DEALS)
            else -> enabledRaw.toServiceIdSet().ifEmpty { setOf(ServiceId.DEALS) }
        }
        InitialSettings(
            enabledServices = enabledServices,
            favoriteService = prefs[FAVORITE_SERVICE_KEY]?.toServiceIdOrNull(),
            lastService = prefs[LAST_SERVICE_KEY]?.toServiceIdOrNull(),
            onboardingDone = prefs[ONBOARDING_DONE_KEY] ?: false
        )
    }.also { it.onFailure { e -> Log.e(TAG, "getInitialSettings failed", e) } }
}
