package com.dotz.utility.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dotz.utility.data.AppDatabase
import com.dotz.utility.data.calendar.CalendarEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.get(app).calendarEventDao()

    private val _displayMonth = MutableStateFlow(YearMonth.now())
    val displayMonth = _displayMonth.asStateFlow()

    private val _selectedDay = MutableStateFlow<LocalDate?>(null)
    val selectedDay = _selectedDay.asStateFlow()

    /** Days in the visible month that have events */
    @OptIn(ExperimentalCoroutinesApi::class)
    val eventDays: StateFlow<Set<Long>> = _displayMonth.flatMapLatest { month ->
        val start = month.atDay(1).toEpochDay()
        val end   = month.atEndOfMonth().toEpochDay()
        dao.observeEventDaysInRange(start, end).map { it.toSet() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    /** Events for the currently selected day */
    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedDayEvents: StateFlow<List<CalendarEvent>> = _selectedDay.flatMapLatest { day ->
        if (day == null) flowOf(emptyList())
        else dao.observeByDay(day.toEpochDay())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun previousMonth() { _displayMonth.update { it.minusMonths(1) } }
    fun nextMonth()     { _displayMonth.update { it.plusMonths(1) } }

    fun selectDay(day: LocalDate) {
        _selectedDay.value = if (_selectedDay.value == day) null else day
    }

    fun addEvent(title: String, note: String = "") {
        val day = _selectedDay.value ?: return
        viewModelScope.launch {
            dao.insert(CalendarEvent(epochDay = day.toEpochDay(), title = title, note = note))
        }
    }

    fun deleteEvent(event: CalendarEvent) = viewModelScope.launch {
        dao.delete(event)
    }
}
