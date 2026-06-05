package com.dotz.utility.data.calendar

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ── Entity ─────────────────────────────────────────────────────────────────

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Day stored as epoch days for easy querying (System.currentTimeMillis() / 86_400_000) */
    val epochDay: Long,
    val title: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

// ── DAO ────────────────────────────────────────────────────────────────────

@Dao
interface CalendarEventDao {
    /** All events for a specific day */
    @Query("SELECT * FROM calendar_events WHERE epochDay = :epochDay ORDER BY createdAt ASC")
    fun observeByDay(epochDay: Long): Flow<List<CalendarEvent>>

    /** Days that have at least one event in a given month range */
    @Query("SELECT DISTINCT epochDay FROM calendar_events WHERE epochDay BETWEEN :start AND :end")
    fun observeEventDaysInRange(start: Long, end: Long): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: CalendarEvent): Long

    @Delete
    suspend fun delete(event: CalendarEvent)
}
