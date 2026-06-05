package com.dotz.utility.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dotz.utility.data.calendar.CalendarEvent
import com.dotz.utility.data.calendar.CalendarEventDao
import com.dotz.utility.data.notes.NoteDao
import com.dotz.utility.data.notes.NoteEntity

/**
 * Single Room database for the whole app.
 * Using a companion-object singleton avoids multiple connections.
 */
@Database(
    entities = [NoteEntity::class, CalendarEvent::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun calendarEventDao(): CalendarEventDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dotz_utility.db"
                ).build().also { INSTANCE = it }
            }
    }
}
