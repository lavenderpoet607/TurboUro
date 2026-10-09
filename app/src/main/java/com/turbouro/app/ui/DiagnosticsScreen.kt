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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turbouro.app.integration.AdbManager
import com.turbouro.app.manager.CapabilityManager
import com.turbouro.app.ui.component.CapabilityRow
import com.turbouro.app.ui.component.MetricRow
import com.turbouro.app.ui.component.TurboCard
import com.turbouro.app.ui.theme.TurboUroTheme

@Composable
fun DiagnosticsScreen() {
    val context = LocalContext.current
    val colors = TurboUroTheme.colors
    val capabilityManager = remember { CapabilityManager(context) }

    val gameModeCap = remember { capabilityManager.checkGameModeCapability() }
    val thermalCap = remember { capabilityManager.checkThermalCapability() }
    val overlayCap = remember { capabilityManager.checkOverlayCapability() }
    val usageCap = remember { capabilityManager.checkUsageAccessCapability() }
    val notifCap = remember { capabilityManager.checkNotificationCapability() }
    val batteryOptCap = remember { capabilityManager.checkBatteryOptimizationCapability() }
    val shizukuCap = remember { capabilityManager.checkShizukuCapability() }
    val adbCap = remember { AdbManager.getAdbState() }
    val cpuCap = remember { capabilityManager.checkCpuTelemetryCapability() }
    val gpuCap = remember { capabilityManager.checkGpuTelemetryCapability() }
    val gameFpsCap = remember { capabilityManager.checkGameFpsCapability() }

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
                text = "Diagnostik Sistem",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = "Matriks kompatibilitas hardware dan API sistem Android",
                fontSize = 13.sp,
                color = colors.textSecondary
            )
        }

        TurboCard(isLarge = true) {
            Text(
                text = "INFORMASI PERANGKAT",
                style = androidx.compose.ui.text.TextStyle(
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            MetricRow(
                label = "Versi Android",
                value = "Android ${Build.VERSION.RELEASE}"
            )
            MetricRow(
                label = "Level API",
                value = "API ${Build.VERSION.SDK_INT}"
            )
            MetricRow(
                label = "Manufaktur",
                value = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
            )
            MetricRow(
                label = "Model",
                value = Build.MODEL
            )
            MetricRow(
                label = "Board / Hardware",
                value = Build.HARDWARE
            )
        }

        TurboCard {
            Text(
                text = "KEMAMPUAN FITUR & API",
                style = androidx.compose.ui.text.TextStyle(
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            CapabilityRow(
                title = "Android Game Mode API",
                state = gameModeCap,
                subtitle = "API resmi sistem (Android 12+)"
            )
            CapabilityRow(
                title = "Hardware Thermal API",
                state = thermalCap,
                subtitle = "Listener throttling sistem"
            )
            CapabilityRow(
                title = "Floating Overlay Window",
                state = overlayCap,
                subtitle = "Izin SYSTEM_ALERT_WINDOW"
            )
            CapabilityRow(
                title = "Usage Access Stats",
                state = usageCap,
                subtitle = "Deteksi game foreground otomatis"
            )
            CapabilityRow(
                title = "Notifikasi Layanan",
                state = notifCap,
                subtitle = "Foreground service notification"
            )
            CapabilityRow(
                title = "Pengecualian Baterai",
                state = batteryOptCap,
                subtitle = "Eksekusi telemetri tanpa limitasi"
            )
            CapabilityRow(
                title = "Integrasi Shizuku",
                state = shizukuCap,
                subtitle = "Binder IPC opsional"
            )
            CapabilityRow(
                title = "Akses Shell ADB",
                state = adbCap,
                subtitle = "Akses shell tingkat sistem"
            )
            CapabilityRow(
                title = "Telemetri Frekuensi CPU",
                state = cpuCap,
                subtitle = "Membaca sysfs scaling cpufreq"
            )
            CapabilityRow(
                title = "Telemetri Frekuensi GPU",
                state = gpuCap,
                subtitle = "Sysfs kgsl / devfreq / mali"
            )
            CapabilityRow(
                title = "Telemetri Game FPS",
                state = gameFpsCap,
                subtitle = "SurfaceFlinger dumpsys render pipeline"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
