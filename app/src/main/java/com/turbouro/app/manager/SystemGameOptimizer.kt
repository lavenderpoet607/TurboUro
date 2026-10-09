package com.turbouro.app.manager

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.turbouro.app.integration.ShizukuManager

data class OptimizationResult(
    val gameModeApplied: Boolean,
    val killedPackageCount: Int,
    val summary: String,
    val freedMemoryMb: Long = 0L,
    val availableMemoryMb: Long = 0L,
    val totalMemoryMb: Long = 0L
)

class SystemGameOptimizer(private val context: Context) {

    fun applySystemGameMode(packageName: String, enableHighPerformance: Boolean): Boolean {
        var applied = false

        if (ShizukuManager.hasPermission()) {
            val modeStr = if (enableHighPerformance) "performance" else "standard"
            val modeNum = if (enableHighPerformance) "2" else "1"

            val res1 = ShizukuManager.executeShell("cmd game mode $modeStr $packageName")
            val res2 = ShizukuManager.executeShell("cmd game mode $modeNum $packageName")
            if (enableHighPerformance) {
                ShizukuManager.executeShell("cmd power set-fixed-performance-mode-enabled true")
                ShizukuManager.executeShell("setprop debug.hwui.fps_divisor 1")
            }
            applied = (res1 != null && !res1.contains("Error", ignoreCase = true)) ||
                    (res2 != null && !res2.contains("Error", ignoreCase = true))
        }

        if (!applied && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val gameManager = context.getSystemService(Context.GAME_SERVICE)
                if (gameManager != null) {
                    val targetMode = if (enableHighPerformance) 2 else 1
                    val method = gameManager.javaClass.getMethod(
                        "setGameMode",
                        String::class.java,
                        Int::class.javaPrimitiveType
                    )
                    method.invoke(gameManager, packageName, targetMode)
                    applied = true
                }
            } catch (e: Throwable) {
            }
        }

        return applied
    }

    fun applyFpsUnlock(targetHz: Int, packageName: String? = null): Boolean {
        if (!ShizukuManager.hasPermission()) return false
        return try {
            val hzFloat = "$targetHz.0"
            ShizukuManager.executeShell("settings put system peak_refresh_rate $hzFloat")
            ShizukuManager.executeShell("settings put system min_refresh_rate $hzFloat")
            ShizukuManager.executeShell("settings put system user_refresh_rate $targetHz")
            ShizukuManager.executeShell("settings put global peak_refresh_rate $hzFloat")
            ShizukuManager.executeShell("settings put global min_refresh_rate $hzFloat")
            ShizukuManager.executeShell("settings put secure refresh_rate_mode 2")

            val dm = context.resources.displayMetrics
            val w = maxOf(dm.widthPixels, dm.heightPixels)
            val h = minOf(dm.widthPixels, dm.heightPixels)

            ShizukuManager.executeShell("cmd display set-user-preferred-display-mode $w $h $targetHz")
            ShizukuManager.executeShell("service call SurfaceFlinger 1035 i32 $targetHz")
            ShizukuManager.executeShell("service call SurfaceFlinger 1036 i32 $targetHz")

            ShizukuManager.executeShell("cmd power set-fixed-performance-mode-enabled true")
            if (!packageName.isNullOrBlank()) {
                ShizukuManager.executeShell("device_config put game_overlay $packageName mode=2,fps=$targetHz")
            }
            ShizukuManager.executeShell("setprop debug.hwui.fps_divisor 1")
            ShizukuManager.executeShell("setprop debug.graphics.game_default_frame_rate $targetHz")
            true
        } catch (e: Throwable) {
            false
        }
    }

    fun resetFpsUnlock(packageName: String? = null): Boolean {
        if (!ShizukuManager.hasPermission()) return false
        return try {
            ShizukuManager.executeShell("cmd display clear-user-preferred-display-mode")
            ShizukuManager.executeShell("settings delete system peak_refresh_rate")
            ShizukuManager.executeShell("settings delete system min_refresh_rate")
            ShizukuManager.executeShell("settings delete system user_refresh_rate")
            ShizukuManager.executeShell("settings delete global peak_refresh_rate")
            ShizukuManager.executeShell("settings delete global min_refresh_rate")
            ShizukuManager.executeShell("settings delete secure refresh_rate_mode")
            ShizukuManager.executeShell("setprop debug.graphics.game_default_frame_rate 0")
            if (!packageName.isNullOrBlank()) {
                ShizukuManager.executeShell("device_config delete game_overlay $packageName")
            }
            true
        } catch (e: Throwable) {
            false
        }
    }

    fun applyAdvancedPerformanceTuning(): Boolean {
        if (!ShizukuManager.hasPermission()) return false
        return try {
            ShizukuManager.executeShell("cmd power set-fixed-performance-mode-enabled true")
            ShizukuManager.executeShell("setprop debug.egl.hw 1")
            ShizukuManager.executeShell("for gov in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do echo performance > \$gov; done")
            true
        } catch (e: Throwable) {
            false
        }
    }

    fun resetAdvancedPerformanceTuning(): Boolean {
        if (!ShizukuManager.hasPermission()) return false
        return try {
            ShizukuManager.executeShell("cmd power set-fixed-performance-mode-enabled false")
            ShizukuManager.executeShell("for gov in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do echo schedutil > \$gov; done")
            true
        } catch (e: Throwable) {
            false
        }
    }

    fun setDndMode(enabled: Boolean): Boolean {
        if (ShizukuManager.hasPermission()) {
            val zenVal = if (enabled) "1" else "0"
            ShizukuManager.executeShell("settings put global zen_mode $zenVal")
            return true
        }
        return try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (nm != null && nm.isNotificationPolicyAccessGranted) {
                nm.setInterruptionFilter(
                    if (enabled) NotificationManager.INTERRUPTION_FILTER_PRIORITY
                    else NotificationManager.INTERRUPTION_FILTER_ALL
                )
                true
            } else {
                false
            }
        } catch (e: Throwable) {
            false
        }
    }

    fun setBrightnessLock(enabled: Boolean): Boolean {
        if (ShizukuManager.hasPermission()) {
            val modeVal = if (enabled) "0" else "1"
            ShizukuManager.executeShell("settings put system screen_brightness_mode $modeVal")
            return true
        }
        return false
    }

    fun cleanBackgroundProcesses(targetPackageToKeep: String? = null): OptimizationResult {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        var killedCount = 0

        val memBefore = ActivityManager.MemoryInfo()
        am?.getMemoryInfo(memBefore)
        val availBeforeMb = memBefore.availMem / (1024 * 1024)

        if (ShizukuManager.hasPermission()) {
            ShizukuManager.executeShell("am kill-all")
        }

        try {
            val runningProcesses = am?.runningAppProcesses
            if (runningProcesses != null) {
                for (proc in runningProcesses) {
                    val pkgList = proc.pkgList ?: continue
                    for (pkg in pkgList) {
                        if (pkg == context.packageName || pkg == targetPackageToKeep) continue
                        am.killBackgroundProcesses(pkg)
                        if (ShizukuManager.hasPermission()) {
                            ShizukuManager.executeShell("am trim-memory $pkg COMPLETE")
                        }
                        killedCount++
                    }
                }
            }
        } catch (e: Exception) {
        }

        val memAfter = ActivityManager.MemoryInfo()
        am?.getMemoryInfo(memAfter)
        val availAfterMb = memAfter.availMem / (1024 * 1024)
        val totalMb = memAfter.totalMem / (1024 * 1024)
        val freedMb = (availAfterMb - availBeforeMb).coerceAtLeast(0L)

        val summary = if (freedMb > 0) {
            "Berhasil membersihkan RAM: $killedCount proses latar dipangkas (+${freedMb} MB bebas, total bebas: ${availAfterMb} MB / ${totalMb} MB)"
        } else if (killedCount > 0) {
            "RAM dioptimalkan: $killedCount proses latar dipangkas (Tersedia: ${availAfterMb} MB / ${totalMb} MB)"
        } else {
            "Memori RAM sudah optimal (Tersedia: ${availAfterMb} MB / ${totalMb} MB)"
        }

        return OptimizationResult(
            gameModeApplied = false,
            killedPackageCount = killedCount,
            summary = summary,
            freedMemoryMb = freedMb,
            availableMemoryMb = availAfterMb,
            totalMemoryMb = totalMb
        )
    }
}
