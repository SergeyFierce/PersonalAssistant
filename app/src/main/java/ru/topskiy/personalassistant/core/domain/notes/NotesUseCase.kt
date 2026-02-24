package ru.topskiy.personalassistant.core.domain.notes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.topskiy.personalassistant.core.data.RepositoryResult
import ru.topskiy.personalassistant.core.data.notes.NotesRepository
import ru.topskiy.personalassistant.core.model.Note
import ru.topskiy.personalassistant.core.model.NoteId

/**
 * Use case домена «Заметки»: инкапсулирует бизнес‑правила поверх репозитория.
 */
interface NotesUseCase {

    /**
     * Поток заметок для отображения в UI.
     */
    val notesFlow: Flow<List<Note>>

    suspend fun getNote(id: NoteId): Result<Note?>

    suspend fun createNote(title: String, body: String): Result<Note>

    suspend fun updateNoteContent(id: NoteId, title: String, body: String): Result<Note>

    suspend fun togglePinned(id: NoteId): Result<Unit>

    suspend fun deleteNote(id: NoteId): Result<Unit>
}

class NotesUseCaseImpl(
    private val repository: NotesRepository,
    private val timeProvider: () -> Long = { System.currentTimeMillis() }
) : NotesUseCase {

    override val notesFlow: Flow<List<Note>> =
        repository.observeNotes().map { notes ->
            // Здесь можно будет добавить дополнительные бизнес‑правила сортировки/фильтрации.
            notes
        }

    override suspend fun getNote(id: NoteId): Result<Note?> =
        repository.getNote(id).toResult()

    override suspend fun createNote(title: String, body: String): Result<Note> {
        val normalizedTitle = title.trim()
        val normalizedBody = body.trim()

        if (normalizedTitle.isEmpty() && normalizedBody.isEmpty()) {
            return Result.failure(IllegalArgumentException("Note must not be completely empty"))
        }

        return repository.createNote(
            title = normalizedTitle,
            body = normalizedBody
        ).toResult()
    }

    override suspend fun updateNoteContent(
        id: NoteId,
        title: String,
        body: String
    ): Result<Note> {
        val normalizedTitle = title.trim()
        val normalizedBody = body.trim()

        if (normalizedTitle.isEmpty() && normalizedBody.isEmpty()) {
            return Result.failure(IllegalArgumentException("Note must not be completely empty"))
        }

        val existing = repository.getNote(id).toResult().getOrElse { return Result.failure(it) }
            ?: return Result.failure(IllegalStateException("Note not found"))

        val updated = existing.copy(
            title = normalizedTitle,
            body = normalizedBody
        )
        return repository.updateNote(updated).toResult()
    }

    override suspend fun togglePinned(id: NoteId): Result<Unit> =
        repository.togglePinned(id).toResult()

    override suspend fun deleteNote(id: NoteId): Result<Unit> =
        repository.softDelete(id).toResult()
}

private fun <T> RepositoryResult<T>.toResult(): Result<T> = this

