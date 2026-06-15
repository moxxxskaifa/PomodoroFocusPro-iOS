package com.pomodorofocus.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var focusMinutes by remember { mutableStateOf(25) }
    var shortBreakMinutes by remember { mutableStateOf(5) }
    var longBreakMinutes by remember { mutableStateOf(15) }
    var mode by remember { mutableStateOf(TimerMode.Focus) }
    var secondsLeft by remember { mutableStateOf(focusMinutes * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var completedFocusSessions by remember { mutableStateOf(0) }
    var totalFocusMinutes by remember { mutableStateOf(0) }

    LaunchedEffect(isRunning, mode, focusMinutes, shortBreakMinutes, longBreakMinutes) {
        if (!isRunning) return@LaunchedEffect
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft = (secondsLeft - 1).coerceAtLeast(0)
        }
        if (secondsLeft == 0) {
            isRunning = false
            if (mode == TimerMode.Focus) {
                val nextSessionCount = completedFocusSessions + 1
                completedFocusSessions = nextSessionCount
                totalFocusMinutes += focusMinutes
                mode = if (nextSessionCount % 4 == 0) {
                    TimerMode.LongBreak
                } else {
                    TimerMode.ShortBreak
                }
            } else {
                mode = TimerMode.Focus
            }
            secondsLeft = mode.durationMinutes(focusMinutes, shortBreakMinutes, longBreakMinutes) * 60
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Focus Timer", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ModeSelector(
                selectedMode = mode,
                enabled = !isRunning,
                onSelect = {
                    mode = it
                    secondsLeft = it.durationMinutes(focusMinutes, shortBreakMinutes, longBreakMinutes) * 60
                }
            )

            Spacer(Modifier.height(28.dp))

            TimerDial(
                mode = mode,
                secondsLeft = secondsLeft,
                totalSeconds = mode.durationMinutes(focusMinutes, shortBreakMinutes, longBreakMinutes) * 60
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = mode.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { isRunning = !isRunning },
                    modifier = Modifier.height(52.dp)
                ) {
                    Text(if (isRunning) "Pause" else "Start", fontSize = 18.sp)
                }
                OutlinedButton(
                    onClick = {
                        isRunning = false
                        secondsLeft = mode.durationMinutes(focusMinutes, shortBreakMinutes, longBreakMinutes) * 60
                    },
                    modifier = Modifier.height(52.dp)
                ) {
                    Text("Reset", fontSize = 18.sp)
                }
            }

            Spacer(Modifier.height(28.dp))

            StatsRow(
                completedFocusSessions = completedFocusSessions,
                totalFocusMinutes = totalFocusMinutes
            )

            Spacer(Modifier.height(24.dp))

            SettingsPanel(
                enabled = !isRunning,
                focusMinutes = focusMinutes,
                shortBreakMinutes = shortBreakMinutes,
                longBreakMinutes = longBreakMinutes,
                onFocusChange = {
                    focusMinutes = it
                    if (mode == TimerMode.Focus) secondsLeft = it * 60
                },
                onShortBreakChange = {
                    shortBreakMinutes = it
                    if (mode == TimerMode.ShortBreak) secondsLeft = it * 60
                },
                onLongBreakChange = {
                    longBreakMinutes = it
                    if (mode == TimerMode.LongBreak) secondsLeft = it * 60
                }
            )
        }
    }
}

@Composable
private fun ModeSelector(
    selectedMode: TimerMode,
    enabled: Boolean,
    onSelect: (TimerMode) -> Unit
) {
    val modes = TimerMode.values()
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        modes.forEachIndexed { index, timerMode ->
            SegmentedButton(
                selected = selectedMode == timerMode,
                onClick = { onSelect(timerMode) },
                enabled = enabled,
                shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size)
            ) {
                Text(timerMode.label)
            }
        }
    }
}

@Composable
private fun TimerDial(
    mode: TimerMode,
    secondsLeft: Int,
    totalSeconds: Int
) {
    val progress = if (totalSeconds == 0) 0f else secondsLeft.toFloat() / totalSeconds.toFloat()
    val dialColor = when (mode) {
        TimerMode.Focus -> MaterialTheme.colorScheme.primary
        TimerMode.ShortBreak -> Color(0xFF4A90D9)
        TimerMode.LongBreak -> Color(0xFF8B5CF6)
    }

    Box(
        modifier = Modifier
            .size(250.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .height(14.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 34.dp),
            color = dialColor,
            trackColor = dialColor.copy(alpha = 0.18f)
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatTime(secondsLeft),
                fontSize = 54.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = mode.label.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = dialColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatsRow(
    completedFocusSessions: Int,
    totalFocusMinutes: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard("Sessions", completedFocusSessions.toString(), Modifier.weight(1f))
        StatCard("Focus min", totalFocusMinutes.toString(), Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsPanel(
    enabled: Boolean,
    focusMinutes: Int,
    shortBreakMinutes: Int,
    longBreakMinutes: Int,
    onFocusChange: (Int) -> Unit,
    onShortBreakChange: (Int) -> Unit,
    onLongBreakChange: (Int) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text("Timer Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(14.dp))
            DurationStepper("Focus", focusMinutes, enabled, 5..90, onFocusChange)
            DurationStepper("Short Break", shortBreakMinutes, enabled, 1..30, onShortBreakChange)
            DurationStepper("Long Break", longBreakMinutes, enabled, 5..45, onLongBreakChange)
        }
    }
}

@Composable
private fun DurationStepper(
    label: String,
    value: Int,
    enabled: Boolean,
    range: IntRange,
    onChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f))
        OutlinedButton(
            onClick = { onChange((value - 1).coerceIn(range)) },
            enabled = enabled && value > range.first,
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) { Text("-") }
        Text(
            "$value min",
            modifier = Modifier.width(72.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedButton(
            onClick = { onChange((value + 1).coerceIn(range)) },
            enabled = enabled && value < range.last,
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) { Text("+") }
    }
}

private enum class TimerMode(
    val label: String,
    val description: String
) {
    Focus("Focus", "Work with full attention until the timer ends."),
    ShortBreak("Break", "Take a short reset before your next focus session."),
    LongBreak("Long", "Recover after four completed focus sessions.");

    fun durationMinutes(focus: Int, shortBreak: Int, longBreak: Int): Int = when (this) {
        Focus -> focus
        ShortBreak -> shortBreak
        LongBreak -> longBreak
    }
}

private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}
