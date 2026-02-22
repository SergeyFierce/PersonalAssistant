package ru.topskiy.personalassistant.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.NoOpCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class MigrationTest {

    companion object {
        private var dataStoreDirCounter = 0
    }

    private fun createDataStore(): DataStore<Preferences> {
        dataStoreDirCounter++
        val testDir = File("build/tmp", "migrationTest_$dataStoreDirCounter")
        return PreferenceDataStoreFactory.create(
            corruptionHandler = NoOpCorruptionHandler(),
            produceFile = { File(testDir, "settings.preferences_pb") }
        )
    }

    private fun createTargetPrefs(): InMemorySharedPreferences = InMemorySharedPreferences()

    @Test
    fun `migrateDataStoreToEncryptedIfNeeded copies all keys and sets migration_done`() = runTest {
        val dataStore = createDataStore()
        val enabledSet = setOf("DEALS", "NOTES")
        val favorite = "CREDITS"
        val last = "NOTES"
        val theme = "dark"
        dataStore.edit { prefs ->
            prefs[stringSetPreferencesKey("enabled_services")] = enabledSet
            prefs[stringPreferencesKey("favorite_service")] = favorite
            prefs[stringPreferencesKey("last_service")] = last
            prefs[booleanPreferencesKey("onboarding_done")] = true
            prefs[stringPreferencesKey("theme")] = theme
            prefs[booleanPreferencesKey("services_catalog_list_view")] = false
            prefs[booleanPreferencesKey("notifications_enabled")] = true
        }

        val targetPrefs = createTargetPrefs()

        migrateDataStoreToEncryptedIfNeeded(dataStore, targetPrefs)

        assertEquals(enabledSet, targetPrefs.getStringSet("enabled_services", null))
        assertEquals(favorite, targetPrefs.getString("favorite_service", null))
        assertEquals(last, targetPrefs.getString("last_service", null))
        assertTrue(targetPrefs.getBoolean("onboarding_done", false))
        assertEquals(theme, targetPrefs.getString("theme", null))
        assertFalse(targetPrefs.getBoolean("services_catalog_list_view", true))
        assertTrue(targetPrefs.getBoolean("notifications_enabled", false))
        assertTrue(targetPrefs.getBoolean(MIGRATION_DONE_KEY, false))
    }

    @Test
    fun `migrateDataStoreToEncryptedIfNeeded with empty DataStore writes defaults and migration_done`() = runTest {
        val dataStore = createDataStore()
        val targetPrefs = createTargetPrefs()

        migrateDataStoreToEncryptedIfNeeded(dataStore, targetPrefs)

        assertEquals(null, targetPrefs.getStringSet("enabled_services", null))
        assertEquals(null, targetPrefs.getString("favorite_service", null))
        assertEquals(null, targetPrefs.getString("last_service", null))
        assertFalse(targetPrefs.getBoolean("onboarding_done", true))
        assertEquals(null, targetPrefs.getString("theme", null))
        assertTrue(targetPrefs.getBoolean("services_catalog_list_view", false))
        assertFalse(targetPrefs.getBoolean("notifications_enabled", true))
        assertTrue(targetPrefs.getBoolean(MIGRATION_DONE_KEY, false))
    }

    @Test
    fun `second call to migrateDataStoreToEncryptedIfNeeded does not overwrite target`() = runTest {
        val dataStore = createDataStore()
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey("theme")] = "first"
            prefs[booleanPreferencesKey("onboarding_done")] = true
        }
        val targetPrefs = createTargetPrefs()

        migrateDataStoreToEncryptedIfNeeded(dataStore, targetPrefs)
        assertEquals("first", targetPrefs.getString("theme", null))

        dataStore.edit { prefs ->
            prefs[stringPreferencesKey("theme")] = "second"
            prefs[booleanPreferencesKey("onboarding_done")] = false
        }

        migrateDataStoreToEncryptedIfNeeded(dataStore, targetPrefs)

        assertEquals("first", targetPrefs.getString("theme", null))
        assertTrue(targetPrefs.getBoolean("onboarding_done", false))
    }
}
