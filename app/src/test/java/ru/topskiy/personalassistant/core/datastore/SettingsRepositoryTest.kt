package ru.topskiy.personalassistant.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.NoOpCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import ru.topskiy.personalassistant.core.model.ServiceId
import java.io.File
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryTest {

    companion object {
        private const val SETTINGS_DATASTORE_FILENAME = "settings.preferences_pb"
        private const val TEST_TMP_BASE = "build/tmp"
        private const val TEST_DIR_COUNTER_INITIAL = 0
        private var testDirCounter = TEST_DIR_COUNTER_INITIAL
    }

    private fun createRepository(testDir: File): SettingsRepository {
        val dataStore = PreferenceDataStoreFactory.create(
            corruptionHandler = NoOpCorruptionHandler(),
            produceFile = { File(testDir, SETTINGS_DATASTORE_FILENAME) }
        )
        return DataStoreSettingsRepository(dataStore)
    }

    private fun createRepository(): SettingsRepository {
        testDirCounter++
        return createRepository(File(TEST_TMP_BASE, "settingsRepoTest_$testDirCounter"))
    }

    @Test
    fun `enabledServicesFlow falls back to DEALS when empty`() = runTest {
        val repo = createRepository()

        val enabled = repo.enabledServicesFlow.first()

        assertEquals(setOf(ServiceId.DEALS), enabled)
    }

    @Test
    fun `setEnabledServices writes and reads back ids by name`() = runTest {
        val repo = createRepository()
        val set = setOf(ServiceId.DEALS, ServiceId.NOTES, ServiceId.CREDITS)

        val result = repo.setEnabledServices(set)

        assert(result.isSuccess)
        val enabled = repo.enabledServicesFlow.first()
        assertEquals(set, enabled)
    }

    @Test
    fun `favoriteServiceFlow serializes and deserializes ServiceId`() = runTest {
        val repo = createRepository()

        // По умолчанию null
        assertNull(repo.favoriteServiceFlow.first())

        val favorite = ServiceId.SUBSCRIPTIONS
        val setResult = repo.setFavorite(favorite)
        assert(setResult.isSuccess)

        val loaded = repo.favoriteServiceFlow.first()
        assertEquals(favorite, loaded)
    }

    @Test
    fun `getInitialSettings returns defaults when nothing stored`() = runTest {
        val repo = createRepository()

        val result = repo.getInitialSettings()

        assert(result.isSuccess)
        val settings = result.getOrThrow()
        assertEquals(setOf(ServiceId.DEALS), settings.enabledServices)
        assertNull(settings.favoriteService)
        assertNull(settings.lastService)
        assertEquals(false, settings.onboardingDone)
    }

    @Test
    fun `themeFlow reflects setTheme and ignores invalid values`() = runTest {
        val repo = createRepository()

        // По умолчанию light (fallback)
        assertEquals("light", repo.themeFlow.first())

        val darkResult = repo.setTheme("dark")
        assert(darkResult.isSuccess)
        assertEquals("dark", repo.themeFlow.first())

        // Неверное значение не должно менять сохранённую тему
        val invalidResult = repo.setTheme("system")
        assert(invalidResult.isSuccess)
        assertEquals("dark", repo.themeFlow.first())
    }

    @Test
    fun `servicesCatalogListViewFlow stores and reads back value`() = runTest {
        val repo = createRepository()

        // По умолчанию true (список)
        assertEquals(true, repo.servicesCatalogListViewFlow.first())

        val result = repo.setServicesCatalogListView(listView = false)
        assert(result.isSuccess)
        assertEquals(false, repo.servicesCatalogListViewFlow.first())
    }

    @Test
    fun `notificationsEnabledFlow defaults to false and setNotificationsEnabled persists`() = runTest {
        val repo = createRepository()

        assertEquals(false, repo.notificationsEnabledFlow.first())

        val setResult = repo.setNotificationsEnabled(true)
        assert(setResult.isSuccess)
        assertEquals(true, repo.notificationsEnabledFlow.first())

        repo.setNotificationsEnabled(false)
        assertEquals(false, repo.notificationsEnabledFlow.first())
    }

    @Test
    fun `flows fall back to defaults when DataStore throws`() = runTest {
        val failingDataStore = object : DataStore<Preferences> {
            override val data: Flow<Preferences> = flow {
                throw IOException("Test DataStore failure")
            }

            override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
                throw UnsupportedOperationException("Not used in this test")
            }
        }

        val repo = DataStoreSettingsRepository(failingDataStore)

        // При ошибке чтения DataStore должны возвращаться значения по умолчанию
        assertEquals(setOf(ServiceId.DEALS), repo.enabledServicesFlow.first())
        assertNull(repo.favoriteServiceFlow.first())
        assertEquals(false, repo.onboardingDoneFlow.first())
        assertEquals("light", repo.themeFlow.first())
        assertEquals(true, repo.servicesCatalogListViewFlow.first())
        assertEquals(false, repo.notificationsEnabledFlow.first())
    }
}

