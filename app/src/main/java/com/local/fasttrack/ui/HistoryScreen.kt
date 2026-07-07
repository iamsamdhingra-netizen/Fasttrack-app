package com.local.fasttrack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: FastingViewModel) {
    val history by viewModel.history.collectAsState()
    val stats by viewModel.stats.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Your progress", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = FastNavy)
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard("Streak", "${stats.currentStreakDays}d", Modifier.weight(1f))
            StatCard("Total fasts", "${stats.totalFasts}", Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard("Avg length", "%.1fh".format(stats.averageHours), Modifier.weight(1f))
            StatCard("Longest", "%.1fh".format(stats.longestHours), Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))
        Text("History", fontWeight = FontWeight.Bold, color = FastNavy, fontSize = 17.sp)
        Spacer(Modifier.height(8.dp))

        if (history.isEmpty()) {
            Text("No completed fasts yet.", color = FastNavy.copy(alpha = 0.5f))
        } else {
            val fmt = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(history, key = { it.id }) { session ->
                    val durationH = (session.endTime!! - session.startTime) / 3_600_000.0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(FastGreenLight.copy(alpha = 0.5f))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${session.goalHours}h goal", fontWeight = FontWeight.Medium, color = FastNavy)
                            Text(fmt.format(Date(session.startTime)), fontSize = 12.sp, color = FastNavy.copy(alpha = 0.5f))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("%.1fh".format(durationH), fontWeight = FontWeight.Bold, color = FastGreen)
                            IconButton(onClick = { viewModel.deleteFast(session.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = FastNavy.copy(alpha = 0.4f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(FastGreenLight.copy(alpha = 0.6f))
            .padding(14.dp)
    ) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = FastNavy)
        Text(label, fontSize = 12.sp, color = FastNavy.copy(alpha = 0.6f))
    }
}
