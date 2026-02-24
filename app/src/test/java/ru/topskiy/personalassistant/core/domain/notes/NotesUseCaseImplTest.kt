package ru.topskiy.personalassistant.core.domain.notes

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.topskiy.personalassistant.core.data.RepositoryResult
import ru.topskiy.personalassistant.core.data.notes.NotesRepository
import ru.topskiy.personalassistant.core.model.Note
import ru.topskiy.personalassistant.core.model.NoteId

private class FakeNotesRepository : NotesRepository {

    val created = mutableListOf<Note>()
    var existing: Note? = null
    var toggledId: NoteId? = null
    var deletedId: NoteId? = null

    override fun observeNotes() = throw UnsupportedOperationException("Not needed in this test")

    override suspend fun getNote(id: NoteId): RepositoryResult<Note?> =
        RepositoryResult.success(existing?.takeIf { it.id == id })

    override suspend fun createNote(title: String, body: String): RepositoryResult<Note> {
        val note = Note(
            id = NoteId(1L),
            title = title,
            body = body,
            createdAt = 0L,
            updatedAt = 0L,
            pinned = false,
            isDeleted = false,
            hasLocalChanges = true,
            lastLocalChangeAt = 0L
        )
        created += note
        existing = note
        return RepositoryResult.success(note)
    }

    override suspend fun updateNote(note: Note): RepositoryResult<Note> {
        existing = note
        return RepositoryResult.success(note)
    }

    override suspend fun togglePinned(id: NoteId): RepositoryResult<Unit> {
        toggledId = id
        return RepositoryResult.success(Unit)
    }

    override suspend fun softDelete(id: NoteId): RepositoryResult<Unit> {
        deletedId = id
        return RepositoryResult.success(Unit)
    }
}

class NotesUseCaseImplTest {

    @Test
    fun createNote_trimsInput_andFailsOnEmpty() = runBlocking {
        val repo = FakeNotesRepository()
        val useCase = NotesUseCaseImpl(repo)

        // Пустая заметка — ошибка.
        val emptyResult = useCase.createNote("   ", "   ")
        assertTrue(emptyResult.isFailure)

        // Валидная заметка — создаётся с обрезанными пробелами.
        val result = useCase.createNote("  Title  ", "  Body  ")
        assertTrue(result.isSuccess)
        val note = result.getOrThrow()
        assertEquals("Title", note.title)
        assertEquals("Body", note.body)
    }

    @Test
    fun updateNoteContent_usesExistingNote_andTrims() = runBlocking {
        val repo = FakeNotesRepository()
        val initial = Note(
            id = NoteId(10L),
            title = "Old",
            body = "Body",
            createdAt = 0L,
            updatedAt = 0L,
            pinned = false,
            isDeleted = false,
            hasLocalChanges = false,
            lastLocalChangeAt = 0L
        )
        repo.existing = initial

        val useCase = NotesUseCaseImpl(repo)
        val result = useCase.updateNoteContent(NoteId(10L), "  New  ", "  Text  ")

        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        assertEquals("New", updated.title)
        assertEquals("Text", updated.body)
    }

    @Test
    fun togglePinned_andDelete_delegateToRepository() = runBlocking {
        val repo = FakeNotesRepository()
        val useCase = NotesUseCaseImpl(repo)
        val id = NoteId(5L)

        val toggleResult = useCase.togglePinned(id)
        val deleteResult = useCase.deleteNote(id)

        assertTrue(toggleResult.isSuccess)
        assertTrue(deleteResult.isSuccess)
        assertEquals(id, repo.toggledId)
        assertEquals(id, repo.deletedId)
    }
}

