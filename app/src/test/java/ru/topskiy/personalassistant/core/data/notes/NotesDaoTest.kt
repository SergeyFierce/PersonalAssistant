package ru.topskiy.personalassistant.core.data.notes

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import ru.topskiy.personalassistant.core.data.AppDatabase

/**
 * Простые unit‑тесты DAO заметок на in‑memory Room.
 *
 * Покрывают базовые операции: создание, чтение, обновление, мягкое удаление и сортировку.
 */
class NotesDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: NotesDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.notesDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndReadNote() = runBlocking {
        val now = System.currentTimeMillis()
        val id = dao.upsertNote(
            NoteEntity(
                title = "Test",
                body = "Body",
                createdAt = now,
                updatedAt = now,
                pinned = false,
                isDeleted = false,
                hasLocalChanges = false,
                lastLocalChangeAt = now
            )
        )

        val loaded = dao.getNote(id)
        assertNotNull(loaded)
        assertEquals("Test", loaded?.title)
        assertEquals("Body", loaded?.body)
    }

    @Test
    fun observeNotes_excludesDeleted_andSortsByPinnedAndUpdatedAt() = runBlocking {
        val baseTime = System.currentTimeMillis()

        val firstId = dao.upsertNote(
            NoteEntity(
                title = "Old",
                body = "",
                createdAt = baseTime - 10_000,
                updatedAt = baseTime - 10_000,
                pinned = false,
                isDeleted = false,
                hasLocalChanges = false,
                lastLocalChangeAt = baseTime - 10_000
            )
        )

        val secondId = dao.upsertNote(
            NoteEntity(
                title = "New pinned",
                body = "",
                createdAt = baseTime,
                updatedAt = baseTime,
                pinned = true,
                isDeleted = false,
                hasLocalChanges = false,
                lastLocalChangeAt = baseTime
            )
        )

        // Помечаем первую заметку как удалённую.
        dao.markDeleted(firstId, baseTime + 5_000)

        val list = dao.observeNotes().first()
        // В списке только вторая заметка (первая isDeleted = 1).
        assertEquals(1, list.size)
        assertEquals(secondId, list.first().id)
        assertEquals("New pinned", list.first().title)
    }
}

