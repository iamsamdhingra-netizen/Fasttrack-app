package com.local.fasttrack.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.unit.dp
import com.local.fasttrack.MainActivity
import com.local.fasttrack.data.FastingRepository
import com.local.fasttrack.data.FastingStage
import java.util.Locale

class FastingWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = FastingRepository(context)
        val session = repo.getActiveSession()

        provideContent {
            val bg = ColorProvider(androidx.compose.ui.graphics.Color(0xFFDFF6EC))
            val fg = ColorProvider(androidx.compose.ui.graphics.Color(0xFF162447))

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(bg)
                    .padding(12.dp)
                    .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                if (session == null) {
                    Text(
                        "Not fasting",
                        style = TextStyle(color = fg, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    )
                } else {
                    val now = System.currentTimeMillis()
                    val elapsedH = (now - session.startTime) / 3_600_000.0
                    val stage = FastingStage.forElapsedHours(elapsedH)
                    val goalSeconds = session.goalHours * 3600.0
                    val elapsedSeconds = (now - session.startTime) / 1000.0
                    val remaining = (goalSeconds - elapsedSeconds)
                    val display = formatHhMm(if (remaining > 0) remaining else elapsedSeconds)

                    Text(stage.label, style = TextStyle(color = fg, fontWeight = FontWeight.Bold))
                    Text(
                        display,
                        style = TextStyle(color = fg, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    )
                    Text(
                        if (remaining > 0) "left to go" else "past goal",
                        style = TextStyle(color = fg)
                    )
                }
            }
        }
    }

    private fun formatHhMm(totalSeconds: Double): String {
        val s = totalSeconds.toLong().coerceAtLeast(0)
        val h = s / 3600
        val m = (s % 3600) / 60
        return String.format(Locale.US, "%02d:%02d", h, m)
    }
}

suspend fun updateFastingWidget(context: Context) {
    val manager = GlanceAppWidgetManager(context)
    val ids = manager.getGlanceIds(FastingWidget::class.java)
    ids.forEach { id -> FastingWidget().update(context, id) }
}
