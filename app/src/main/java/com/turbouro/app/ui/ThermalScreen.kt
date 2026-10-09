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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.turbouro.app.manager.CpuTelemetry
import com.turbouro.app.manager.GpuTelemetry
import com.turbouro.app.manager.ThermalTracker
import com.turbouro.app.model.ThermalStatus
import com.turbouro.app.ui.component.MetricRow
import com.turbouro.app.ui.component.PerformanceGraph
import com.turbouro.app.ui.component.StatusChip
import com.turbouro.app.ui.component.ThermalIndicator
import com.turbouro.app.ui.component.TurboCard
import com.turbouro.app.ui.theme.TelemetryUnitStyle
import com.turbouro.app.ui.theme.TelemetryValueStyle
import com.turbouro.app.ui.theme.TurboUroTheme
import kotlinx.coroutines.delay

@Composable
fun ThermalScreen() {
    val context = LocalContext.current
    val colors = TurboUroTheme.colors

    val thermalTracker = remember { ThermalTracker(context) }
    val cpuTelemetry = remember { CpuTelemetry() }
    val gpuTelemetry = remember { GpuTelemetry() }

    var batteryTemp by remember { mutableStateOf<Float?>(null) }
    var cpuTemp by remember { mutableStateOf<Float?>(null) }
    var thermalStatus by remember { mutableStateOf(ThermalStatus.NORMAL) }
    val tempHistory = remember { mutableStateListOf<Float>() }

    LaunchedEffect(Unit) {
        while (true) {
            val (tempC, _, _) = thermalTracker.getBatteryTelemetry()
            batteryTemp = tempC
            cpuTemp = cpuTelemetry.getCpuTemperatureC()
            thermalStatus = thermalTracker.getCurrentThermalStatus(tempC)

            if (tempC != null) {
                tempHistory.add(tempC)
                if (tempHistory.size > 30) {
                    tempHistory.removeAt(0)
                }
            }

            delay(2000L)
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
                text = "Manajemen Termal",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = "Pemantauan sensor temperatur dan keamanan perangkat",
                fontSize = 13.sp,
                color = colors.textSecondary
            )
        }

        ThermalIndicator(
            status = thermalStatus,
            temperatureC = batteryTemp
        )

        TurboCard {
            Text(
                text = "SENSOR TEMPERATUR HARDWARE",
                style = androidx.compose.ui.text.TextStyle(
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            MetricRow(
                label = "Temperatur Baterai",
                value = batteryTemp?.let { String.format("%.1f°C", it) } ?: "N/A"
            )

            MetricRow(
                label = "Temperatur CPU",
                value = cpuTemp?.let { String.format("%.1f°C", it) } ?: "N/A"
            )

            MetricRow(
                label = "Temperatur GPU",
                value = "N/A"
            )

            MetricRow(
                label = "Status Throttling Sistem",
                value = thermalStatus.displayText
            )
        }

        TurboCard {
            Text(
                text = "HISTORI TEMPERATUR",
                style = androidx.compose.ui.text.TextStyle(
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            PerformanceGraph(
                dataPoints = tempHistory,
                lineColor = when (thermalStatus) {
                    ThermalStatus.COOL -> colors.info
                    ThermalStatus.NORMAL -> colors.success
                    ThermalStatus.WARM -> colors.warning
                    ThermalStatus.HOT -> colors.warning
                    ThermalStatus.CRITICAL -> colors.danger
                },
                minY = 25f,
                maxY = 50f
            )
        }

        TurboCard {
            Text(
                text = "KEBIJAKAN KEAMANAN TERMAL",
                style = androidx.compose.ui.text.TextStyle(
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "TurboUro mengutamakan integritas fisik perangkat. Kami tidak akan memaksakan profil performa ekstrem ketika sistem Android melaporkan status panas atau kritis.",
                fontSize = 13.sp,
                color = colors.textSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusChip(text = "DINGIN", color = colors.info)
                StatusChip(text = "NORMAL", color = colors.success)
                StatusChip(text = "HANGAT", color = colors.warning)
                StatusChip(text = "KRITIS", color = colors.danger)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
