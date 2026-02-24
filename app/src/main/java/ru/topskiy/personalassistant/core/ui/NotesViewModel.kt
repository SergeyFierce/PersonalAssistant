package ru.topskiy.personalassistant.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.topskiy.personalassistant.R
import ru.topskiy.personalassistant.core.domain.notes.NotesUseCase
import ru.topskiy.personalassistant.core.model.Note
import ru.topskiy.personalassistant.core.model.NoteId
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private const val BODY_PREVIEW_MAX_LENGTH = 80

/**
 * Элемент списка заметок для UI (в стиле строки чата в Telegram).
 */
data class NoteListItemUi(
    val id: NoteId,
    val title: String,
    val bodyPreview: String,
    val updatedAtFormatted: String,
    val pinned: Boolean
)

/**
 * Состояние экрана списка заметок.
 */
data class NotesUiState(
    val notes: List<NoteListItemUi> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessageRes: Int? = null
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val notesUseCase: NotesUseCase
) : ViewModel() {

    private val _messageEvent = MutableSharedFlow<Int>()
    val messageEvent: SharedFlow<Int> = _messageEvent.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val uiState: StateFlow<NotesUiState> =
        combine(
            notesUseCase.notesFlow,
            _searchQuery
        ) { notes, query ->
            val q = query.trim().lowercase()
            val filtered = if (q.isEmpty()) {
                notes
            } else {
                notes.filter { note ->
                    note.title.contains(q, ignoreCase = true) ||
                        note.body.contains(q, ignoreCase = true)
                }
            }
            NotesUiState(
                notes = filtered.map { note -> note.toListItemUi() },
                isLoading = false,
                errorMessageRes = null
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NotesUiState(isLoading = true)
        )

    fun onSearchQueryChange(value: String) {
        _searchQuery.value = value
    }

    fun onCreateNote(title: String, body: String) {
        viewModelScope.launch {
            notesUseCase.createNote(title, body)
                .onFailure { _messageEvent.emit(R.string.notes_save_error) }
        }
    }

    fun onUpdateNote(id: NoteId, title: String, body: String) {
        viewModelScope.launch {
            notesUseCase.updateNoteContent(id, title, body)
                .onFailure { _messageEvent.emit(R.string.notes_save_error) }
        }
    }

    fun onTogglePinned(id: NoteId) {
        viewModelScope.launch {
            notesUseCase.togglePinned(id)
                .onFailure { _messageEvent.emit(R.string.notes_save_error) }
        }
    }

    fun onDeleteNote(id: NoteId) {
        viewModelScope.launch {
            notesUseCase.deleteNote(id)
                .onFailure { _messageEvent.emit(R.string.notes_save_error) }
        }
    }

    suspend fun getNote(id: NoteId): Result<Note?> = notesUseCase.getNote(id)
}

private fun Note.toListItemUi(): NoteListItemUi {
    val preview = body
        .replace('\n', ' ')
        .trim()
        .take(BODY_PREVIEW_MAX_LENGTH)
        .let { if (body.length > BODY_PREVIEW_MAX_LENGTH) "$it…" else it }
    return NoteListItemUi(
        id = id,
        title = title.ifEmpty { " " },
        bodyPreview = preview,
        updatedAtFormatted = formatUpdatedAt(updatedAt),
        pinned = pinned
    )
}

private fun formatUpdatedAt(millis: Long): String {
    val locale = Locale.getDefault()
    val nowCal = Calendar.getInstance()
    val noteCal = Calendar.getInstance().apply { timeInMillis = millis }

    fun Calendar.isSameDay(other: Calendar): Boolean =
        get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
            get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)

    if (noteCal.isSameDay(nowCal)) {
        // Сегодня — показываем только время, как в списке чатов.
        val timeFormat = SimpleDateFormat("HH:mm", locale)
        return timeFormat.format(Date(millis))
    }

    val yesterdayCal = (nowCal.clone() as Calendar).apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }
    if (noteCal.isSameDay(yesterdayCal)) {
        val timeFormat = SimpleDateFormat("HH:mm", locale)
        return "Вчера ${timeFormat.format(Date(millis))}"
    }

    // Старые даты: день и месяц без года (в духе Telegram‑списка).
    val dateFormat = SimpleDateFormat("d MMM", locale)
    return dateFormat.format(Date(millis))
}
