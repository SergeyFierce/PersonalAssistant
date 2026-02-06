package ru.topsky.personalassistant.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.topsky.personalassistant.core.model.ServiceId

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

private val ENABLED_SERVICES_KEY = stringSetPreferencesKey("enabled_services")
private val FAVORITE_SERVICE_KEY = stringPreferencesKey("favorite_service")
private val LAST_SERVICE_KEY = stringPreferencesKey("last_service")
private val ONBOARDING_DONE_KEY = booleanPreferencesKey("onboarding_done")
private val THEME_KEY = stringPreferencesKey("theme")

private fun String.toServiceIdOrNull(): ServiceId? = try {
    ServiceId.valueOf(this)
} catch (_: IllegalArgumentException) {
    null
}

private fun Set<String>.toServiceIdSet(): Set<ServiceId> = mapNotNull { it.toServiceIdOrNull() }.toSet()

class SettingsRepository(context: Context) {

    private val dataStore = context.settingsDataStore

    val enabledServicesFlow: Flow<Set<ServiceId>> = dataStore.data.map { preferences ->
        val raw = preferences[ENABLED_SERVICES_KEY]
        when {
            raw == null || raw.isEmpty() -> setOf(ServiceId.DEALS)
            else -> raw.toServiceIdSet().ifEmpty { setOf(ServiceId.DEALS) }
        }
    }

    val favoriteServiceFlow: Flow<ServiceId?> = dataStore.data.map { preferences ->
        preferences[FAVORITE_SERVICE_KEY]?.toServiceIdOrNull()
    }

    val lastServiceFlow: Flow<ServiceId?> = dataStore.data.map { preferences ->
        preferences[LAST_SERVICE_KEY]?.toServiceIdOrNull()
    }

    val onboardingDoneFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[ONBOARDING_DONE_KEY] ?: false
    }

    /** "dark" | "light" | "system" */
    val themeFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: "system"
    }

    suspend fun setEnabledServices(set: Set<ServiceId>) {
        if (set.isEmpty()) return
        dataStore.edit { preferences ->
            preferences[ENABLED_SERVICES_KEY] = set.map { it.name }.toSet()
            val currentFavorite = preferences[FAVORITE_SERVICE_KEY]?.toServiceIdOrNull()
            if (currentFavorite != null && currentFavorite !in set) {
                preferences.remove(FAVORITE_SERVICE_KEY)
            }
        }
    }

    suspend fun setFavorite(value: ServiceId?) {
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

    suspend fun setLastService(value: ServiceId?) {
        dataStore.edit { preferences ->
            if (value != null) {
                preferences[LAST_SERVICE_KEY] = value.name
            } else {
                preferences.remove(LAST_SERVICE_KEY)
            }
        }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_DONE_KEY] = done
        }
    }

    suspend fun setTheme(mode: String) {
        if (mode !in listOf("dark", "light", "system")) return
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = mode
        }
    }
}
