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
import kotlinx.coroutines.withContext
import ru.topskiy.personalassistant.core.di.SettingsRepositoryEntryPoint

@HiltAndroidApp
class PersonalAssistantApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        ProcessLifecycleOwner.get().lifecycleScope.launch(Dispatchers.Main.immediate) {
            val repo = EntryPointAccessors.fromApplication(
                this@PersonalAssistantApp,
                SettingsRepositoryEntryPoint::class.java
            ).getSettingsRepository()
            withContext(Dispatchers.IO) {
                repo.ensureThemeInitialized(this@PersonalAssistantApp)
            }
        }
    }
}
