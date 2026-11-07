package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.Note
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ILIA Notes", appName)
        val credit = context.getString(R.string.designer_credit)
        assertEquals("Designed and developed by ILIA SOFTWARE", credit)
    }

    @Test
    fun `insert and retrieve note from room database`() = runBlocking {
        val dao = db.noteDao()
        val note = Note(
            title = "Test Note",
            content = "This is a test note for Room database persistence.",
            category = "Work",
            isPinned = true
        )
        val id = dao.insertNote(note)
        assertTrue(id > 0)

        val notes = dao.getAllNotes().first()
        assertEquals(1, notes.size)
        assertEquals("Test Note", notes[0].title)
        assertTrue(notes[0].isPinned)

        dao.deleteNoteById(id)
        val remainingNotes = dao.getAllNotes().first()
        assertTrue(remainingNotes.isEmpty())
    }
}
