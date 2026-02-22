package ru.topskiy.personalassistant

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.withContext
import ru.topskiy.personalassistant.core.di.SettingsRepositoryEntryPoint

/**
 * Точка входа приложения. Инициализирует Firebase, Crashlytics и тему при первом запуске.
 *
 * До отображения первого экрана нужно один раз инициализировать тему (light/dark) по системным
 * настройкам — это делается в [SettingsRepository.ensureThemeInitialized]. Activity и Hilt-граф для
 * неё создаются позже, поэтому репозиторий берём через [EntryPointAccessors]: EntryPoint даёт
 * доступ к отдельным зависимостям из графа без внедрения всей Activity.
 */
@HiltAndroidApp
class PersonalAssistantApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        // Ранний доступ к репозиторию через EntryPoint: инициализация темы до первого экрана.
        ProcessLifecycleOwner.get().lifecycleScope.launch(Dispatchers.Main.immediate) {
            try {
                val repo = EntryPointAccessors.fromApplication(
                    this@PersonalAssistantApp,
                    SettingsRepositoryEntryPoint::class.java
                ).getSettingsRepository()
                withContext(Dispatchers.IO) {
                    repo.ensureThemeInitialized(this@PersonalAssistantApp)
                }
            } catch (e: Throwable) {
                Log.e("PersonalAssistantApp", "Theme init failed", e)
            }
        }
    }
}
