package com.dotz.utility.ui.clock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dotz.utility.viewmodel.ClockViewModel
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockScreen(
    vm: ClockViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Clock", "Stopwatch", "Timer")

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
            TabRow(selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground) {
                tabs.forEachIndexed { i, title ->
                    Tab(selected = selectedTab == i,
                        onClick = { selectedTab = i },
                        text = { Text(title) })
                }
            }

            when (selectedTab) {
                0 -> DigitalClockTab()
                1 -> StopwatchTab(vm)
                2 -> TimerTab(vm)
            }
        }
    }
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
