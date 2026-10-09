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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Troubleshoot
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turbouro.app.ui.component.TurboCard
import com.turbouro.app.ui.theme.TurboUroTheme

@Composable
fun MoreScreen(
    onNavigateToThermal: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    onNavigateToOverlaySettings: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPermissions: () -> Unit
) {
    val colors = TurboUroTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundPrimary)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            Text(
                text = "Fitur Lanjutan",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = "Menu utilitas telemetri dan konfigurasi sistem",
                fontSize = 13.sp,
                color = colors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        MoreMenuItem(
            icon = Icons.Default.Thermostat,
            title = "Manajemen Termal",
            subtitle = "Pantau temperatur baterai dan status throttling",
            onClick = onNavigateToThermal
        )

        MoreMenuItem(
            icon = Icons.Default.History,
            title = "Riwayat Permainan",
            subtitle = "Lihat catatan durasi dan FPS sesi sebelumnya",
            onClick = onNavigateToHistory
        )

        MoreMenuItem(
            icon = Icons.Default.Troubleshoot,
            title = "Diagnostik Sistem",
            subtitle = "Periksa matriks dukungan hardware dan API",
            onClick = onNavigateToDiagnostics
        )

        MoreMenuItem(
            icon = Icons.Default.Layers,
            title = "Pengaturan Overlay",
            subtitle = "Kustomisasi metrik dan ukuran jendela floating",
            onClick = onNavigateToOverlaySettings
        )

        MoreMenuItem(
            icon = Icons.Default.Security,
            title = "Pusat Izin",
            subtitle = "Status izin overlay, usage access, dan notifikasi",
            onClick = onNavigateToPermissions
        )

        MoreMenuItem(
            icon = Icons.Default.Settings,
            title = "Pengaturan Umum",
            subtitle = "Tema antarmuka, boot auto-start, dan data",
            onClick = onNavigateToSettings
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun MoreMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colors = TurboUroTheme.colors

    TurboCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.accentPrimary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
