package com.turbouro.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turbouro.app.model.AppSettings
import com.turbouro.app.model.AppTheme
import com.turbouro.app.model.ProfileType
import com.turbouro.app.storage.SessionHistoryStore
import com.turbouro.app.storage.TurboUroPreferences
import com.turbouro.app.ui.component.PrimaryButton
import com.turbouro.app.ui.component.SecondaryButton
import com.turbouro.app.ui.component.SettingsTile
import com.turbouro.app.ui.component.TurboCard
import com.turbouro.app.ui.theme.CardShape
import com.turbouro.app.ui.theme.SmallButtonShape
import com.turbouro.app.ui.theme.TurboUroTheme

@Composable
fun SettingsScreen(
    prefs: TurboUroPreferences,
    sessionStore: SessionHistoryStore,
    onNavigateToDiagnostics: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val colors = TurboUroTheme.colors
    val appSettings by prefs.appSettingsFlow.collectAsState()
    var currentSettings by remember(appSettings) { mutableStateOf(appSettings) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

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
                text = "Pengaturan",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = "Konfigurasi sistem, tema antarmuka, dan privasi",
                fontSize = 13.sp,
                color = colors.textSecondary
            )
        }

        TurboCard {
            Text(
                text = "TEMA APLIKASI",
                style = androidx.compose.ui.text.TextStyle(
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppTheme.entries.forEach { theme ->
                    val isSelected = currentSettings.theme == theme
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(SmallButtonShape)
                            .background(if (isSelected) colors.accentPrimary.copy(alpha = 0.2f) else colors.surfaceElevated)
                            .border(1.dp, if (isSelected) colors.accentPrimary else colors.border, SmallButtonShape)
                            .clickable {
                                currentSettings = currentSettings.copy(theme = theme)
                                prefs.updateAppSettings(currentSettings)
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = theme.displayName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) colors.accentPrimary else colors.textSecondary
                        )
                    }
                }
            }
        }

        TurboCard {
            Text(
                text = "UMUM",
                style = androidx.compose.ui.text.TextStyle(
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingsTile(
                title = "Mulai Saat Perangkat Booting",
                subtitle = "Jalankan layanan setelah reboot jika overlay aktif",
                checked = currentSettings.startOnBoot,
                onCheckedChange = {
                    currentSettings = currentSettings.copy(startOnBoot = it)
                    prefs.updateAppSettings(currentSettings)
                }
            )

            SettingsTile(
                title = "Deteksi Game Otomatis",
                subtitle = "Terapkan profil otomatis saat game masuk foreground",
                checked = currentSettings.autoDetectGame,
                onCheckedChange = {
                    currentSettings = currentSettings.copy(autoDetectGame = it)
                    prefs.updateAppSettings(currentSettings)
                }
            )
        }

        TurboCard {
            Text(
                text = "KEBIJAKAN PERFORMA",
                style = androidx.compose.ui.text.TextStyle(
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingsTile(
                title = "Proteksi Termal Ketat",
                subtitle = "Prioritaskan penurunan beban saat temperatur tinggi",
                checked = currentSettings.thermalProtection,
                onCheckedChange = {
                    currentSettings = currentSettings.copy(thermalProtection = it)
                    prefs.updateAppSettings(currentSettings)
                }
            )

            SettingsTile(
                title = "Pembersihan Memori Latar Belakang",
                subtitle = "Evakuasi proses background non-kritis",
                checked = currentSettings.backgroundCleanup,
                onCheckedChange = {
                    currentSettings = currentSettings.copy(backgroundCleanup = it)
                    prefs.updateAppSettings(currentSettings)
                }
            )
        }

        TurboCard {
            Text(
                text = "PENYIMPANAN DATA",
                style = androidx.compose.ui.text.TextStyle(
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingsTile(
                title = "Simpan Riwayat Sesi Game",
                subtitle = "Catat statistik FPS dan temperatur ke penyimpanan lokal",
                checked = currentSettings.saveSessionHistory,
                onCheckedChange = {
                    currentSettings = currentSettings.copy(saveSessionHistory = it)
                    prefs.updateAppSettings(currentSettings)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SecondaryButton(
                text = "Hapus Riwayat Sesi Tersimpan",
                onClick = { showClearHistoryDialog = true },
                modifier = Modifier.fillMaxWidth()
            )
        }

        TurboCard {
            Text(
                text = "TENTANG TURBOURO",
                style = androidx.compose.ui.text.TextStyle(
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "TurboUro v1.0.0",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = "Utilitas Performa dan Telemetri Game Android Nyata",
                fontSize = 13.sp,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Prinsip Zero Placebo: Tanpa animasi boost palsu, tanpa RAM cleaner fiktif, dan tanpa angka FPS buatan.",
                fontSize = 12.sp,
                color = colors.textMuted,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            SecondaryButton(
                text = "Periksa Diagnostik Perangkat",
                onClick = onNavigateToDiagnostics,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = {
                Text(
                    text = "Hapus Riwayat Sesi?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Semua catatan sesi permainan yang tersimpan di perangkat ini akan dihapus secara permanen.",
                    fontSize = 14.sp,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    sessionStore.clearAllSessions()
                    showClearHistoryDialog = false
                    onShowMessage("Riwayat sesi telah dikosongkan")
                }) {
                    Text("Hapus", color = colors.danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Batal", color = colors.textSecondary)
                }
            },
            containerColor = colors.surface,
            shape = CardShape
        )
    }
}
