package com.turbouro.app.ui

import android.content.pm.PackageManager
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.turbouro.app.integration.ShizukuManager
import com.turbouro.app.manager.AppLaunchManager
import com.turbouro.app.manager.CapabilityManager
import com.turbouro.app.model.CapabilityState
import com.turbouro.app.model.GameProfile
import com.turbouro.app.model.ProfileType
import com.turbouro.app.storage.GameProfileStore
import com.turbouro.app.storage.SessionHistoryStore
import com.turbouro.app.ui.component.CustomTurboIconButton
import com.turbouro.app.ui.component.MetricRow
import com.turbouro.app.ui.component.PrimaryButton
import com.turbouro.app.ui.component.SecondaryButton
import com.turbouro.app.ui.component.SettingsTile
import com.turbouro.app.ui.component.StatusChip
import com.turbouro.app.ui.component.TurboCard
import com.turbouro.app.ui.theme.SmallButtonShape
import com.turbouro.app.ui.theme.TurboUroTheme

@Composable
fun GameDetailScreen(
    packageName: String,
    profileStore: GameProfileStore,
    sessionStore: SessionHistoryStore,
    launchManager: AppLaunchManager,
    capabilityManager: CapabilityManager,
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val colors = TurboUroTheme.colors

    val appLabel = remember(packageName) {
        try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    val appIcon = remember(packageName) {
        try {
            val drawable = context.packageManager.getApplicationIcon(packageName)
            drawable.toBitmap(120, 120).asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    val initialProfile = remember(packageName) {
        profileStore.getProfile(packageName, appLabel)
    }

    var currentProfile by remember { mutableStateOf(initialProfile) }
    var selectedType by remember { mutableStateOf(initialProfile.profileType) }
    var gameModeState by remember { mutableStateOf(capabilityManager.checkGameModeCapability()) }
    val shizukuReady = ShizukuManager.hasPermission()

    val recentSessions = remember(packageName) {
        sessionStore.getAllSessions().filter { it.packageName == packageName }.take(3)
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
            CustomTurboIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Kembali",
                onClick = onBack
            )

            CustomTurboIconButton(
                icon = Icons.Default.Delete,
                contentDescription = "Hapus Game",
                onClick = {
                    profileStore.removeTrackedGame(packageName)
                    onShowMessage("$appLabel dihapus dari pustaka")
                    onBack()
                },
                tint = colors.danger
            )
        }

        TurboCard(isLarge = true) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (appIcon != null) {
                    Image(
                        bitmap = appIcon,
                        contentDescription = appLabel,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(colors.surfaceElevated, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = colors.accentPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = appLabel,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Text(
                    text = packageName,
                    fontSize = 12.sp,
                    color = colors.textMuted
                )

                Spacer(modifier = Modifier.height(10.dp))

                StatusChip(
                    text = "PROFIL: ${currentProfile.profileType.displayName.uppercase()}",
                    color = colors.accentPrimary
                )

                Spacer(modifier = Modifier.height(18.dp))

                PrimaryButton(
                    text = "Mainkan Sekarang",
                    icon = Icons.Default.PlayArrow,
                    onClick = {
                        profileStore.saveProfile(currentProfile)
                        val launched = launchManager.launchGame(packageName, appLabel)
                        if (launched) {
                            onShowMessage("Membuka $appLabel dengan profil ${currentProfile.profileType.displayName}")
                        } else {
                            onShowMessage("Gagal membuka game")
                        }
                    }
                )
            }
        }

        TurboCard {
            Text(
                text = "PILIHAN PROFIL",
                style = androidx.compose.ui.text.TextStyle(
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileType.entries.forEach { pType ->
                    val isSelected = selectedType == pType
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(SmallButtonShape)
                            .background(if (isSelected) colors.accentPrimary.copy(alpha = 0.2f) else colors.surfaceElevated)
                            .border(1.dp, if (isSelected) colors.accentPrimary else colors.border, SmallButtonShape)
                            .clickable {
                                selectedType = pType
                                val newProfile = GameProfile.defaultFor(packageName, appLabel, pType)
                                currentProfile = newProfile
                                profileStore.saveProfile(newProfile)
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = pType.displayName,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) colors.accentPrimary else colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = currentProfile.profileType.description,
                fontSize = 12.sp,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            SettingsTile(
                title = "Android Game Mode",
                subtitle = if (gameModeState == CapabilityState.AVAILABLE) "Gunakan Android 12+ official Game Manager" else "Tidak tersedia di perangkat ini",
                checked = currentProfile.gameModeEnabled && gameModeState == CapabilityState.AVAILABLE,
                onCheckedChange = {
                    if (gameModeState == CapabilityState.AVAILABLE) {
                        val updated = currentProfile.copy(gameModeEnabled = it)
                        currentProfile = updated
                        profileStore.saveProfile(updated)
                    } else {
                        onShowMessage("Game Mode API tidak didukung pada versi Android/perangkat ini")
                    }
                }
            )

            SettingsTile(
                title = "Auto Optimize Sebelum Main",
                subtitle = "Terapkan profil otomatis saat game dimulai",
                checked = currentProfile.autoOptimize,
                onCheckedChange = {
                    val updated = currentProfile.copy(autoOptimize = it)
                    currentProfile = updated
                    profileStore.saveProfile(updated)
                }
            )

            SettingsTile(
                title = "FPS Overlay Saat Bermain",
                subtitle = "Tampilkan floating meter performa di sidebar",
                checked = currentProfile.fpsOverlay,
                onCheckedChange = {
                    val updated = currentProfile.copy(fpsOverlay = it)
                    currentProfile = updated
                    profileStore.saveProfile(updated)
                }
            )

            SettingsTile(
                title = "Proteksi Termal",
                subtitle = "Peringatkan saat suhu mencapai ambang batas panas",
                checked = currentProfile.thermalProtection,
                onCheckedChange = {
                    val updated = currentProfile.copy(thermalProtection = it)
                    currentProfile = updated
                    profileStore.saveProfile(updated)
                }
            )

            SettingsTile(
                title = "Pembersihan Proses Latar Belakang",
                subtitle = "Hentikan proses non-kritis sebelum game berjalan",
                checked = currentProfile.backgroundCleanup,
                onCheckedChange = {
                    val updated = currentProfile.copy(backgroundCleanup = it)
                    currentProfile = updated
                    profileStore.saveProfile(updated)
                }
            )
        }

        TurboCard {
            Text(
                text = "PENGATURAN PERFORMA LANJUTAN & TOOLBOX",
                style = androidx.compose.ui.text.TextStyle(
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            SettingsTile(
                title = "Unlock Refresh Rate Sistem (via Shizuku)",
                subtitle = if (shizukuReady) "Paksa display refresh rate target via sistem shell" else "Membutuhkan otorisasi Shizuku aktif",
                checked = currentProfile.unlockFpsEnabled,
                onCheckedChange = {
                    if (it && !shizukuReady) {
                        onShowMessage("Aktifkan dan berikan izin Shizuku terlebih dahulu di Pusat Izin")
                    } else {
                        val updated = currentProfile.copy(unlockFpsEnabled = it)
                        currentProfile = updated
                        profileStore.saveProfile(updated)
                    }
                }
            )

            if (currentProfile.unlockFpsEnabled) {
                Column(modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 10.dp)) {
                    Text(
                        text = "Target Refresh Rate:",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(60, 90, 120, 144).forEach { hz ->
                            val isSelected = currentProfile.customRefreshRate == hz
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(SmallButtonShape)
                                    .background(if (isSelected) colors.accentPrimary.copy(alpha = 0.25f) else colors.surfaceElevated)
                                    .border(1.dp, if (isSelected) colors.accentPrimary else colors.border, SmallButtonShape)
                                    .clickable {
                                        val updated = currentProfile.copy(customRefreshRate = hz)
                                        currentProfile = updated
                                        profileStore.saveProfile(updated)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$hz Hz",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) colors.accentPrimary else colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            SettingsTile(
                title = "Governor & Fixed Performance (via Shizuku)",
                subtitle = "Terapkan mode performa daya tetap dan scaling CPU maksimal",
                checked = currentProfile.advancedGovernorTuning,
                onCheckedChange = {
                    if (it && !shizukuReady) {
                        onShowMessage("Fitur tuning kernel governor memerlukan izin Shizuku")
                    } else {
                        val updated = currentProfile.copy(advancedGovernorTuning = it)
                        currentProfile = updated
                        profileStore.saveProfile(updated)
                    }
                }
            )

            SettingsTile(
                title = "Custom Crosshair Assistant",
                subtitle = "Tampilkan titik bidik kustom di tengah layar game",
                checked = currentProfile.crosshairEnabled,
                onCheckedChange = {
                    val updated = currentProfile.copy(crosshairEnabled = it)
                    currentProfile = updated
                    profileStore.saveProfile(updated)
                }
            )

            if (currentProfile.crosshairEnabled) {
                Column(modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 10.dp)) {
                    Text(
                        text = "Gaya Crosshair:",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("CROSS" to "Silang", "DOT" to "Titik", "CIRCLE" to "Lingkar").forEach { (styleKey, styleLabel) ->
                            val isSelected = currentProfile.crosshairStyle.equals(styleKey, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(SmallButtonShape)
                                    .background(if (isSelected) colors.accentPrimary.copy(alpha = 0.25f) else colors.surfaceElevated)
                                    .border(1.dp, if (isSelected) colors.accentPrimary else colors.border, SmallButtonShape)
                                    .clickable {
                                        val updated = currentProfile.copy(crosshairStyle = styleKey)
                                        currentProfile = updated
                                        profileStore.saveProfile(updated)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = styleLabel,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) colors.accentPrimary else colors.textSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Warna Bidik:",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf(
                            "#00E5FF" to "Cyan",
                            "#00E676" to "Hijau",
                            "#FF5C67" to "Merah",
                            "#FFEA00" to "Kuning"
                        ).forEach { (colorHex, colorLabel) ->
                            val isColorSelected = currentProfile.crosshairColor.equals(colorHex, ignoreCase = true)
                            val parsedColor = Color(android.graphics.Color.parseColor(colorHex))
                            Row(
                                modifier = Modifier
                                    .clip(SmallButtonShape)
                                    .background(if (isColorSelected) parsedColor.copy(alpha = 0.2f) else colors.surfaceElevated)
                                    .border(1.dp, if (isColorSelected) parsedColor else colors.border, SmallButtonShape)
                                    .clickable {
                                        val updated = currentProfile.copy(crosshairColor = colorHex)
                                        currentProfile = updated
                                        profileStore.saveProfile(updated)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(parsedColor)
                                )
                                Text(
                                    text = colorLabel,
                                    fontSize = 11.sp,
                                    color = if (isColorSelected) parsedColor else colors.textSecondary,
                                    fontWeight = if (isColorSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            SettingsTile(
                title = "Mode Jangan Ganggu (DND Otomatis)",
                subtitle = "Senyapkan notifikasi dan pop-up saat game berjalan",
                checked = currentProfile.dndModeEnabled,
                onCheckedChange = {
                    val updated = currentProfile.copy(dndModeEnabled = it)
                    currentProfile = updated
                    profileStore.saveProfile(updated)
                }
            )

            SettingsTile(
                title = "Kunci Kecerahan Layar",
                subtitle = "Cegah layar meredup sendiri akibat panas perangkat",
                checked = currentProfile.lockBrightness,
                onCheckedChange = {
                    val updated = currentProfile.copy(lockBrightness = it)
                    currentProfile = updated
                    profileStore.saveProfile(updated)
                }
            )

            SettingsTile(
                title = "Mini Frame Time Graph di Sidebar",
                subtitle = "Tampilkan kurva gelombang latensi frame di HUD",
                checked = currentProfile.showMiniGraphInOverlay,
                onCheckedChange = {
                    val updated = currentProfile.copy(showMiniGraphInOverlay = it)
                    currentProfile = updated
                    profileStore.saveProfile(updated)
                }
            )

            SettingsTile(
                title = "Peringatan Getar Suhu Kritis (>= 43°C)",
                subtitle = "Beri getaran lembut saat suhu perangkat mencapai batas aman",
                checked = currentProfile.thermalHapticAlert,
                onCheckedChange = {
                    val updated = currentProfile.copy(thermalHapticAlert = it)
                    currentProfile = updated
                    profileStore.saveProfile(updated)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            SecondaryButton(
                text = "Simpan Seluruh Pengaturan",
                onClick = {
                    profileStore.saveProfile(currentProfile)
                    onShowMessage("Seluruh pengaturan untuk $appLabel berhasil disimpan")
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (recentSessions.isNotEmpty()) {
            TurboCard {
                Text(
                    text = "RIWAYAT SESI GAME INI",
                    style = androidx.compose.ui.text.TextStyle(
                        color = colors.textMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                recentSessions.forEach { sess ->
                    MetricRow(
                        label = "Durasi ${sess.formattedDuration}",
                        value = if (sess.averageFps > 0) String.format("Avg %.0f FPS", sess.averageFps) else "FPS N/A"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
