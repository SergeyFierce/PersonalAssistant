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
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import ru.topskiy.personalassistant.core.model.ServiceId
import java.io.File

/**
 * Unit-тесты [EncryptedSettingsRepository] с in-memory [SharedPreferences]
 * (без реального шифрования; проверяется логика чтения/записи и потоков).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EncryptedSettingsRepositoryTest {

    companion object {
        private var dirCounter = 0
    }

    private fun createDataStore(): DataStore<Preferences> {
        dirCounter++
        val dir = File("build/tmp", "encryptedRepoTest_$dirCounter")
        return PreferenceDataStoreFactory.create(
            corruptionHandler = NoOpCorruptionHandler(),
            produceFile = { File(dir, "settings.preferences_pb") }
        )
    }

    private fun createRepository(): Pair<EncryptedSettingsRepository, InMemorySharedPreferences> {
        val prefs = InMemorySharedPreferences()
        val dataStore = createDataStore()
        val repo = EncryptedSettingsRepository(prefs, dataStore)
        return repo to prefs
    }

    @Test
    fun `flows and getInitialSettings return defaults when prefs empty`() = runTest {
        val (repo, _) = createRepository()
        repo.ensureMigrationDone()

        assertEquals(setOf(ServiceId.DEALS), repo.enabledServicesFlow.first())
        assertNull(repo.favoriteServiceFlow.first())
        assertNull(repo.lastServiceFlow.first())
        assertFalse(repo.onboardingDoneFlow.first())
        assertEquals("light", repo.themeFlow.first())
        assertTrue(repo.servicesCatalogListViewFlow.first())
        assertFalse(repo.notificationsEnabledFlow.first())

        val initial = repo.getInitialSettings().getOrThrow()
        assertEquals(setOf(ServiceId.DEALS), initial.enabledServices)
        assertNull(initial.favoriteService)
        assertNull(initial.lastService)
        assertFalse(initial.onboardingDone)
    }

    @Test
    fun `setEnabledServices persists and flow updates`() = runTest {
        val (repo, _) = createRepository()
        repo.ensureMigrationDone()

        val set = setOf(ServiceId.DEALS, ServiceId.NOTES, ServiceId.CREDITS)
        val result = repo.setEnabledServices(set)

        assertTrue(result.isSuccess)
        assertEquals(set, repo.enabledServicesFlow.first())
        assertEquals(set, repo.getInitialSettings().getOrThrow().enabledServices)
    }

    @Test
    fun `setTheme persists and flow updates`() = runTest {
        val (repo, _) = createRepository()
        repo.ensureMigrationDone()

        repo.setTheme("dark")

        assertEquals("dark", repo.themeFlow.first())
        repo.setTheme("light")
        assertEquals("light", repo.themeFlow.first())
    }

    @Test
    fun `setTheme ignores invalid value`() = runTest {
        val (repo, _) = createRepository()
        repo.ensureMigrationDone()

        repo.setTheme("dark")
        repo.setTheme("system")

        assertEquals("dark", repo.themeFlow.first())
    }

    @Test
    fun `setNotificationsEnabled persists and flow updates`() = runTest {
        val (repo, _) = createRepository()
        repo.ensureMigrationDone()

        repo.setNotificationsEnabled(true)

        assertTrue(repo.notificationsEnabledFlow.first())
        repo.setNotificationsEnabled(false)
        assertFalse(repo.notificationsEnabledFlow.first())
    }

    @Test
    fun `setFavorite and setLastService persist and flows update`() = runTest {
        val (repo, _) = createRepository()
        repo.ensureMigrationDone()
        repo.setEnabledServices(setOf(ServiceId.DEALS, ServiceId.NOTES, ServiceId.CREDITS))

        repo.setFavorite(ServiceId.CREDITS)
        assertEquals(ServiceId.CREDITS, repo.favoriteServiceFlow.first())
        repo.setLastService(ServiceId.NOTES)
        assertEquals(ServiceId.NOTES, repo.lastServiceFlow.first())

        val initial = repo.getInitialSettings().getOrThrow()
        assertEquals(ServiceId.CREDITS, initial.favoriteService)
        assertEquals(ServiceId.NOTES, initial.lastService)

        repo.setFavorite(null)
        repo.setLastService(null)
        assertNull(repo.favoriteServiceFlow.first())
        assertNull(repo.lastServiceFlow.first())
    }

    @Test
    fun `setOnboardingDone and setServicesCatalogListView persist and flows update`() = runTest {
        val (repo, _) = createRepository()
        repo.ensureMigrationDone()

        repo.setOnboardingDone(true)
        assertTrue(repo.onboardingDoneFlow.first())
        assertEquals(true, repo.getInitialSettings().getOrThrow().onboardingDone)

        repo.setServicesCatalogListView(false)
        assertFalse(repo.servicesCatalogListViewFlow.first())
    }

    @Test
    fun `setEnabledServices clears favorite when favorite not in new set`() = runTest {
        val (repo, _) = createRepository()
        repo.ensureMigrationDone()
        repo.setEnabledServices(setOf(ServiceId.DEALS, ServiceId.NOTES, ServiceId.CREDITS))
        repo.setFavorite(ServiceId.CREDITS)

        repo.setEnabledServices(setOf(ServiceId.DEALS, ServiceId.NOTES))

        assertEquals(setOf(ServiceId.DEALS, ServiceId.NOTES), repo.enabledServicesFlow.first())
        assertNull(repo.favoriteServiceFlow.first())
    }

    @Test
    fun `migration from DataStore fills prefs and flows`() = runTest {
        val dataStore = createDataStore()
        val prefs = InMemorySharedPreferences()
        dataStore.edit { p ->
            p[stringSetPreferencesKey("enabled_services")] = setOf("DEALS", "NOTES")
            p[stringPreferencesKey("favorite_service")] = "NOTES"
            p[stringPreferencesKey("last_service")] = "DEALS"
            p[booleanPreferencesKey("onboarding_done")] = true
            p[stringPreferencesKey("theme")] = "dark"
            p[booleanPreferencesKey("services_catalog_list_view")] = false
            p[booleanPreferencesKey("notifications_enabled")] = true
        }

        val repo = EncryptedSettingsRepository(prefs, dataStore)
        repo.ensureMigrationDone()

        assertEquals(setOf(ServiceId.DEALS, ServiceId.NOTES), repo.enabledServicesFlow.first())
        assertEquals(ServiceId.NOTES, repo.favoriteServiceFlow.first())
        assertEquals(ServiceId.DEALS, repo.lastServiceFlow.first())
        assertTrue(repo.onboardingDoneFlow.first())
        assertEquals("dark", repo.themeFlow.first())
        assertFalse(repo.servicesCatalogListViewFlow.first())
        assertTrue(repo.notificationsEnabledFlow.first())

        val initial = repo.getInitialSettings().getOrThrow()
        assertEquals(setOf(ServiceId.DEALS, ServiceId.NOTES), initial.enabledServices)
        assertEquals(ServiceId.NOTES, initial.favoriteService)
        assertEquals(ServiceId.DEALS, initial.lastService)
        assertTrue(initial.onboardingDone)
    }

    @Test
    fun `ensureMigrationDone is idempotent`() = runTest {
        val (repo, _) = createRepository()

        // Первый вызов: миграция (при пустом DataStore) и инициализация потоков.
        repo.ensureMigrationDone()
        val enabledAfterFirst = repo.enabledServicesFlow.first()
        val themeAfterFirst = repo.themeFlow.first()

        // Повторный вызов не должен менять состояние и не должен падать.
        repo.ensureMigrationDone()
        val enabledAfterSecond = repo.enabledServicesFlow.first()
        val themeAfterSecond = repo.themeFlow.first()

        assertEquals(enabledAfterFirst, enabledAfterSecond)
        assertEquals(themeAfterFirst, themeAfterSecond)
    }
}
