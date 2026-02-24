package ru.topskiy.personalassistant.core.model

/**
 * Уникальный идентификатор заметки.
 *
 * Используется как обёртка над Long, чтобы не путать с другими идентификаторами
 * и облегчить миграции в будущем (например, на UUID).
 */
@JvmInline
value class NoteId(val value: Long)

/**
 * Доменная модель заметки.
 *
 * Поля подобраны с учётом возможной будущей синхронизации:
 * - [isDeleted] — мягкое удаление;
 * - [hasLocalChanges]/[lastLocalChangeAt] — отслеживание локальных изменений.
 */
data class Note(
    val id: NoteId,
    val title: String,
    val body: String,
    val createdAt: Long,
    val updatedAt: Long,
    val pinned: Boolean,
    val isDeleted: Boolean,
    val hasLocalChanges: Boolean,
    val lastLocalChangeAt: Long
)

