package com.dotz.utility.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dotz.utility.data.AppDatabase
import com.dotz.utility.data.settings.SettingsRepository
import com.dotz.utility.data.settings.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = SettingsRepository(app)
    private val noteDao = AppDatabase.get(app).noteDao()

    val themeMode = repo.themeMode.stateIn(
        viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM
    )

    fun setTheme(mode: ThemeMode) = viewModelScope.launch {
        repo.setThemeMode(mode)
    }

    /** Returns a plain-text dump of all notes for export */
    suspend fun exportNotesText(): String {
        return noteDao.observeAll().first().joinToString("\n\n---\n\n") { n ->
            "# ${n.title}\n\n${n.content}"
        }
    }
}
