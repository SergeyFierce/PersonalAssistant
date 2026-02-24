package ru.topskiy.personalassistant.core.ui

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import ru.topskiy.personalassistant.R
import ru.topskiy.personalassistant.core.domain.notes.NotesUseCase
import ru.topskiy.personalassistant.core.model.Note
import ru.topskiy.personalassistant.core.model.NoteId

private fun note(
    id: Long,
    title: String = "Title",
    body: String = "Body",
    updatedAt: Long = 1000L,
    pinned: Boolean = false,
    isDeleted: Boolean = false
) = Note(
    id = NoteId(id),
    title = title,
    body = body,
    createdAt = updatedAt,
    updatedAt = updatedAt,
    pinned = pinned,
    isDeleted = isDeleted,
    hasLocalChanges = false,
    lastLocalChangeAt = updatedAt
)

/**
 * Fake [NotesUseCase] for unit tests: keeps an in-memory list and exposes it via [notesFlow].
 * Supports create/update/togglePinned/softDelete; [notesFlow] excludes deleted notes.
 */
private class FakeNotesUseCase(initialNotes: List<Note> = emptyList()) : NotesUseCase {

    private val _notes = MutableStateFlow(initialNotes.toMutableList())

    override val notesFlow: Flow<List<Note>> = _notes.map { list ->
        list.filter { !it.isDeleted }
    }

    override suspend fun getNote(id: NoteId): Result<Note?> =
        Result.success(_notes.value.find { it.id == id })

    override suspend fun createNote(title: String, body: String): Result<Note> {
        val maxId = _notes.value.maxOfOrNull { it.id.value } ?: 0L
        val newNote = note(
            id = maxId + 1,
            title = title.trim(),
            body = body.trim(),
            updatedAt = System.currentTimeMillis(),
            pinned = false
        )
        _notes.value = _notes.value + newNote
        return Result.success(newNote)
    }

    override suspend fun updateNoteContent(id: NoteId, title: String, body: String): Result<Note> {
        val idx = _notes.value.indexOfFirst { it.id == id }
        if (idx < 0) return Result.failure(IllegalStateException("Note not found"))
        val updated = _notes.value[idx].copy(
            title = title.trim(),
            body = body.trim(),
            updatedAt = System.currentTimeMillis()
        )
        _notes.value = _notes.value.toMutableList().apply { set(idx, updated) }
        return Result.success(updated)
    }

    override suspend fun togglePinned(id: NoteId): Result<Unit> {
        val idx = _notes.value.indexOfFirst { it.id == id }
        if (idx < 0) return Result.failure(IllegalStateException("Note not found"))
        val n = _notes.value[idx]
        _notes.value = _notes.value.toMutableList().apply {
            set(idx, n.copy(pinned = !n.pinned, updatedAt = System.currentTimeMillis()))
        }
        return Result.success(Unit)
    }

    override suspend fun deleteNote(id: NoteId): Result<Unit> {
        val idx = _notes.value.indexOfFirst { it.id == id }
        if (idx < 0) return Result.failure(IllegalStateException("Note not found"))
        val n = _notes.value[idx]
        _notes.value = _notes.value.toMutableList().apply {
            set(idx, n.copy(isDeleted = true, updatedAt = System.currentTimeMillis()))
        }
        return Result.success(Unit)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    @Before
    fun setup() {
        setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        resetMain()
    }

    @Test
    fun `uiState shows notes in order - pinned first then by date`() = runTest {
        val pinnedOlder = note(1, "Pinned", "Pinned body", updatedAt = 1000, pinned = true)
        val unpinnedNewer = note(2, "Unpinned", "Unpinned body", updatedAt = 2000, pinned = false)
        val useCase = FakeNotesUseCase(initialNotes = listOf(pinnedOlder, unpinnedNewer))
        val vm = NotesViewModel(useCase)

        advanceUntilIdle()

        val notes = vm.uiState.value.notes
        assertEquals(2, notes.size)
        assertTrue(notes[0].pinned)
        assertEquals("Pinned", notes[0].title)
        assertTrue(!notes[1].pinned)
        assertEquals("Unpinned", notes[1].title)
    }

    @Test
    fun `onCreateNote adds note and uiState updates`() = runTest {
        val useCase = FakeNotesUseCase()
        val vm = NotesViewModel(useCase)

        advanceUntilIdle()
        assertTrue(vm.uiState.value.notes.isEmpty())

        vm.onCreateNote("New title", "New body")
        advanceUntilIdle()

        val notes = vm.uiState.value.notes
        assertEquals(1, notes.size)
        assertEquals("New title", notes[0].title)
        assertTrue(notes[0].bodyPreview.startsWith("New body") || notes[0].bodyPreview == "New body")
    }

    @Test
    fun `onUpdateNote updates content and uiState reflects it`() = runTest {
        val n = note(1, "Old", "Old body", updatedAt = 1000)
        val useCase = FakeNotesUseCase(initialNotes = listOf(n))
        val vm = NotesViewModel(useCase)

        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.notes.size)
        assertEquals("Old", vm.uiState.value.notes[0].title)

        vm.onUpdateNote(NoteId(1), "New title", "New body")
        advanceUntilIdle()

        val notes = vm.uiState.value.notes
        assertEquals(1, notes.size)
        assertEquals("New title", notes[0].title)
        assertTrue(notes[0].bodyPreview.contains("New body") || notes[0].bodyPreview == "New body")
    }

    @Test
    fun `onTogglePinned flips pinned and uiState updates`() = runTest {
        val n = note(1, "Note", "Body", pinned = false)
        val useCase = FakeNotesUseCase(initialNotes = listOf(n))
        val vm = NotesViewModel(useCase)

        advanceUntilIdle()
        assertTrue(!vm.uiState.value.notes[0].pinned)

        vm.onTogglePinned(NoteId(1))
        advanceUntilIdle()

        assertTrue(vm.uiState.value.notes[0].pinned)
    }

    @Test
    fun `onDeleteNote removes note from uiState`() = runTest {
        val n = note(1, "To delete", "Body")
        val useCase = FakeNotesUseCase(initialNotes = listOf(n))
        val vm = NotesViewModel(useCase)

        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.notes.size)

        vm.onDeleteNote(NoteId(1))
        advanceUntilIdle()

        assertTrue(vm.uiState.value.notes.isEmpty())
    }

    @Test
    fun `createNote failure emits notes_save_error on messageEvent`() = runTest {
        val failingUseCase = object : NotesUseCase {
            override val notesFlow: Flow<List<Note>> = flow { emit(emptyList()) }
            override suspend fun getNote(id: NoteId): Result<Note?> = Result.success(null)
            override suspend fun createNote(title: String, body: String): Result<Note> =
                Result.failure(IllegalStateException("fail"))
            override suspend fun updateNoteContent(id: NoteId, title: String, body: String): Result<Note> =
                Result.failure(IllegalStateException("fail"))
            override suspend fun togglePinned(id: NoteId): Result<Unit> =
                Result.failure(IllegalStateException("fail"))
            override suspend fun deleteNote(id: NoteId): Result<Unit> =
                Result.failure(IllegalStateException("fail"))
        }
        val vm = NotesViewModel(failingUseCase)

        var receivedMessageId: Int? = null
        val job = launch {
            receivedMessageId = vm.messageEvent.first()
        }

        vm.onCreateNote("A", "B")
        advanceUntilIdle()

        job.join()
        assertEquals(R.string.notes_save_error, receivedMessageId)
    }
}
