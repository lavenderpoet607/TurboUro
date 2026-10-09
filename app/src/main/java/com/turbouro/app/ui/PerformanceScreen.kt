package com.turbouro.app.ui

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turbouro.app.manager.AppLaunchManager
import com.turbouro.app.manager.FpsCalculator
import com.turbouro.app.manager.FpsMetrics
import com.turbouro.app.ui.component.MetricRow
import com.turbouro.app.ui.component.PerformanceGraph
import com.turbouro.app.ui.component.StatusChip
import com.turbouro.app.ui.component.TelemetryCard
import com.turbouro.app.ui.component.TurboCard
import com.turbouro.app.ui.theme.TelemetryUnitStyle
import com.turbouro.app.ui.theme.TelemetryValueStyle
import com.turbouro.app.ui.theme.TurboUroTheme

@Composable
fun PerformanceScreen(
    launchManager: AppLaunchManager
) {
    val context = LocalContext.current
    val colors = TurboUroTheme.colors

    var liveMetrics by remember {
        mutableStateOf(
            FpsMetrics(
                currentFps = null,
                averageFps = null,
                minFps = null,
                maxFps = null,
                onePercentLow = null,
                zeroPointOnePercentLow = null,
                frameTimeMs = null,
                frameDropCount = 0,
                isGameFps = false,
                sourceDescription = "Memulai pemantauan..."
            )
        )
    }

    val fpsHistoryPoints = remember { mutableStateListOf<Float>() }

    val activeSession = launchManager.activeSessionFlow.value

    DisposableEffect(Unit) {
        val calculator = FpsCalculator { metrics ->
            liveMetrics = metrics
            metrics.currentFps?.let {
                fpsHistoryPoints.add(it.toFloat())
                if (fpsHistoryPoints.size > 40) {
                    fpsHistoryPoints.removeAt(0)
                }
            }
        }
        calculator.start(activeSession?.packageName)

        onDispose {
            calculator.stop()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundPrimary)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Performa",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = "Telemetri rendering frame rate dan analisis stabilitas",
                fontSize = 13.sp,
                color = colors.textSecondary
            )
        }

        TurboCard(isLarge = true) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FRAME RATE (FPS)",
                    style = androidx.compose.ui.text.TextStyle(
                        color = colors.textMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                )

                StatusChip(
                    text = if (liveMetrics.isGameFps) "GAME FPS" else "APP FPS",
                    color = if (liveMetrics.isGameFps) colors.accentPrimary else colors.info
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = liveMetrics.currentFps?.toString() ?: "N/A",
                    style = TelemetryValueStyle.copy(fontSize = 36.sp),
                    color = when {
                        liveMetrics.currentFps == null -> colors.textMuted
                        liveMetrics.currentFps!! >= 55 -> colors.accentPrimary
                        liveMetrics.currentFps!! in 30..54 -> colors.warning
                        else -> colors.danger
                    }
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "FPS",
                    style = TelemetryUnitStyle.copy(fontSize = 14.sp),
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            MetricRow(
                label = "Rata-rata FPS",
                value = liveMetrics.averageFps?.let { String.format("%.1f", it) } ?: "N/A"
            )
            MetricRow(
                label = "Minimum FPS",
                value = liveMetrics.minFps?.toString() ?: "N/A"
            )
            MetricRow(
                label = "Maksimum FPS",
                value = liveMetrics.maxFps?.toString() ?: "N/A"
            )
            MetricRow(
                label = "1% Low FPS",
                value = liveMetrics.onePercentLow?.toString() ?: "N/A"
            )
            MetricRow(
                label = "0.1% Low FPS",
                value = liveMetrics.zeroPointOnePercentLow?.toString() ?: "N/A"
            )
        }

        TurboCard {
            Text(
                text = "FRAME TIME & STABILITAS",
                style = androidx.compose.ui.text.TextStyle(
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = liveMetrics.frameTimeMs?.let { String.format("%.1f", it) } ?: "N/A",
                    style = TelemetryValueStyle,
                    color = colors.textPrimary
                )
                if (liveMetrics.frameTimeMs != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ms",
                        style = TelemetryUnitStyle,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            MetricRow(
                label = "Total Frame Drops",
                value = "${liveMetrics.frameDropCount} frame"
            )
        }

        TurboCard {
            Text(
                text = "GRAFIK FPS AKTIF",
                style = androidx.compose.ui.text.TextStyle(
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            PerformanceGraph(
                dataPoints = fpsHistoryPoints,
                lineColor = colors.accentPrimary
            )
        }

        TurboCard {
            Text(
                text = "SUMBER DATA TELEMETRI",
                style = androidx.compose.ui.text.TextStyle(
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = liveMetrics.sourceDescription,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Aplikasi TurboUro membedakan App FPS internal dan Game FPS eksternal. Kami tidak menampilkan angka tiruan acak jika sumber sistem eksternal tidak dapat diakses.",
                fontSize = 12.sp,
                color = colors.textSecondary,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
