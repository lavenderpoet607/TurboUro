package com.turbouro.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turbouro.app.storage.SessionHistoryStore
import com.turbouro.app.ui.component.CustomTurboIconButton
import com.turbouro.app.ui.component.MetricRow
import com.turbouro.app.ui.component.SecondaryButton
import com.turbouro.app.ui.component.TurboCard
import com.turbouro.app.ui.theme.TurboUroTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SessionDetailScreen(
    sessionId: String,
    sessionStore: SessionHistoryStore,
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val colors = TurboUroTheme.colors
    val session = remember(sessionId) { sessionStore.getSessionById(sessionId) }

    if (session == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.backgroundPrimary)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Sesi tidak ditemukan", color = colors.textPrimary, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            SecondaryButton(text = "Kembali", onClick = onBack)
        }
        return
    }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundPrimary)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CustomTurboIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Kembali",
                onClick = onBack
            )

            CustomTurboIconButton(
                icon = Icons.Default.Delete,
                contentDescription = "Hapus Sesi",
                onClick = {
                    sessionStore.deleteSession(sessionId)
                    onShowMessage("Sesi dihapus")
                    onBack()
                },
                tint = colors.danger
            )
        }

        TurboCard(isLarge = true) {
            Text(
                text = session.gameName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Durasi: ${session.formattedDuration}",
                fontSize = 14.sp,
                color = colors.accentPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = dateFormatter.format(Date(session.startTime)),
                fontSize = 12.sp,
                color = colors.textSecondary
            )
        }

        TurboCard {
            Text(
                text = "STATISTIK FRAME RATE",
                style = androidx.compose.ui.text.TextStyle(
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            MetricRow(
                label = "Rata-rata FPS",
                value = if (session.averageFps > 0) String.format("%.1f", session.averageFps) else "N/A",
                valueColor = colors.accentPrimary
            )
            MetricRow(
                label = "Minimum FPS",
                value = if (session.minFps > 0) session.minFps.toString() else "N/A"
            )
            MetricRow(
                label = "Maksimum FPS",
                value = if (session.maxFps > 0) session.maxFps.toString() else "N/A"
            )
            MetricRow(
                label = "1% Low FPS",
                value = if (session.onePercentLow > 0) session.onePercentLow.toString() else "N/A"
            )
            MetricRow(
                label = "0.1% Low FPS",
                value = if (session.zeroPointOnePercentLow > 0) session.zeroPointOnePercentLow.toString() else "N/A"
            )
            MetricRow(
                label = "Rata-rata Frame Time",
                value = if (session.averageFrameTime > 0) String.format("%.1f ms", session.averageFrameTime) else "N/A"
            )
        }

        TurboCard {
            Text(
                text = "STATUS TERMAL & DAYA",
                style = androidx.compose.ui.text.TextStyle(
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            MetricRow(
                label = "Temperatur Tertinggi",
                value = if (session.maxTemperature > 0) String.format("%.1f°C", session.maxTemperature) else "N/A"
            )
            MetricRow(
                label = "Baterai Awal",
                value = "${session.batteryStart}%"
            )
            MetricRow(
                label = "Baterai Akhir",
                value = "${session.batteryEnd}%"
            )
            MetricRow(
                label = "Konsumsi Baterai",
                value = "${(session.batteryStart - session.batteryEnd).coerceAtLeast(0)}%"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        SecondaryButton(
            text = "Hapus Sesi Ini",
            onClick = {
                sessionStore.deleteSession(sessionId)
                onShowMessage("Sesi dihapus")
                onBack()
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
