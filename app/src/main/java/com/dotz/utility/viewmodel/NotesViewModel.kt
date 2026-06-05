package com.dotz.utility.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dotz.utility.data.AppDatabase
import com.dotz.utility.data.notes.NoteEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotesViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.get(app).noteDao()

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    /** Notes list reacts to search query changes */
    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<NoteEntity>> = _query
        .debounce(200)
        .flatMapLatest { q ->
            if (q.isBlank()) dao.observeAll() else dao.search(q)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(q: String) { _query.value = q }

    fun togglePin(note: NoteEntity) = viewModelScope.launch {
        dao.setPin(note.id, !note.isPinned)
    }

    fun delete(note: NoteEntity) = viewModelScope.launch {
        dao.delete(note)
    }

    suspend fun getNote(id: Long): NoteEntity? = dao.getById(id)

    fun save(id: Long, title: String, content: String) = viewModelScope.launch {
        val ts = System.currentTimeMillis()
        val existing = if (id > 0) dao.getById(id) else null
        dao.upsert(
            NoteEntity(
                id = existing?.id ?: 0,
                title = title.ifBlank { "Untitled" },
                content = content,
                isPinned = existing?.isPinned ?: false,
                createdAt = existing?.createdAt ?: ts,
                updatedAt = ts
            )
        )
    }

    /** Export all notes as a plain-text string */
    suspend fun exportText(): String {
        return dao.observeAll().first().joinToString("\n\n---\n\n") { n ->
            "${n.title}\n\n${n.content}"
        }
    }
}
