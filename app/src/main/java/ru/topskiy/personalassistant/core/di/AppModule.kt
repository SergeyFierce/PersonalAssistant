package ru.topskiy.personalassistant.core.di

import android.content.Context
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.topskiy.personalassistant.core.datastore.DataStoreSettingsRepository
import ru.topskiy.personalassistant.core.datastore.EncryptedSettingsRepository
import ru.topskiy.personalassistant.core.datastore.SettingsRepository
import ru.topskiy.personalassistant.core.datastore.settingsDataStore
import javax.inject.Singleton

private const val TAG = "AppModule"

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SettingsRepositoryEntryPoint {
    fun getSettingsRepository(): SettingsRepository
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context
    ): SettingsRepository = try {
        EncryptedSettingsRepository(context, context.settingsDataStore)
    } catch (e: Throwable) {
        Log.e(TAG, "EncryptedSettingsRepository failed, falling back to DataStore", e)
        DataStoreSettingsRepository(context.settingsDataStore)
    }
}
