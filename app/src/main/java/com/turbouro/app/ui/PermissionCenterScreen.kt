package com.turbouro.app.ui

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.turbouro.app.integration.ShizukuManager
import com.turbouro.app.manager.CapabilityManager
import com.turbouro.app.manager.PermissionManager
import com.turbouro.app.ui.component.PermissionCard
import com.turbouro.app.ui.component.PrimaryButton
import com.turbouro.app.ui.component.SecondaryButton
import com.turbouro.app.ui.theme.CardShape
import com.turbouro.app.ui.theme.TurboUroTheme

@Composable
fun PermissionCenterScreen(
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val colors = TurboUroTheme.colors
    val permissionManager = remember { PermissionManager(context) }
    val capabilityManager = remember { CapabilityManager(context) }

    var hasOverlay by remember { mutableStateOf(permissionManager.hasOverlayPermission()) }
    var hasUsage by remember { mutableStateOf(permissionManager.hasUsageStatsPermission()) }
    var hasNotification by remember { mutableStateOf(permissionManager.hasNotificationPermission()) }
    var hasBatteryOpt by remember { mutableStateOf(permissionManager.isIgnoringBatteryOptimizations()) }
    var shizukuState by remember { mutableStateOf(capabilityManager.checkShizukuCapability()) }

    var showOverlayDialog by remember { mutableStateOf(false) }
    var showUsageDialog by remember { mutableStateOf(false) }

    val refreshState = {
        hasOverlay = permissionManager.hasOverlayPermission()
        hasUsage = permissionManager.hasUsageStatsPermission()
        hasNotification = permissionManager.hasNotificationPermission()
        hasBatteryOpt = permissionManager.isIgnoringBatteryOptimizations()
        shizukuState = capabilityManager.checkShizukuCapability()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME || event == Lifecycle.Event.ON_START) {
                refreshState()
            }
        }
        val permissionListener = rikka.shizuku.Shizuku.OnRequestPermissionResultListener { _, _ ->
            refreshState()
        }
        val binderListener = rikka.shizuku.Shizuku.OnBinderReceivedListener {
            refreshState()
        }
        try {
            ShizukuManager.addRequestPermissionResultListener(permissionListener)
            ShizukuManager.addBinderReceivedListenerSticky(binderListener)
        } catch (e: Throwable) {
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            try {
                ShizukuManager.removeRequestPermissionResultListener(permissionListener)
                ShizukuManager.removeBinderReceivedListener(binderListener)
            } catch (e: Throwable) {
            }
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotification = isGranted
        refreshState()
    }

    if (showOverlayDialog) {
        AlertDialog(
            onDismissRequest = { showOverlayDialog = false },
            title = {
                Text(
                    text = "Aktifkan Izin Overlay",
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = "Sistem Android mewajibkan izin tampilan di atas aplikasi lain agar HUD telemetri performa dapat tampil saat game berjalan. Tekan Buka Sakelar untuk mengaktifkan izin bagi TurboUro.",
                    color = colors.textSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                PrimaryButton(
                    text = "Buka Sakelar",
                    onClick = {
                        showOverlayDialog = false
                        try {
                            context.startActivity(permissionManager.getOverlayPermissionIntent())
                        } catch (e: Exception) {
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { showOverlayDialog = false }
                ) {
                    Text(text = "Batal", color = colors.textMuted)
                }
            },
            shape = CardShape,
            containerColor = colors.surface,
            tonalElevation = 6.dp
        )
    }

    if (showUsageDialog) {
        AlertDialog(
            onDismissRequest = { showUsageDialog = false },
            title = {
                Text(
                    text = "Aktifkan Akses Penggunaan",
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = "Akses data penggunaan diperlukan agar TurboUro dapat mendeteksi secara otomatis saat game diluncurkan. Tekan Buka Pengaturan untuk memilih TurboUro pada daftar sistem.",
                    color = colors.textSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                PrimaryButton(
                    text = "Buka Pengaturan",
                    onClick = {
                        showUsageDialog = false
                        try {
                            context.startActivity(permissionManager.getUsageStatsPermissionIntent())
                        } catch (e: Exception) {
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { showUsageDialog = false }
                ) {
                    Text(text = "Batal", color = colors.textMuted)
                }
            },
            shape = CardShape,
            containerColor = colors.surface,
            tonalElevation = 6.dp
        )
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
                text = "Pusat Izin",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Setiap izin memiliki tujuan teknis spesifik untuk memastikan telemetri berfungsi secara akurat.",
                fontSize = 13.sp,
                color = colors.textSecondary
            )
        }

        PermissionCard(
            title = "Tampilan di Atas Aplikasi Lain (Overlay)",
            description = "Mengizinkan TurboUro menampilkan floating overlay meter performa saat game sedang dimainkan.",
            isGranted = hasOverlay,
            onActionClick = {
                showOverlayDialog = true
            },
            actionButtonText = "Aktifkan Izin"
        )

        PermissionCard(
            title = "Akses Penggunaan Aplikasi (Usage Stats)",
            description = "Diperlukan untuk mendeteksi secara otomatis ketika game berpindah ke foreground.",
            isGranted = hasUsage,
            onActionClick = {
                showUsageDialog = true
            },
            actionButtonText = "Aktifkan Izin"
        )

        PermissionCard(
            title = "Notifikasi Layanan Sistem",
            description = "Diperlukan untuk status foreground service agar proses monitoring tidak ditutup oleh sistem Android.",
            isGranted = hasNotification,
            onActionClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    try {
                        context.startActivity(permissionManager.getNotificationPermissionIntent())
                    } catch (e: Exception) {
                    }
                }
            },
            actionButtonText = "Izinkan Notifikasi"
        )

        PermissionCard(
            title = "Pengecualian Hemat Baterai",
            description = "Mencegah sistem Android membatasi frekuensi pembacaan sensor selama sesi permainan panjang.",
            isGranted = hasBatteryOpt,
            onActionClick = {
                try {
                    context.startActivity(permissionManager.getBatteryOptimizationIntent())
                } catch (e: Exception) {
                }
            },
            actionButtonText = "Aktifkan Izin"
        )

        PermissionCard(
            title = "Integrasi Shizuku (Opsional)",
            description = "Fitur lanjutan opsional untuk mengakses statistik pipeline render SurfaceFlinger sistem.",
            isGranted = shizukuState.isAvailable,
            onActionClick = {
                if (ShizukuManager.hasPermission()) {
                    refreshState()
                } else if (ShizukuManager.isServiceRunning()) {
                    val requested = ShizukuManager.requestPermission()
                    if (!requested) {
                        ShizukuManager.openShizuku(context)
                    }
                    refreshState()
                } else {
                    ShizukuManager.openShizuku(context)
                }
            },
            actionButtonText = if (shizukuState.isAvailable) {
                "Terhubung"
            } else if (ShizukuManager.isServiceRunning()) {
                "Izinkan Shizuku"
            } else {
                "Buka Shizuku"
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        PrimaryButton(
            text = "Lanjutkan ke Dasbor",
            onClick = {
                refreshState()
                onContinue()
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
