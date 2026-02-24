package ru.topskiy.personalassistant.core.data.notes

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с таблицей [notes].
 *
 * Список заметок для главного экрана возвращается в стиле списка чатов:
 * закреплённые заметки сверху, дальше — по дате последнего обновления.
 */
@Dao
interface NotesDao {

    /**
     * Наблюдает за всеми заметками, которые не помечены как удалённые.
     *
     * Сортировка:
     * - сначала pinned = 1,
     * - внутри групп — по updatedAt по убыванию.
     */
    @Query(
        """
        SELECT * FROM notes
        WHERE isDeleted = 0
        ORDER BY pinned DESC, updatedAt DESC
        """
    )
    fun observeNotes(): Flow<List<NoteEntity>>

    /**
     * Возвращает заметку по идентификатору или null, если она отсутствует.
     */
    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNote(id: Long): NoteEntity?

    /**
     * Создаёт или обновляет заметку. При id = 0L Room сгенерирует новый идентификатор.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(entity: NoteEntity): Long

    /**
     * Помечает заметку как мягко удалённую и обновляет метки локальных изменений.
     */
    @Query(
        """
        UPDATE notes
        SET isDeleted = 1,
            hasLocalChanges = 1,
            lastLocalChangeAt = :timestamp,
            updatedAt = :timestamp
        WHERE id = :id
        """
    )
    suspend fun markDeleted(id: Long, timestamp: Long)

    /**
     * Переключает флаг pinned и обновляет время последнего изменения/локальных изменений.
     */
    @Query(
        """
        UPDATE notes
        SET pinned = NOT pinned,
            hasLocalChanges = 1,
            lastLocalChangeAt = :timestamp,
            updatedAt = :timestamp
        WHERE id = :id AND isDeleted = 0
        """
    )
    suspend fun togglePinned(id: Long, timestamp: Long)
}

