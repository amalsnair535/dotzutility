package com.dotz.utility.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dotz.utility.data.calendar.CalendarEvent
import com.dotz.utility.data.calendar.CalendarEventDao
import com.dotz.utility.data.clock.AlarmDao
import com.dotz.utility.data.clock.AlarmEntity
import com.dotz.utility.data.notes.NoteDao
import com.dotz.utility.data.notes.NoteEntity

/**
 * Single Room database for the whole app.
 */
@Database(
    entities = [NoteEntity::class, CalendarEvent::class, AlarmEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun alarmDao(): AlarmDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dotz_utility.db"
                )
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
            }
    }
}
