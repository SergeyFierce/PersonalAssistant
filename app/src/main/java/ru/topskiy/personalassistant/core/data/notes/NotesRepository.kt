package ru.topskiy.personalassistant.core.data.notes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.topskiy.personalassistant.core.data.RepositoryResult
import ru.topskiy.personalassistant.core.model.Note
import ru.topskiy.personalassistant.core.model.NoteId

/**
 * Репозиторий заметок: инкапсулирует работу с локальным хранилищем (Room) и,
 * в будущем, удалёнными источниками.
 */
interface NotesRepository {

    /**
     * Наблюдение за всеми заметками, видимыми в списке.
     *
     * Сортировка и фильтрация (pinned сверху, isDeleted = 0) обеспечиваются на уровне DAO.
     */
    fun observeNotes(): Flow<List<Note>>

    suspend fun getNote(id: NoteId): RepositoryResult<Note?>

    suspend fun createNote(title: String, body: String): RepositoryResult<Note>

    suspend fun updateNote(note: Note): RepositoryResult<Note>

    suspend fun togglePinned(id: NoteId): RepositoryResult<Unit>

    suspend fun softDelete(id: NoteId): RepositoryResult<Unit>
}

/**
 * Реализация [NotesRepository] поверх [NotesDao].
 *
 * Следит за полями времени и локальных изменений:
 * - при создании/обновлении/удалении обновляет updatedAt, hasLocalChanges, lastLocalChangeAt;
 * - мягкое удаление — пометка isDeleted = true.
 */
class NotesRepositoryImpl(
    private val dao: NotesDao,
    private val timeProvider: () -> Long = { System.currentTimeMillis() }
) : NotesRepository {

    override fun observeNotes(): Flow<List<Note>> =
        dao.observeNotes().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getNote(id: NoteId): RepositoryResult<Note?> = runCatching {
        dao.getNote(id.value)?.toDomain()
    }

    override suspend fun createNote(title: String, body: String): RepositoryResult<Note> = runCatching {
        val now = timeProvider()
        val entity = NoteEntity(
            title = title,
            body = body,
            createdAt = now,
            updatedAt = now,
            pinned = false,
            isDeleted = false,
            hasLocalChanges = true,
            lastLocalChangeAt = now
        )
        val id = dao.upsertNote(entity)
        dao.getNote(id)?.toDomain()
            ?: error("Failed to load note after insert with id=$id")
    }

    override suspend fun updateNote(note: Note): RepositoryResult<Note> = runCatching {
        val now = timeProvider()
        val entity = note.toEntity().copy(
            updatedAt = now,
            hasLocalChanges = true,
            lastLocalChangeAt = now
        )
        val id = dao.upsertNote(entity)
        dao.getNote(id)?.toDomain()
            ?: error("Failed to load note after update with id=$id")
    }

    override suspend fun togglePinned(id: NoteId): RepositoryResult<Unit> = runCatching {
        val now = timeProvider()
        dao.togglePinned(id.value, now)
    }

    override suspend fun softDelete(id: NoteId): RepositoryResult<Unit> = runCatching {
        val now = timeProvider()
        dao.markDeleted(id.value, now)
    }
}

private fun NoteEntity.toDomain(): Note =
    Note(
        id = NoteId(id),
        title = title,
        body = body,
        createdAt = createdAt,
        updatedAt = updatedAt,
        pinned = pinned,
        isDeleted = isDeleted,
        hasLocalChanges = hasLocalChanges,
        lastLocalChangeAt = lastLocalChangeAt
    )

private fun Note.toEntity(): NoteEntity =
    NoteEntity(
        id = id.value,
        title = title,
        body = body,
        createdAt = createdAt,
        updatedAt = updatedAt,
        pinned = pinned,
        isDeleted = isDeleted,
        hasLocalChanges = hasLocalChanges,
        lastLocalChangeAt = lastLocalChangeAt
    )

