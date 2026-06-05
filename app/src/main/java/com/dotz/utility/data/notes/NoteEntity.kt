package com.dotz.utility.data.notes

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ── Entity ─────────────────────────────────────────────────────────────────

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

// ── DAO ────────────────────────────────────────────────────────────────────

@Dao
interface NoteDao {
    /** Observe all notes: pinned first, then sorted by last update */
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    /** Search notes by title or content */
    @Query("""
        SELECT * FROM notes
        WHERE title LIKE '%' || :q || '%' OR content LIKE '%' || :q || '%'
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun search(q: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: NoteEntity): Long

    @Delete
    suspend fun delete(note: NoteEntity)

    @Query("UPDATE notes SET isPinned = :pinned, updatedAt = :ts WHERE id = :id")
    suspend fun setPin(id: Long, pinned: Boolean, ts: Long = System.currentTimeMillis())
}
