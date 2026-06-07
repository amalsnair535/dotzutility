package com.dotz.utility.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dotz.utility.data.calendar.CalendarEvent
import com.dotz.utility.viewmodel.CalendarViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JTextStyle
import java.util.Locale

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    vm: CalendarViewModel = viewModel()
) {
    val month by vm.displayMonth.collectAsState()
    val selectedDay by vm.selectedDay.collectAsState()
    val eventDays by vm.eventDays.collectAsState()
    val events by vm.selectedDayEvents.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    val dateFullFormatter = remember { 
        java.text.SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    }
    
    // Swipe state
    var swipeOffsetX by remember { mutableStateOf(0f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Circle,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Calendar", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    if (selectedDay != null) {
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Outlined.Add, "Add event")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (swipeOffsetX > 150) {
                                vm.previousMonth()
                            } else if (swipeOffsetX < -150) {
                                vm.nextMonth()
                            }
                            swipeOffsetX = 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            swipeOffsetX += dragAmount
                        }
                    )
                }
        ) {
            // Month navigator
            MonthHeader(
                month = month,
                onPrev = vm::previousMonth,
                onNext = vm::nextMonth
            )

            // Day-of-week labels
            DayLabels()

            // Calendar grid
            CalendarGrid(
                month = month,
                today = LocalDate.now(),
                selectedDay = selectedDay,
                eventDays = eventDays,
                onDayClick = vm::selectDay
            )

            HorizontalDivider(
                Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Events list for selected day
            if (selectedDay != null) {
                Text(
                    text = try {
                        val date = java.util.Date.from(selectedDay!!.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())
                        dateFullFormatter.format(date)
                    } catch (e: Exception) {
                        selectedDay!!.toString()
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                if (events.isEmpty()) {
                    Text(
                        "No events. Tap + to add one.",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(events, key = { it.id }) { event ->
                            EventRow(event = event, onDelete = { vm.deleteEvent(event) })
                        }
                    }
                }
            } else {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Tap a day to see events",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                    )
                }
            }
        }
    }

    if (showAddDialog && selectedDay != null) {
        AddEventDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, note ->
                vm.addEvent(title, note)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun MonthHeader(month: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, "Previous month")
        }
        Text(
            text = "${month.month.getDisplayName(JTextStyle.FULL, Locale.getDefault())} ${month.year}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, "Next month")
        }
    }
}

@Composable
private fun DayLabels() {
    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        listOf("Su","Mo","Tu","We","Th","Fr","Sa").forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    today: LocalDate,
    selectedDay: LocalDate?,
    eventDays: Set<Long>,
    onDayClick: (LocalDate) -> Unit,
) {
    val firstDayOfMonth = month.atDay(1)
    val startOffset = firstDayOfMonth.dayOfWeek.let { (it.value % 7) }
    val daysInMonth = month.lengthOfMonth()
    
    // Calculate total slots needed (rows of 7)
    val totalSlots = startOffset + daysInMonth
    val rows = (totalSlots + 6) / 7

    Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        for (row in 0 until rows) {
            Row(Modifier.fillMaxWidth()) {
                for (col in 0..6) {
                    val index = row * 7 + col
                    val dayNum = index - startOffset + 1
                    
                    Box(
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dayNum in 1..daysInMonth) {
                            val date = month.atDay(dayNum)
                            val isToday = date == today
                            val isSelected = date == selectedDay
                            val hasEvent = eventDays.contains(date.toEpochDay())

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.onSurface
                                            isToday    -> MaterialTheme.colorScheme.surfaceVariant
                                            else       -> MaterialTheme.colorScheme.background
                                        }
                                    )
                                    .then(
                                        if (isToday && !isSelected)
                                            Modifier.border(1.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                        else Modifier
                                    )
                                    .clickable { onDayClick(date) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dayNum.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.surface
                                            else       -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    if (hasEvent) {
                                        Box(
                                            Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected)
                                                        MaterialTheme.colorScheme.surface
                                                    else
                                                        MaterialTheme.colorScheme.onSurface
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventRow(event: CalendarEvent, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(event.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            if (event.note.isNotBlank())
                Text(event.note, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Outlined.DeleteOutline, "Delete", modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
        }
    }
}

@Composable
private fun AddEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var note  by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Event") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (title.isNotBlank()) onConfirm(title, note) },
                enabled = title.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
