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
    val pinned: Boolean,
    val dayGroup: NoteDayGroup,
    val titleHighlightRanges: List<IntRange> = emptyList(),
    val bodyHighlightRanges: List<IntRange> = emptyList()
)

/**
 * Группа по дате для заметки: сегодня / вчера / ранее.
 */
enum class NoteDayGroup {
    TODAY,
    YESTERDAY,
    EARLIER
}

/**
 * Тип секции в списке заметок.
 *
 * PINNED — закреплённые заметки сверху отдельным блоком,
 * остальные секции — по дате.
 */
enum class NotesSectionType {
    PINNED,
    TODAY,
    YESTERDAY,
    EARLIER
}

/**
 * Секция списка заметок: заголовок (через [type]) и элементы.
 */
data class NotesSectionUi(
    val type: NotesSectionType,
    val items: List<NoteListItemUi>
)

/**
 * Состояние экрана списка заметок.
 */
data class NotesUiState(
    val notes: List<NoteListItemUi> = emptyList(),
    val sections: List<NotesSectionUi> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessageRes: Int? = null
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val notesUseCase: NotesUseCase
) : ViewModel() {

    private val _messageEvent = MutableSharedFlow<Int>()
    val messageEvent: SharedFlow<Int> = _messageEvent.asSharedFlow()

    /**
     * Последние удалённые заметки для Undo (поддерживает пакетное удаление).
     * Очищается после восстановления или следующего удаления/пакета.
     */
    private val _lastDeletedNotes = MutableStateFlow<List<Note>>(emptyList())

    /** Событие «показать снэкбар Undo» после удаления заметки. */
    private val _undoSnackbarRequest = MutableSharedFlow<Unit>()
    val undoSnackbarRequest: SharedFlow<Unit> = _undoSnackbarRequest.asSharedFlow()

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

            val searchOrNull = if (q.isEmpty()) null else q
            val listItems = filtered.map { note -> note.toListItemUi(searchOrNull) }
            val pinnedItems = listItems.filter { it.pinned }
            val otherItems = listItems.filterNot { it.pinned }

            val sections = buildList {
                if (pinnedItems.isNotEmpty()) {
                    add(NotesSectionUi(NotesSectionType.PINNED, pinnedItems))
                }

                fun itemsForGroup(group: NoteDayGroup): List<NoteListItemUi> =
                    otherItems.filter { it.dayGroup == group }

                val todayItems = itemsForGroup(NoteDayGroup.TODAY)
                if (todayItems.isNotEmpty()) {
                    add(NotesSectionUi(NotesSectionType.TODAY, todayItems))
                }

                val yesterdayItems = itemsForGroup(NoteDayGroup.YESTERDAY)
                if (yesterdayItems.isNotEmpty()) {
                    add(NotesSectionUi(NotesSectionType.YESTERDAY, yesterdayItems))
                }

                val earlierItems = itemsForGroup(NoteDayGroup.EARLIER)
                if (earlierItems.isNotEmpty()) {
                    add(NotesSectionUi(NotesSectionType.EARLIER, earlierItems))
                }
            }

            NotesUiState(
                notes = listItems,
                sections = sections,
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
        onDeleteNotes(setOf(id))
    }

    /**
     * Пакетное мягкое удаление заметок. Используется как для одиночного удаления, так и для
     * режима мультивыбора. Все успешно загруженные заметки сохраняются для последующего Undo.
     */
    fun onDeleteNotes(ids: Set<NoteId>) {
        if (ids.isEmpty()) return
        viewModelScope.launch {
            val notes = ids.mapNotNull { id ->
                notesUseCase.getNote(id).getOrNull()
            }
            if (notes.isEmpty()) {
                _messageEvent.emit(R.string.notes_save_error)
                return@launch
            }

            val failures = mutableListOf<Throwable>()
            ids.forEach { id ->
                notesUseCase.deleteNote(id)
                    .onFailure { failures += it }
            }

            if (failures.isNotEmpty()) {
                _messageEvent.emit(R.string.notes_save_error)
                return@launch
            }

            _lastDeletedNotes.value = notes
            _undoSnackbarRequest.emit(Unit)
        }
    }

    /** Восстанавливает последнюю партию удалённых заметок (Undo). */
    fun restoreLastDeleted() {
        viewModelScope.launch {
            val notes = _lastDeletedNotes.value
            if (notes.isEmpty()) return@launch

            var hasFailure = false
            notes.forEach { note ->
                notesUseCase.restoreNote(note)
                    .onFailure {
                        hasFailure = true
                    }
            }
            _lastDeletedNotes.value = emptyList()

            if (hasFailure) {
                _messageEvent.emit(R.string.notes_save_error)
            }
        }
    }

    suspend fun getNote(id: NoteId): Result<Note?> = notesUseCase.getNote(id)
}

private fun String.findHighlightRanges(lowerNeedle: String): List<IntRange> {
    if (lowerNeedle.isEmpty()) return emptyList()
    val lowerHaystack = lowercase(Locale.getDefault())
    val result = mutableListOf<IntRange>()
    var startIndex = 0
    while (true) {
        val index = lowerHaystack.indexOf(lowerNeedle, startIndex)
        if (index < 0) break
        result += index until (index + lowerNeedle.length)
        startIndex = index + lowerNeedle.length
    }
    return result
}

private fun Note.toListItemUi(searchLower: String?): NoteListItemUi {
    val preview = body
        .replace('\n', ' ')
        .trim()
        .take(BODY_PREVIEW_MAX_LENGTH)
        .let { if (body.length > BODY_PREVIEW_MAX_LENGTH) "$it…" else it }
    val group = classifyDayGroup(updatedAt)
    val safeTitle = title.ifEmpty { " " }
    val titleRanges = if (searchLower != null) {
        safeTitle.findHighlightRanges(searchLower)
    } else {
        emptyList()
    }
    val bodyRanges = if (searchLower != null) {
        preview.findHighlightRanges(searchLower)
    } else {
        emptyList()
    }
    return NoteListItemUi(
        id = id,
        title = safeTitle,
        bodyPreview = preview,
        updatedAtFormatted = formatUpdatedAt(updatedAt),
        pinned = pinned,
        dayGroup = group,
        titleHighlightRanges = titleRanges,
        bodyHighlightRanges = bodyRanges
    )
}

private fun classifyDayGroup(millis: Long): NoteDayGroup {
    val locale = Locale.getDefault()
    val nowCal = Calendar.getInstance()
    val noteCal = Calendar.getInstance().apply { timeInMillis = millis }

    fun Calendar.isSameDay(other: Calendar): Boolean =
        get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
            get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)

    if (noteCal.isSameDay(nowCal)) {
        return NoteDayGroup.TODAY
    }

    val yesterdayCal = (nowCal.clone() as Calendar).apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }
    if (noteCal.isSameDay(yesterdayCal)) {
        return NoteDayGroup.YESTERDAY
    }

    return NoteDayGroup.EARLIER
}

private fun formatUpdatedAt(millis: Long): String {
    val locale = Locale.getDefault()
    return when (classifyDayGroup(millis)) {
        NoteDayGroup.TODAY -> {
            val timeFormat = SimpleDateFormat("HH:mm", locale)
            timeFormat.format(Date(millis))
        }

        NoteDayGroup.YESTERDAY -> {
            val timeFormat = SimpleDateFormat("HH:mm", locale)
            "Вчера ${timeFormat.format(Date(millis))}"
        }

        NoteDayGroup.EARLIER -> {
            val dateFormat = SimpleDateFormat("d MMM", locale)
            dateFormat.format(Date(millis))
        }
    }
}
