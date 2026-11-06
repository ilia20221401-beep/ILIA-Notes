package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Note
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ilia_notes_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialNotes(database.noteDao())
                    }
                }
            }

            suspend fun populateInitialNotes(dao: NoteDao) {
                dao.insertNote(
                    Note(
                        title = "به ILIA Notes خوش آمدید!",
                        content = "این برنامه یادداشت‌های شما را با استفاده از پایگاه داده قدرتمند Room به صورت کاملاً امن و محلی روی دستگاه شما ذخیره می‌کند.\n\nویژگی‌ها:\n• افزودن یادداشت با رنگ و دسته‌بندی دلخواه\n• امکان ویرایش و حذف سریع\n• سنجاق کردن یادداشت‌های مهم در بالای لیست\n• جستجوی فوری در میان عنوان و محتوا",
                        timestamp = System.currentTimeMillis(),
                        colorHex = 0xFFFFFBEB, // Warm amber
                        category = "ایده‌ها",
                        isPinned = true
                    )
                )
                dao.insertNote(
                    Note(
                        title = "لیست کارهای روزمره",
                        content = "۱. مرور پروژه‌های در دست انجام\n۲. هماهنگی جلسه کاری با تیم\n۳. مطالعه ۳۰ دقیقه کتاب\n۴. پیاده‌روی عصرگاهی",
                        timestamp = System.currentTimeMillis() - 3600000,
                        colorHex = 0xFFECFDF5, // Mint green
                        category = "شخصی",
                        isPinned = false
                    )
                )
            }
        }
    }
}
