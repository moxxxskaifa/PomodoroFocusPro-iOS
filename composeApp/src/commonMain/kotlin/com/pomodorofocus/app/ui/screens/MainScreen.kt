package com.pomodorofocus.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pomodorofocus.app.platform.HapticFeedback
import kotlinx.coroutines.delay

enum class TimerMode { FOCUS, SHORT_BREAK, LONG_BREAK }

data class TimerState(
    val mode: TimerMode = TimerMode.FOCUS,
    val secondsRemaining: Int = 25 * 60,
    val totalSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val completedSessions: Int = 0,
    val totalFocusMinutes: Int = 0,
)

@Composable
fun MainScreen() {
    var state by remember { mutableStateOf(TimerState()) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val haptic = remember { HapticFeedback() }
    val density = androidx.compose.ui.platform.LocalDensity.current

    // Timer tick
    LaunchedEffect(state.isRunning) {
        if (state.isRunning) {
            while (state.secondsRemaining > 0) {
                delay(1000L)
                state = state.copy(secondsRemaining = state.secondsRemaining - 1)
            }
            haptic.medium()
            if (state.mode == TimerMode.FOCUS) {
                val ns = state.completedSessions + 1
                val total = state.totalFocusMinutes + 25
                state = state.copy(completedSessions = ns, totalFocusMinutes = total,
                    isRunning = false,
                    mode = if (ns % 4 == 0) TimerMode.LONG_BREAK else TimerMode.SHORT_BREAK,
                    secondsRemaining = if (ns % 4 == 0) 15 * 60 else 5 * 60,
                    totalSeconds = if (ns % 4 == 0) 15 * 60 else 5 * 60)
            } else {
                state = state.copy(isRunning = false, mode = TimerMode.FOCUS,
                    secondsRemaining = 25 * 60, totalSeconds = 25 * 60)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Focus Timer", fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(onClick = { selectedTab = 1 - selectedTab }) {
                        Icon(
                            if (selectedTab == 0) Icons.Default.BarChart else Icons.Default.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            if (selectedTab == 0) TimerScreen(state, onUpdate = { state = it })
            else StatsScreen(state)
        }
    }
}

// ---- TIMER SCREEN ----
@Composable
private fun TimerScreen(state: TimerState, onUpdate: (TimerState) -> Unit) {
    val progress = if (state.totalSeconds > 0) state.secondsRemaining.toFloat() / state.totalSeconds else 1.0f
    val min = state.secondsRemaining / 60
    val sec = state.secondsRemaining % 60

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(12.dp))

        // Mode chips
        Row(
            Modifier.clip(RoundedCornerShape(28.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            TimerMode.entries.forEach { mode ->
                val label = when (mode) { FOCUS -> "Focus"; SHORT_BREAK -> "Break"; LONG_BREAK -> "Long" }
                val sel = state.mode == mode
                val chipColor = when (mode) {
                    TimerMode.FOCUS -> MaterialTheme.colorScheme.primary
                    TimerMode.SHORT_BREAK -> BreakBlue
                    TimerMode.LONG_BREAK -> LongBreakPurple
                }
                Surface(
                    onClick = { onUpdate(state.copy(mode = mode, isRunning = false,
                        secondsRemaining = when(mode){FOCUS->25*60;SHORT_BREAK->5*60;LONG_BREAK->15*60},
                        totalSeconds = when(mode){FOCUS->25*60;SHORT_BREAK->5*60;LONG_BREAK->15*60})) },
                    shape = RoundedCornerShape(26.dp),
                    color = if (sel) chipColor else Color.Transparent,
                    contentColor = if (sel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text(label, modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                         fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }

        Spacer(Modifier.weight(1.0f))

        // Circular timer
        val arcColor = when (state.mode) {
            TimerMode.FOCUS -> MaterialTheme.colorScheme.primary
            TimerMode.SHORT_BREAK -> BreakBlue
            TimerMode.LONG_BREAK -> LongBreakPurple
        }
        val bgColor = MaterialTheme.colorScheme.surfaceVariant

        Box(Modifier.size(280.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = 14.dp.toPx()
                val s = size - stroke
                drawArc(bgColor, -90f, 360f, false, Offset(stroke / 2, stroke / 2), Size(s, s),
                    style = Stroke(stroke, cap = StrokeCap.Round))
                drawArc(arcColor, -90f, 360f * (1.0f - progress), false, Offset(stroke / 2, stroke / 2), Size(s, s),
                    style = Stroke(stroke, cap = StrokeCap.Round))
                // Inner glow ring
                drawArc(arcColor.copy(alpha = 0.15f), -90f, 360f, false, Offset(stroke / 2, stroke / 2), Size(s, s),
                    style = Stroke(stroke + 4.dp.toPx(), cap = StrokeCap.Round))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("%02d:%02d".format(min, sec), fontSize = 56.sp,
                     fontWeight = FontWeight.Bold, letterSpacing = 2.sp,
                     color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(4.dp))
                val modeText = when (state.mode) {
                    FOCUS -> "FOCUS TIME"; SHORT_BREAK -> "SHORT BREAK"; LONG_BREAK -> "LONG BREAK"
                }
                Text(modeText, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                     letterSpacing = 3.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.weight(0.6f))

        // Controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Reset
            FilledTonalButton(onClick = { onUpdate(state.copy(isRunning = false,
                secondsRemaining = state.totalSeconds)) },
                Modifier.size(52.dp), shape = CircleShape, contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Icon(Icons.Default.Refresh, null, Modifier.size(22.dp),
                     tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Start/Pause - main button
            val isRunning = state.isRunning
            Button(onClick = { onUpdate(state.copy(isRunning = !isRunning)) },
                Modifier.size(80.dp), shape = CircleShape, contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = arcColor)) {
                Icon(
                    if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    null, Modifier.size(40.dp), tint = Color.White
                )
            }

            // Skip
            FilledTonalButton(onClick = {
                val next = if (state.mode == TimerMode.FOCUS) TimerMode.SHORT_BREAK else TimerMode.FOCUS
                onUpdate(state.copy(mode = next, isRunning = false,
                    secondsRemaining = when(next){FOCUS->25*60;SHORT_BREAK->5*60;LONG_BREAK->15*60},
                    totalSeconds = when(next){FOCUS->25*60;SHORT_BREAK->5*60;LONG_BREAK->15*60}))
            }, Modifier.size(52.dp), shape = CircleShape, contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Icon(Icons.Default.SkipNext, null, Modifier.size(22.dp),
                     tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.weight(0.3f))

        // Tomato counter
        if (state.completedSessions > 0) {
            Row(horizontalArrangement = Arrangement.Center) {
                Text("🍅", fontSize = 16.sp)
                Spacer(Modifier.width(4.dp))
                val label = if (state.completedSessions > 1) "s" else ""
                Text("${state.completedSessions} pomodoro${label} today",
                     fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.Center) {
                val show = state.completedSessions.coerceAtMost(8)
                repeat(show) {
                    Text("🍅", fontSize = 16.sp)
                    Spacer(Modifier.width(3.dp))
                }
            }
        } else {
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ---- STATS SCREEN ----
@Composable
private fun StatsScreen(state: TimerState) {
    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(12.dp))
        Text("Today's Focus", fontSize = 24.sp, fontWeight = FontWeight.Bold,
             letterSpacing = (-0.5).sp)
        Spacer(Modifier.height(20.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Pomodoros", state.completedSessions.toString(), Icons.Default.CheckCircle,
                MaterialTheme.colorScheme.primary, Modifier.weight(1.0f))
            StatCard("Focus Time", state.totalFocusMinutes.toString() + " min", Icons.Default.Timer,
                PomodoroSecondary, Modifier.weight(1.0f))
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Best Streak", "0 days", Icons.Default.LocalFireDepartment,
                LongBreakPurple, Modifier.weight(1.0f))
            val avg = if (state.completedSessions > 0) state.totalFocusMinutes / state.completedSessions else 0
            StatCard("Avg Session", avg.toString() + " min", Icons.Default.TrendingUp,
                BreakBlue, Modifier.weight(1.0f))
        }

        Spacer(Modifier.height(24.dp))
        Text("Daily Goal", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))

        val goal = 8
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(Modifier.fillMaxWidth()) {
                    Text("Complete 8 pomodoros", fontSize = 14.sp,
                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.weight(1.0f))
                    Text("{state.completedSessions}/8", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(12.dp))
                val fill = (state.completedSessions.toFloat() / goal).coerceAtMost(1.0f)
                LinearProgressIndicator(
                    progress = { fill },
                    modifier = Modifier.fillMaxWidth().height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("What is Pomodoro?", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                listOf(
                    "Work in 25-minute focused sessions",
                    "Take a 5-minute break between sessions",
                    "After 4 sessions, take a 15-30 min break",
                    "Track your progress and build streaks"
                ).forEach { tip ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text("•  ", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        Text(tip, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                             lineHeight = 22.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier, shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, Modifier.size(20.dp), tint = color)
            }
            Spacer(Modifier.height(10.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                 color = MaterialTheme.colorScheme.onSurface)
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
