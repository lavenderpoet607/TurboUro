package com.turbouro.app.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turbouro.app.manager.AppLaunchManager
import com.turbouro.app.manager.CpuTelemetry
import com.turbouro.app.manager.GpuTelemetry
import com.turbouro.app.manager.SystemGameOptimizer
import com.turbouro.app.manager.ThermalTracker
import com.turbouro.app.model.DeviceTelemetry
import com.turbouro.app.model.ThermalStatus
import com.turbouro.app.service.FpsOverlayService
import com.turbouro.app.storage.TurboUroPreferences
import com.turbouro.app.ui.component.CustomTurboIconButton
import com.turbouro.app.ui.component.MetricRow
import com.turbouro.app.ui.component.PrimaryButton
import com.turbouro.app.ui.component.QuickActionCard
import com.turbouro.app.ui.component.SecondaryButton
import com.turbouro.app.ui.component.StatusChip
import com.turbouro.app.ui.component.TurboCard
import com.turbouro.app.ui.theme.TurboUroTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    launchManager: AppLaunchManager,
    optimizer: SystemGameOptimizer,
    onNavigateToGames: () -> Unit,
    onNavigateToThermal: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSessionDetail: (String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val colors = TurboUroTheme.colors
    val scope = rememberCoroutineScope()

    val thermalTracker = remember { ThermalTracker(context) }
    val cpuTelemetry = remember { CpuTelemetry() }
    val gpuTelemetry = remember { GpuTelemetry() }
    val prefs = remember { TurboUroPreferences(context) }

    val activeSession by launchManager.activeSessionFlow.collectAsState()
    var telemetry by remember { mutableStateOf(DeviceTelemetry()) }
    var isOverlayRunning by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            val (tempC, batteryPct, isCharging) = thermalTracker.getBatteryTelemetry()
            val thermalStatus = thermalTracker.getCurrentThermalStatus(tempC)
            val cpuFreq = cpuTelemetry.getScalingCurrentFreqKHz()
            val gpuFreq = gpuTelemetry.getGpuCurrentFreqKHz()

            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    context.display
                } catch (e: Exception) {
                    null
                }
            } else null
            val refreshRate = display?.refreshRate ?: 60f

            telemetry = telemetry.copy(
                cpuFreqKHz = cpuFreq,
                cpuCores = cpuTelemetry.coreCount,
                gpuFreqKHz = gpuFreq,
                batteryTempC = tempC,
                thermalStatus = thermalStatus,
                batteryPercent = batteryPct,
                isCharging = isCharging,
                refreshRateHz = refreshRate
            )

            delay(1500L)
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TurboUro",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (telemetry.thermalStatus == ThermalStatus.CRITICAL || telemetry.thermalStatus == ThermalStatus.HOT) {
                        "Peringatan: Suhu perangkat meningkat"
                    } else {
                        "Perangkat siap digunakan"
                    },
                    fontSize = 13.sp,
                    color = if (telemetry.thermalStatus == ThermalStatus.CRITICAL) colors.danger else colors.textSecondary
                )
            }

            CustomTurboIconButton(
                icon = Icons.Default.Settings,
                contentDescription = "Pengaturan",
                onClick = onNavigateToSettings
            )
        }

        if (telemetry.thermalStatus == ThermalStatus.CRITICAL || telemetry.thermalStatus == ThermalStatus.HOT) {
            TurboCard(
                borderColor = colors.danger.copy(alpha = 0.5f),
                onClick = onNavigateToThermal
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = colors.danger,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Peringatan Suhu Tinggi",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.danger
                        )
                        Text(
                            text = "Suhu baterai mencapai ${telemetry.displayTemp}. Kurangi beban dan biarkan dingin.",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }

        TurboCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STATUS PERANGKAT",
                    style = androidx.compose.ui.text.TextStyle(
                        color = colors.textMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                )

                StatusChip(
                    text = telemetry.thermalStatus.displayText.uppercase(),
                    color = when (telemetry.thermalStatus) {
                        ThermalStatus.COOL -> colors.info
                        ThermalStatus.NORMAL -> colors.success
                        ThermalStatus.WARM -> colors.warning
                        ThermalStatus.HOT -> colors.warning
                        ThermalStatus.CRITICAL -> colors.danger
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            MetricRow(
                label = "Frekuensi CPU",
                value = telemetry.displayCpuFreq
            )
            MetricRow(
                label = "Frekuensi GPU",
                value = telemetry.displayGpuFreq
            )
            MetricRow(
                label = "Temperatur Baterai",
                value = telemetry.displayTemp
            )
            MetricRow(
                label = "Level Baterai",
                value = "${telemetry.batteryPercent}%${if (telemetry.isCharging) " (Mengisi)" else ""}"
            )
            MetricRow(
                label = "Refresh Rate Layar",
                value = String.format("%.0f Hz", telemetry.refreshRateHz)
            )
        }

        TurboCard {
            Text(
                text = "GAME SAAT INI",
                style = androidx.compose.ui.text.TextStyle(
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            val session = activeSession
            if (session == null) {
                Text(
                    text = "Belum ada game berjalan",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Pilih judul game dari pustaka untuk menerapkan profil performa dan pemantauan.",
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))
                SecondaryButton(
                    text = "Buka Pustaka Game",
                    onClick = onNavigateToGames,
                    icon = Icons.Default.SportsEsports,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = session.gameName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Berjalan sejak ${session.formattedDuration}",
                            fontSize = 12.sp,
                            color = colors.accentPrimary
                        )
                    }
                    StatusChip(
                        text = "AKTIF",
                        color = colors.accentPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                PrimaryButton(
                    text = "Selesaikan Sesi Permainan",
                    onClick = {
                        launchManager.finishActiveSession()
                        onShowMessage("Sesi permainan disimpan ke riwayat")
                    }
                )
            }
        }

        Text(
            text = "Aksi Cepat",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                icon = Icons.Default.PlayArrow,
                title = "Buka Game",
                subtitle = "Pilih pustaka",
                onClick = onNavigateToGames,
                modifier = Modifier.weight(1f)
            )

            QuickActionCard(
                icon = Icons.Default.Layers,
                title = if (isOverlayRunning) "Tutup Overlay" else "FPS Overlay",
                subtitle = if (isOverlayRunning) "Aktif" else "Layar floating",
                onClick = {
                    if (Settings.canDrawOverlays(context)) {
                        if (isOverlayRunning) {
                            val stopIntent = Intent(context, FpsOverlayService::class.java).apply {
                                action = FpsOverlayService.ACTION_STOP_OVERLAY
                            }
                            context.startService(stopIntent)
                            isOverlayRunning = false
                            onShowMessage("Overlay dihentikan")
                        } else {
                            val startIntent = Intent(context, FpsOverlayService::class.java)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(startIntent)
                            } else {
                                context.startService(startIntent)
                            }
                            isOverlayRunning = true
                            onShowMessage("Overlay dimulai")
                        }
                    } else {
                        onShowMessage("Izin overlay belum aktif. Berikan izin di Pusat Izin.")
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                icon = Icons.Default.CleaningServices,
                title = "Bersihkan RAM",
                subtitle = "Bebaskan memori",
                onClick = {
                    val result = optimizer.cleanBackgroundProcesses()
                    onShowMessage(result.summary)
                },
                modifier = Modifier.weight(1f)
            )

            QuickActionCard(
                icon = Icons.Default.Thermostat,
                title = "Status Suhu",
                subtitle = telemetry.displayTemp,
                onClick = onNavigateToThermal,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
