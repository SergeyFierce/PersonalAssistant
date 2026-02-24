package ru.topskiy.personalassistant.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.topskiy.personalassistant.core.data.notes.NoteEntity
import ru.topskiy.personalassistant.core.data.notes.NotesDao

/**
 * Общая база данных приложения.
 *
 * Сейчас содержит только таблицу заметок, но в будущем сюда можно добавить сущности
 * других доменов (дела, финансы и т.д.).
 */
@Database(
    entities = [
        NoteEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun notesDao(): NotesDao
}

