package ru.topskiy.personalassistant.core.datastore

import android.content.Context
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

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

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

class SettingsRepository(context: Context) {

    private val dataStore = context.settingsDataStore

    private val dataStoreFlow = dataStore.data
        .catch { _ -> emit(emptyPreferences()) }

    val enabledServicesFlow: Flow<Set<ServiceId>> = dataStoreFlow.map { preferences ->
        val raw = preferences[ENABLED_SERVICES_KEY]
        when {
            raw == null || raw.isEmpty() -> setOf(ServiceId.DEALS)
            else -> raw.toServiceIdSet().ifEmpty { setOf(ServiceId.DEALS) }
        }
    }

    val favoriteServiceFlow: Flow<ServiceId?> = dataStoreFlow.map { preferences ->
        preferences[FAVORITE_SERVICE_KEY]?.toServiceIdOrNull()
    }

    val lastServiceFlow: Flow<ServiceId?> = dataStoreFlow.map { preferences ->
        preferences[LAST_SERVICE_KEY]?.toServiceIdOrNull()
    }

    val onboardingDoneFlow: Flow<Boolean> = dataStoreFlow.map { preferences ->
        preferences[ONBOARDING_DONE_KEY] ?: false
    }

    /** "dark" | "light" | "system" */
    val themeFlow: Flow<String> = dataStoreFlow.map { preferences ->
        preferences[THEME_KEY] ?: "system"
    }

    /** true = вид списком, false = вид карточками. По умолчанию карточки. */
    val servicesCatalogListViewFlow: Flow<Boolean> = dataStoreFlow.map { preferences ->
        preferences[SERVICES_CATALOG_LIST_VIEW_KEY] ?: false
    }

    suspend fun setServicesCatalogListView(listView: Boolean): Result<Unit> = runCatching {
        dataStore.edit { preferences ->
            preferences[SERVICES_CATALOG_LIST_VIEW_KEY] = listView
        }
    }

    suspend fun setEnabledServices(set: Set<ServiceId>): Result<Unit> = runCatching {
        if (set.isEmpty()) return@runCatching
        dataStore.edit { preferences ->
            preferences[ENABLED_SERVICES_KEY] = set.map { it.name }.toSet()
            val currentFavorite = preferences[FAVORITE_SERVICE_KEY]?.toServiceIdOrNull()
            if (currentFavorite != null && currentFavorite !in set) {
                preferences.remove(FAVORITE_SERVICE_KEY)
            }
        }
    }

    suspend fun setFavorite(value: ServiceId?): Result<Unit> = runCatching {
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
    }

    suspend fun setLastService(value: ServiceId?): Result<Unit> = runCatching {
        dataStore.edit { preferences ->
            if (value != null) {
                preferences[LAST_SERVICE_KEY] = value.name
            } else {
                preferences.remove(LAST_SERVICE_KEY)
            }
        }
    }

    suspend fun setOnboardingDone(done: Boolean): Result<Unit> = runCatching {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_DONE_KEY] = done
        }
    }

    suspend fun setTheme(mode: String): Result<Unit> = runCatching {
        if (mode !in listOf("dark", "light", "system")) return@runCatching
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = mode
        }
    }

    /** Читает сохранённые настройки один раз (для корректного старта без гонки с stateIn). При ошибке — fallback к дефолтам. */
    suspend fun getInitialSettings(): Result<InitialSettings> = runCatching {
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
    }
}
