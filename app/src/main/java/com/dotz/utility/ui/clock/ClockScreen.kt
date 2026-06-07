package com.dotz.utility.ui.clock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dotz.utility.data.clock.AlarmEntity
import com.dotz.utility.viewmodel.ClockViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockScreen(
    vm: ClockViewModel = viewModel()
) {
    val tabs = listOf("Alarm", "Clock", "Stopwatch", "Timer")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

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
                        Text("Clock", fontWeight = FontWeight.Bold)
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
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground
            ) {
                tabs.forEachIndexed { i, title ->
                    Tab(
                        selected = pagerState.currentPage == i,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(i)
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1
            ) { page ->
                when (page) {
                    0 -> AlarmTab(vm)
                    1 -> DigitalClockTab()
                    2 -> StopwatchTab(vm)
                    3 -> TimerTab(vm)
                }
            }
        }
    }
}

// ── Alarms ──────────────────────────────────────────────────────────────

@Composable
private fun AlarmTab(vm: ClockViewModel) {
    val alarms by vm.alarms.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        if (alarms.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No alarms set", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(alarms, key = { it.id }) { alarm ->
                    AlarmItem(
                        alarm = alarm,
                        onToggle = { vm.toggleAlarm(alarm) },
                        onDelete = { vm.deleteAlarm(alarm) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = MaterialTheme.colorScheme.onSurface,
            contentColor = MaterialTheme.colorScheme.surface,
            shape = CircleShape
        ) {
            Icon(Icons.Outlined.Add, "Add Alarm")
        }
    }

    if (showAddDialog) {
        AddAlarmDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { h, m, label ->
                vm.addAlarm(h, m, label)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AlarmItem(
    alarm: AlarmEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                val isPm = alarm.hour >= 12
                val displayHour = when {
                    alarm.hour == 0 -> 12
                    alarm.hour > 12 -> alarm.hour - 12
                    else -> alarm.hour
                }
                val amPm = if (isPm) "PM" else "AM"
                
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "%02d:%02d".format(displayHour, alarm.minute),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Light,
                        color = if (alarm.isEnabled) 
                            MaterialTheme.colorScheme.onSurface 
                        else 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = amPm,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (alarm.isEnabled) 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) 
                        else 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                if (alarm.label.isNotBlank()) {
                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Outlined.DeleteOutline, 
                    "Delete",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
            
            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.surface,
                    checkedTrackColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
private fun AddAlarmDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, String) -> Unit
) {
    var h by remember { mutableStateOf("07") }
    var m by remember { mutableStateOf("00") }
    var isPm by remember { mutableStateOf(false) }
    var label by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Alarm") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = h,
                        onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) h = it },
                        modifier = Modifier.width(64.dp),
                        label = { Text("HH") },
                        singleLine = true
                    )
                    Text(" : ", style = MaterialTheme.typography.headlineMedium)
                    OutlinedTextField(
                        value = m,
                        onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) m = it },
                        modifier = Modifier.width(64.dp),
                        label = { Text("MM") },
                        singleLine = true
                    )
                    Spacer(Modifier.width(12.dp))
                    
                    Column {
                        TextButton(
                            onClick = { isPm = false },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (!isPm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        ) { Text("AM", fontWeight = if (!isPm) FontWeight.Bold else FontWeight.Normal) }
                        TextButton(
                            onClick = { isPm = true },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (isPm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        ) { Text("PM", fontWeight = if (isPm) FontWeight.Bold else FontWeight.Normal) }
                    }
                }
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                var hour = h.toIntOrNull() ?: 0
                val minute = m.toIntOrNull() ?: 0
                
                // Convert to 24h format for saving
                if (isPm && hour < 12) hour += 12
                if (!isPm && hour == 12) hour = 0
                
                onConfirm(hour % 24, minute % 60, label)
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ── Digital Clock ──────────────────────────────────────────────────────────

@Composable
private fun DigitalClockTab() {
    var time by remember { mutableStateOf(LocalTime.now()) }
    val fmt = remember { DateTimeFormatter.ofPattern("HH:mm:ss") }
    val dateFmt = remember { DateTimeFormatter.ofPattern("EEEE, MMMM d") }

    LaunchedEffect(Unit) {
        while (true) { delay(500); time = LocalTime.now() }
    }

    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = time.format(fmt),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Thin,
                    fontSize = 64.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = java.time.LocalDate.now().format(dateFmt),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
            )
        }
    }
}

// ── Stopwatch ──────────────────────────────────────────────────────────────

@Composable
private fun StopwatchTab(vm: ClockViewModel) {
    val state by vm.stopwatch.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        Text(
            text = formatMs(state.elapsedMs),
            style = MaterialTheme.typography.displayMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Light,
                fontSize = 56.sp
            )
        )

        Spacer(Modifier.height(40.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (state.isRunning) {
                OutlinedButton(onClick = vm::lapStopwatch, shape = RoundedCornerShape(12.dp)) {
                    Text("Lap")
                }
                Button(onClick = vm::pauseStopwatch, shape = RoundedCornerShape(12.dp)) {
                    Text("Pause")
                }
            } else {
                OutlinedButton(onClick = vm::resetStopwatch, shape = RoundedCornerShape(12.dp)) {
                    Text("Reset")
                }
                Button(onClick = vm::startStopwatch, shape = RoundedCornerShape(12.dp)) {
                    Text("Start")
                }
            }
        }

        if (state.laps.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            LazyColumn {
                itemsIndexed(state.laps.reversed()) { idx, lap ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Lap ${state.laps.size - idx}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            formatMs(lap),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

// ── Timer ──────────────────────────────────────────────────────────────────

@Composable
private fun TimerTab(vm: ClockViewModel) {
    val state by vm.timer.collectAsState()
    var h by remember { mutableStateOf("0") }
    var m by remember { mutableStateOf("5") }
    var s by remember { mutableStateOf("0") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        if (!state.isRunning && state.remainingMs == 0L && !state.isFinished) {
            // Input row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimerInput(value = h, label = "h", onValueChange = { h = it })
                Text(":", style = MaterialTheme.typography.headlineLarge)
                TimerInput(value = m, label = "m", onValueChange = { m = it })
                Text(":", style = MaterialTheme.typography.headlineLarge)
                TimerInput(value = s, label = "s", onValueChange = { s = it })
            }
            Button(
                onClick = {
                    vm.setTimer(
                        h.toIntOrNull() ?: 0,
                        m.toIntOrNull() ?: 0,
                        s.toIntOrNull() ?: 0
                    )
                    vm.startTimer()
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Start Timer") }
        } else {
            // Running display
            val progress = if (state.totalMs > 0) state.remainingMs.toFloat() / state.totalMs else 0f
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(200.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    trackColor = MaterialTheme.colorScheme.outlineVariant,
                    strokeWidth = 4.dp
                )
                Text(
                    text = formatMs(state.remainingMs),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.Monospace
                    )
                )
            }

            if (state.isFinished) {
                Text(
                    "Time's up!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = {
                    vm.resetTimer()
                    vm.setTimer(0, 0, 0) // fully reset — re-show input
                }, shape = RoundedCornerShape(12.dp)) {
                    Text("Reset")
                }
                if (!state.isFinished) {
                    Button(
                        onClick = { if (state.isRunning) vm.pauseTimer() else vm.startTimer() },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (state.isRunning) "Pause" else "Resume")
                    }
                }
            }
        }
    }
}

@Composable
private fun TimerInput(value: String, label: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= 2 && (it.isEmpty() || it.all(Char::isDigit))) onValueChange(it) },
        label = { Text(label) },
        modifier = Modifier.width(72.dp),
        singleLine = true,
        shape = RoundedCornerShape(8.dp)
    )
}

// ── Format helpers ─────────────────────────────────────────────────────────

fun formatMs(ms: Long): String {
    val totalSec = ms / 1000
    val centis = (ms % 1000) / 10
    val secs = totalSec % 60
    val mins = (totalSec / 60) % 60
    val hrs = totalSec / 3600
    return if (hrs > 0)
        "%02d:%02d:%02d".format(hrs, mins, secs)
    else
        "%02d:%02d.%02d".format(mins, secs, centis)
}
