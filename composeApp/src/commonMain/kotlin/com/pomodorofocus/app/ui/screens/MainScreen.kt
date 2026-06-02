package com.pomodorofocus.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pomodorofocus.app.platform.HapticFeedback
import kotlinx.coroutines.delay

enum class TimerMode { FOCUS, SHORT_BREAK, LONG_BREAK }

data class TimerState(
    val mode: TimerMode = TimerMode.FOCUS,
    val secondsRemaining: Int = 25 * 60,
    val isRunning: Boolean = false,
    val completedSessions: Int = 0,
    val totalFocusMinutes: Int = 0,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var state by remember { mutableStateOf(TimerState()) }
    var showStats by remember { mutableStateOf(false) }
    val haptic = remember { HapticFeedback() }

    LaunchedEffect(state.isRunning) {
        if (state.isRunning) {
            while (state.secondsRemaining > 0) {
                delay(1000L)
                state = state.copy(secondsRemaining = state.secondsRemaining - 1)
            }
            haptic.medium()
            if (state.mode == TimerMode.FOCUS) {
                val n = state.completedSessions + 1
                val nextMode = if (n % 4 == 0) TimerMode.LONG_BREAK else TimerMode.SHORT_BREAK
                val nextSecs = if (n % 4 == 0) 15 * 60 else 5 * 60
                state = state.copy(completedSessions = n, totalFocusMinutes = state.totalFocusMinutes + 25,
                    isRunning = false, mode = nextMode, secondsRemaining = nextSecs)
            } else {
                state = state.copy(isRunning = false, mode = TimerMode.FOCUS, secondsRemaining = 25 * 60)
            }
        }
    }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text("Focus Timer", fontWeight = FontWeight.Bold) },
            actions = { IconButton(onClick = { showStats = !showStats }) {
                Icon(if (showStats) Icons.Default.Timer else Icons.Default.BarChart, null) }
            })
    }) { p ->
        if (showStats) StatsTab(state)
        else TimerTab(state,
            onStart = { state = state.copy(isRunning = true) },
            onPause = { state = state.copy(isRunning = false) },
            onReset = { state = state.copy(isRunning = false, secondsRemaining = 25 * 60) },
            onModeChange = { m ->
                val s = when (m) { TimerMode.FOCUS -> 25 * 60; TimerMode.SHORT_BREAK -> 5 * 60; TimerMode.LONG_BREAK -> 15 * 60 }
                state = state.copy(mode = m, secondsRemaining = s, isRunning = false)
            })
    }
}

@Composable
private fun TimerTab(state: TimerState, onStart: () -> Unit, onPause: () -> Unit, onReset: () -> Unit, onModeChange: (TimerMode) -> Unit) {
    val total = when (state.mode) { TimerMode.FOCUS -> 25 * 60; TimerMode.SHORT_BREAK -> 5 * 60; TimerMode.LONG_BREAK -> 15 * 60 }
    val progress = if (total > 0) state.secondsRemaining.toFloat() / total else 1f
    val min = state.secondsRemaining / 60
    val sec = state.secondsRemaining % 60

    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(20.dp))
        Row(Modifier.clip(RoundedCornerShape(24.dp)), horizontalArrangement = Arrangement.Center) {
            TimerMode.entries.forEach { m ->
                val label = when (m) { TimerMode.FOCUS -> "Focus"; TimerMode.SHORT_BREAK -> "Break"; TimerMode.LONG_BREAK -> "Long" }
                val sel = state.mode == m
                TextButton(onClick = { onModeChange(m) },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (sel) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant),
                    shape = RoundedCornerShape(20.dp)) { Text(label, fontWeight = FontWeight.Medium) }
            }
        }
        Spacer(Modifier.weight(1f))
        Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
            val arcColor = when (state.mode) { TimerMode.FOCUS -> MaterialTheme.colorScheme.primary; TimerMode.SHORT_BREAK -> MaterialTheme.colorScheme.secondary; else -> Color(0xFF3498DB) }
            Canvas(Modifier.fillMaxSize()) {
                val stroke = 12.dp.toPx()
                val s = size.width - stroke
                drawArc(MaterialTheme.colorScheme.surfaceVariant, -90f, 360f, false, Offset(stroke / 2, stroke / 2), Size(s, s), style = Stroke(stroke, cap = StrokeCap.Round))
                drawArc(arcColor, -90f, 360f * (1f - progress), false, Offset(stroke / 2, stroke / 2), Size(s, s), style = Stroke(stroke, cap = StrokeCap.Round))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("%02d:%02d".format(min, sec), fontSize = 52.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(8.dp))
                Text(when (state.mode) { TimerMode.FOCUS -> "FOCUS TIME"; TimerMode.SHORT_BREAK -> "BREAK"; TimerMode.LONG_BREAK -> "LONG BREAK" },
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.weight(0.5f))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            FilledTonalButton(onClick = onReset, Modifier.size(56.dp), shape = CircleShape, contentPadding = PaddingValues(0.dp)) {
                Icon(Icons.Default.Refresh, null, Modifier.size(24.dp))
            }
            Button(onClick = { if (state.isRunning) onPause() else onStart() }, Modifier.size(72.dp), shape = CircleShape, contentPadding = PaddingValues(0.dp)) {
                Icon(if (state.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow, null, Modifier.size(36.dp))
            }
            Button(onClick = { if (state.mode != TimerMode.FOCUS) onModeChange(TimerMode.FOCUS) },
                Modifier.size(56.dp), shape = CircleShape, contentPadding = PaddingValues(0.dp),
                enabled = state.mode != TimerMode.FOCUS) {
                Icon(Icons.Default.SkipNext, null, Modifier.size(24.dp))
            }
        }
        Spacer(Modifier.weight(0.3f))
        if (state.completedSessions > 0) {
            Row(horizontalArrangement = Arrangement.Center) {
                repeat(state.completedSessions.coerceAtMost(12)) { Text("\uD83C\uDF45", fontSize = 14.sp); Spacer(Modifier.width(2.dp)) }
            }
            Spacer(Modifier.height(8.dp))
            Text("${state.completedSessions} pomodoro completed today", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatsTab(state: TimerState) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Spacer(Modifier.height(20.dp))
        Text("Today's Focus", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Pomodoros", state.completedSessions.toString(), Icons.Default.CheckCircle, Modifier.weight(1f))
            StatCard("Focus Time", "${state.totalFocusMinutes}m", Icons.Default.Timer, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Streak", "0 days", Icons.Default.LocalFireDepartment, Modifier.weight(1f))
            val avg = if (state.completedSessions > 0) state.totalFocusMinutes / state.completedSessions else 0
            StatCard("Avg Session", "${avg}m", Icons.Default.TrendingUp, Modifier.weight(1f))
        }
        Spacer(Modifier.height(24.dp))
        Text("Progress", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        val goal = 8; val fill = (state.completedSessions.toFloat() / goal).coerceAtMost(1f)
        Column {
            Row(Modifier.fillMaxWidth()) {
                Text("Daily Goal: 8 pomodoros", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                Text("${state.completedSessions}/8", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = fill, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)))
        }
        Spacer(Modifier.height(24.dp))
        Text("Tips", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("- A pomodoro is 25 min of focused work", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("- Take a 5 min break between pomodoros", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("- After 4 pomodoros, take a longer 15-30 min break", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
