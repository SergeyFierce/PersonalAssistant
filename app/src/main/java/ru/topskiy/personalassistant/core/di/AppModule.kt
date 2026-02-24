package ru.topskiy.personalassistant.core.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.topskiy.personalassistant.core.data.AppDatabase
import ru.topskiy.personalassistant.core.data.notes.NotesDao
import ru.topskiy.personalassistant.core.data.notes.NotesRepository
import ru.topskiy.personalassistant.core.data.notes.NotesRepositoryImpl
import ru.topskiy.personalassistant.core.datastore.DataStoreSettingsRepository
import ru.topskiy.personalassistant.core.datastore.EncryptedSettingsRepository
import ru.topskiy.personalassistant.core.datastore.SettingsRepository
import ru.topskiy.personalassistant.core.datastore.settingsDataStore
import ru.topskiy.personalassistant.core.domain.SettingsUseCase
import ru.topskiy.personalassistant.core.domain.SettingsUseCaseImpl
import ru.topskiy.personalassistant.core.domain.notes.NotesUseCase
import ru.topskiy.personalassistant.core.domain.notes.NotesUseCaseImpl
import javax.inject.Singleton

private const val TAG = "AppModule"

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SettingsRepositoryEntryPoint {
    fun getSettingsRepository(): SettingsRepository
    fun getSettingsUseCase(): SettingsUseCase
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

    @Provides
    @Singleton
    fun provideSettingsUseCase(repository: SettingsRepository): SettingsUseCase =
        SettingsUseCaseImpl(repository)

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "personal_assistant.db"
        ).fallbackToDestructiveMigrationOnDowngrade()
            .build()

    @Provides
    fun provideNotesDao(
        database: AppDatabase
    ): NotesDao = database.notesDao()

    @Provides
    @Singleton
    fun provideNotesRepository(
        notesDao: NotesDao
    ): NotesRepository = NotesRepositoryImpl(notesDao)

    @Provides
    @Singleton
    fun provideNotesUseCase(
        repository: NotesRepository
    ): NotesUseCase = NotesUseCaseImpl(repository)
}
