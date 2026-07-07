package com.local.fasttrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.local.fasttrack.ui.FastingViewModel
import com.local.fasttrack.ui.FastTrackTheme
import com.local.fasttrack.ui.HistoryScreen
import com.local.fasttrack.ui.HomeScreen

class MainActivity : ComponentActivity() {
    private val viewModel: FastingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FastTrackTheme {
                var tab by remember { mutableIntStateOf(0) }
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = tab == 0,
                                onClick = { tab = 0 },
                                icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                                label = { Text("Fast") }
                            )
                            NavigationBarItem(
                                selected = tab == 1,
                                onClick = { tab = 1 },
                                icon = { Icon(Icons.Filled.Timeline, contentDescription = "History") },
                                label = { Text("Progress") }
                            )
                        }
                    }
                ) { padding ->
                    Surface(modifier = androidx.compose.ui.Modifier.padding(padding)) {
                        if (tab == 0) HomeScreen(viewModel) else HistoryScreen(viewModel)
                    }
                }
            }
        }
    }
}
