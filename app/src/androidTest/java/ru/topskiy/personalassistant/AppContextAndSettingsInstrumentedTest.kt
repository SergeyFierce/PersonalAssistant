package ru.topskiy.personalassistant

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import ru.topskiy.personalassistant.core.di.SettingsRepositoryEntryPoint

/**
 * Instrumented-тест: контекст приложения и доступность настроек через Hilt/DataStore.
 */
@RunWith(AndroidJUnit4::class)
class AppContextAndSettingsInstrumentedTest {

    @Test
    fun appContextHasCorrectPackage() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertTrue(appContext.packageName == "ru.topskiy.personalassistant")
    }

    @Test
    fun settingsRepositoryProvidesInitialSettings() = runBlocking {
        val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as PersonalAssistantApp
        val repo = EntryPointAccessors.fromApplication(app, SettingsRepositoryEntryPoint::class.java)
            .getSettingsRepository()
        val result = repo.getInitialSettings()
        assertTrue(result.isSuccess)
        val settings = result.getOrThrow()
        assertTrue(settings.enabledServices.isNotEmpty())
    }
}
