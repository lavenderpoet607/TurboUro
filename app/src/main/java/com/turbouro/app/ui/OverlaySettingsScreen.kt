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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
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
import com.turbouro.app.model.OverlayMode
import com.turbouro.app.storage.TurboUroPreferences
import com.turbouro.app.ui.component.PrimaryButton
import com.turbouro.app.ui.component.SecondaryButton
import com.turbouro.app.ui.component.SettingsTile
import com.turbouro.app.ui.component.TurboCard
import com.turbouro.app.ui.theme.SmallButtonShape
import com.turbouro.app.ui.theme.TurboUroTheme

@Composable
fun OverlaySettingsScreen(
    prefs: TurboUroPreferences,
    onShowMessage: (String) -> Unit
) {
    val colors = TurboUroTheme.colors
    val overlaySettings by prefs.overlaySettingsFlow.collectAsState()
    var currentSettings by remember(overlaySettings) { mutableStateOf(overlaySettings) }

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
                text = "Pengaturan Overlay",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = "Kustomisasi tampilan floating window performa",
                fontSize = 13.sp,
                color = colors.textSecondary
            )
        }

        TurboCard {
            SettingsTile(
                title = "Aktifkan Overlay Otomatis",
                subtitle = "Tampilkan floating meter saat bermain game",
                checked = currentSettings.enabled,
                onCheckedChange = { currentSettings = currentSettings.copy(enabled = it) }
            )
        }

        TurboCard {
            Text(
                text = "TIPE TAMPILAN",
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
                OverlayMode.entries.forEach { mode ->
                    val isSelected = currentSettings.mode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(SmallButtonShape)
                            .background(if (isSelected) colors.accentPrimary.copy(alpha = 0.2f) else colors.surfaceElevated)
                            .border(1.dp, if (isSelected) colors.accentPrimary else colors.border, SmallButtonShape)
                            .clickable { currentSettings = currentSettings.copy(mode = mode) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.displayName,
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
                text = "METRIK YANG DITAMPILKAN",
                style = androidx.compose.ui.text.TextStyle(
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            SettingsTile(
                title = "FPS (Frame Per Detik)",
                checked = currentSettings.showFps,
                onCheckedChange = { currentSettings = currentSettings.copy(showFps = it) }
            )
            SettingsTile(
                title = "Frame Time (ms)",
                checked = currentSettings.showFrameTime,
                onCheckedChange = { currentSettings = currentSettings.copy(showFrameTime = it) }
            )
            SettingsTile(
                title = "Temperatur Baterai",
                checked = currentSettings.showTemperature,
                onCheckedChange = { currentSettings = currentSettings.copy(showTemperature = it) }
            )
            SettingsTile(
                title = "Level Baterai",
                checked = currentSettings.showBattery,
                onCheckedChange = { currentSettings = currentSettings.copy(showBattery = it) }
            )
            SettingsTile(
                title = "Frekuensi CPU (Mode Lengkap)",
                checked = currentSettings.showCpu,
                onCheckedChange = { currentSettings = currentSettings.copy(showCpu = it) }
            )
            SettingsTile(
                title = "Frekuensi GPU (Mode Lengkap)",
                checked = currentSettings.showGpu,
                onCheckedChange = { currentSettings = currentSettings.copy(showGpu = it) }
            )
            SettingsTile(
                title = "Refresh Rate Layar",
                checked = currentSettings.showRefreshRate,
                onCheckedChange = { currentSettings = currentSettings.copy(showRefreshRate = it) }
            )
        }

        TurboCard {
            Text(
                text = "TAMPILAN & PERILAKU",
                style = androidx.compose.ui.text.TextStyle(
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Transparansi: ${currentSettings.opacityPercent}%",
                fontSize = 13.sp,
                color = colors.textPrimary
            )
            Slider(
                value = currentSettings.opacityPercent.toFloat(),
                onValueChange = { currentSettings = currentSettings.copy(opacityPercent = it.toInt()) },
                valueRange = 30f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = colors.accentPrimary,
                    activeTrackColor = colors.accentPrimary,
                    inactiveTrackColor = colors.surfaceElevated
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ukuran Teks: ${currentSettings.textSizeSp} sp",
                fontSize = 13.sp,
                color = colors.textPrimary
            )
            Slider(
                value = currentSettings.textSizeSp.toFloat(),
                onValueChange = { currentSettings = currentSettings.copy(textSizeSp = it.toInt()) },
                valueRange = 10f..20f,
                colors = SliderDefaults.colors(
                    thumbColor = colors.accentPrimary,
                    activeTrackColor = colors.accentPrimary,
                    inactiveTrackColor = colors.surfaceElevated
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingsTile(
                title = "Kunci Posisi Overlay",
                subtitle = "Cegah pergeseran tidak sengaja saat menyentuh layar",
                checked = currentSettings.isLocked,
                onCheckedChange = { currentSettings = currentSettings.copy(isLocked = it) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SecondaryButton(
                text = "Reset Posisi ke Sudut Kiri Atas",
                onClick = {
                    currentSettings = currentSettings.copy(posX = 40, posY = 100)
                    onShowMessage("Posisi overlay di-reset")
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        PrimaryButton(
            text = "Simpan Pengaturan Overlay",
            onClick = {
                prefs.updateOverlaySettings(currentSettings)
                onShowMessage("Pengaturan overlay berhasil disimpan")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
