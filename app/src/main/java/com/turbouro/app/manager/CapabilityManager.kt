package com.turbouro.app.manager

import android.app.AppOpsManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.turbouro.app.integration.ShizukuManager
import com.turbouro.app.model.CapabilityState
import java.io.File

class CapabilityManager(private val context: Context) {

    fun checkGameModeCapability(): CapabilityState {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val gameManager = context.getSystemService(Context.GAME_SERVICE)
                if (gameManager != null) CapabilityState.AVAILABLE else CapabilityState.UNSUPPORTED
            } catch (e: Exception) {
                CapabilityState.ERROR
            }
        } else {
            CapabilityState.UNSUPPORTED
        }
    }

    fun checkThermalCapability(): CapabilityState {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                if (powerManager != null) CapabilityState.AVAILABLE else CapabilityState.UNSUPPORTED
            } catch (e: Exception) {
                CapabilityState.ERROR
            }
        } else {
            CapabilityState.LIMITED
        }
    }

    fun checkOverlayCapability(): CapabilityState {
        return try {
            if (Settings.canDrawOverlays(context)) {
                CapabilityState.AVAILABLE
            } else {
                CapabilityState.REQUIRES_PERMISSION
            }
        } catch (e: Exception) {
            CapabilityState.ERROR
        }
    }

    fun checkUsageAccessCapability(): CapabilityState {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
            val mode = appOps?.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
            if (mode == AppOpsManager.MODE_ALLOWED) {
                CapabilityState.AVAILABLE
            } else {
                CapabilityState.REQUIRES_PERMISSION
            }
        } catch (e: Exception) {
            CapabilityState.ERROR
        }
    }

    fun checkNotificationCapability(): CapabilityState {
        return try {
            if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                CapabilityState.AVAILABLE
            } else {
                CapabilityState.REQUIRES_PERMISSION
            }
        } catch (e: Exception) {
            CapabilityState.ERROR
        }
    }

    fun checkBatteryOptimizationCapability(): CapabilityState {
        return try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true) {
                CapabilityState.AVAILABLE
            } else {
                CapabilityState.LIMITED
            }
        } catch (e: Exception) {
            CapabilityState.ERROR
        }
    }

    fun checkShizukuCapability(): CapabilityState {
        return ShizukuManager.getShizukuState()
    }

    fun checkCpuTelemetryCapability(): CapabilityState {
        return try {
            val cpu0 = File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")
            if (cpu0.exists() && cpu0.canRead()) {
                CapabilityState.AVAILABLE
            } else {
                val cpuPresent = File("/sys/devices/system/cpu/present")
                if (cpuPresent.exists() && cpuPresent.canRead()) {
                    CapabilityState.LIMITED
                } else {
                    CapabilityState.UNSUPPORTED
                }
            }
        } catch (e: Exception) {
            CapabilityState.UNSUPPORTED
        }
    }

    fun checkGpuTelemetryCapability(): CapabilityState {
        val paths = listOf(
            "/sys/class/kgsl/kgsl-3d0/gpuclk",
            "/sys/class/kgsl/kgsl-3d0/devfreq/cur_freq",
            "/sys/devices/platform/mali-utgard.0/gpu_clk",
            "/sys/devices/platform/13040000.mali/devfreq/13040000.mali/cur_freq"
        )
        return try {
            val found = paths.any { path ->
                val f = File(path)
                f.exists() && f.canRead()
            }
            if (found) CapabilityState.AVAILABLE else CapabilityState.UNSUPPORTED
        } catch (e: Exception) {
            CapabilityState.UNSUPPORTED
        }
    }

    fun checkGameFpsCapability(): CapabilityState {
        val shizukuState = checkShizukuCapability()
        return if (shizukuState == CapabilityState.AVAILABLE) {
            CapabilityState.AVAILABLE
        } else {
            CapabilityState.LIMITED
        }
    }
}
