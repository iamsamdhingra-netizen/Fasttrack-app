package com.local.fasttrack.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.local.fasttrack.data.FastSession
import com.local.fasttrack.data.FastingStage
import com.local.fasttrack.data.WaterEntry
import java.util.Locale

private val PRESETS = listOf(16, 18, 20)

@Composable
fun HomeScreen(viewModel: FastingViewModel) {
    val active by viewModel.activeSession.collectAsState()
    val now by viewModel.nowMillis.collectAsState()
    val water by viewModel.todaysWater.collectAsState()
    val weights by viewModel.weights.collectAsState()

    var selectedGoal by remember { mutableStateOf(16) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(12.dp))
        Text("FastTrack", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = FastNavy)
        Spacer(Modifier.height(20.dp))

        if (active == null) {
            // --- Not fasting: choose a plan and start ---
            Text("Choose your fast", fontWeight = FontWeight.Medium, color = FastNavy)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PRESETS.forEach { hours ->
                    val selected = hours == selectedGoal
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) FastGreen else FastGreenLight)
                            .clickable { selectedGoal = hours }
                            .padding(horizontal = 18.dp, vertical = 12.dp)
                    ) {
                        Text(
                            "$hours:${24 - hours}",
                            color = if (selected) androidx.compose.ui.graphics.Color.White else FastNavy,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
            FastRing(progress = 0f, centerLabel = "Ready", centerValue = "00:00:00")
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = { viewModel.startFast(selectedGoal) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FastGreen)
            ) { Text("Start Fasting", fontSize = 16.sp) }
        } else {
            val session = active!!
            val elapsedH = viewModel.elapsedHours(session, now)
            val stage = viewModel.stageFor(session, now)
            val goalSeconds = session.goalHours * 3600.0
            val elapsedSeconds = (now - session.startTime) / 1000.0
            val progress = (elapsedSeconds / goalSeconds).toFloat().coerceIn(0f, 1f)
            val remaining = (goalSeconds - elapsedSeconds).coerceAtLeast(0.0)

            Spacer(Modifier.height(8.dp))
            FastRing(
                progress = progress,
                centerLabel = stage.label,
                centerValue = formatDuration(if (remaining > 0) remaining else elapsedSeconds),
                overGoal = remaining <= 0
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (remaining > 0) "time remaining" else "past goal — keep going or end",
                color = FastNavy.copy(alpha = 0.6f), fontSize = 13.sp
            )
            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                onClick = { viewModel.endFast() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) { Text("End Fasting", color = FastGreen, fontSize = 16.sp) }
        }

        Spacer(Modifier.height(28.dp))
        Text("Daily tasks", fontWeight = FontWeight.Bold, color = FastNavy, fontSize = 17.sp,
            modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WaterCard(water = water, onAdd = { viewModel.logWater(it) }, modifier = Modifier.weight(1f))
            WeightCard(latestKg = weights.firstOrNull()?.weightKg, onLog = { viewModel.logWeight(it) }, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun FastRing(progress: Float, centerLabel: String, centerValue: String, overGoal: Boolean = false) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 22.dp.toPx()
            drawArc(
                color = FastGreenLight,
                startAngle = -90f, sweepAngle = 360f, useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                size = Size(size.width - stroke, size.height - stroke),
                topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2)
            )
            drawArc(
                color = if (overGoal) FastOrange else FastGreen,
                startAngle = -90f, sweepAngle = 360f * progress, useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                size = Size(size.width - stroke, size.height - stroke),
                topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(centerLabel, color = FastNavy.copy(alpha = 0.6f), fontSize = 15.sp)
            Spacer(Modifier.height(6.dp))
            Text(centerValue, color = FastNavy, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun WaterCard(water: List<WaterEntry>, onAdd: (Int) -> Unit, modifier: Modifier = Modifier) {
    val total = water.sumOf { it.amountMl }
    val goal = 2400
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(FastGreenLight.copy(alpha = 0.5f))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Water", color = FastNavy, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape).background(FastBlue.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) { Text("💧", fontSize = 22.sp) }
        Spacer(Modifier.height(6.dp))
        Text("$total", fontWeight = FontWeight.Bold, color = FastNavy, fontSize = 18.sp)
        Text("/${goal} ml", color = FastNavy.copy(alpha = 0.5f), fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SmallAddChip("+250") { onAdd(250) }
        }
    }
}

@Composable
private fun WeightCard(latestKg: Double?, onLog: (Double) -> Unit, modifier: Modifier = Modifier) {
    var showDialog by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(FastGreenLight.copy(alpha = 0.5f))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Weight", color = FastNavy, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape).background(FastGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) { Text("⚖️", fontSize = 22.sp) }
        Spacer(Modifier.height(6.dp))
        Text(
            latestKg?.let { String.format(Locale.US, "%.1f", it) } ?: "--",
            fontWeight = FontWeight.Bold, color = FastNavy, fontSize = 18.sp
        )
        Text("kg", color = FastNavy.copy(alpha = 0.5f), fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        SmallAddChip("Log") { showDialog = true }
    }

    if (showDialog) {
        var text by remember { mutableStateOf(latestKg?.toString() ?: "") }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    text.toDoubleOrNull()?.let { onLog(it) }
                    showDialog = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } },
            title = { Text("Log weight (kg)") },
            text = {
                OutlinedTextField(value = text, onValueChange = { text = it }, singleLine = true)
            }
        )
    }
}

@Composable
private fun SmallAddChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(FastGreen)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) { Text(label, color = androidx.compose.ui.graphics.Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
}

private fun formatDuration(totalSeconds: Double): String {
    val s = totalSeconds.toLong().coerceAtLeast(0)
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return String.format(Locale.US, "%02d:%02d:%02d", h, m, sec)
}

