package com.local.fasttrack.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val FastGreen = Color(0xFF1FBE7A)
val FastGreenLight = Color(0xFFDFF6EC)
val FastOrange = Color(0xFFFF7A3D)
val FastNavy = Color(0xFF162447)
val FastBlue = Color(0xFF3D8BFF)

private val LightColors = lightColorScheme(
    primary = FastGreen,
    secondary = FastOrange,
    background = Color(0xFFF3FBF6),
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = FastNavy,
    onSurface = FastNavy
)

private val DarkColors = darkColorScheme(
    primary = FastGreen,
    secondary = FastOrange,
    background = Color(0xFF0E1420),
    surface = Color(0xFF17202E),
)

@Composable
fun FastTrackTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
