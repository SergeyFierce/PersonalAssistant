package ru.topskiy.personalassistant.core.data.notes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room‑сущность для заметки.
 *
 * Таблица [notes] хранит как актуальные, так и мягко удалённые заметки. Для отображения
 * в списке используются только записи с [isDeleted] = 0.
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val title: String,

    val body: String,

    /** Время первого создания заметки, миллисекунды с эпохи. */
    val createdAt: Long,

    /** Время последнего изменения заметки, миллисекунды с эпохи. */
    val updatedAt: Long,

    /** Закреплена ли заметка (аналог pinned‑чатов в Telegram). */
    val pinned: Boolean,

    /** Мягкое удаление: true — заметка скрыта из списка, но может участвовать в будущей синхронизации. */
    val isDeleted: Boolean,

    /** Есть ли несинхронизированные локальные изменения. */
    val hasLocalChanges: Boolean,

    /** Время последнего локального изменения (для разрешения конфликтов в будущем). */
    @ColumnInfo(index = true)
    val lastLocalChangeAt: Long
)

