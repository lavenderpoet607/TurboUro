package com.turbouro.app.manager

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.turbouro.app.model.GameProfile
import com.turbouro.app.model.GameSession
import com.turbouro.app.service.FpsOverlayService
import com.turbouro.app.storage.GameProfileStore
import com.turbouro.app.storage.SessionHistoryStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class AppLaunchManager(
    private val context: Context,
    private val profileStore: GameProfileStore,
    private val sessionStore: SessionHistoryStore,
    private val optimizer: SystemGameOptimizer,
    private val thermalTracker: ThermalTracker
) {
    private val _activeSessionFlow = MutableStateFlow<GameSession?>(null)
    val activeSessionFlow: StateFlow<GameSession?> = _activeSessionFlow.asStateFlow()

    fun launchGame(packageName: String, gameName: String): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val profile = profileStore.getProfile(packageName, gameName)

        if (profile.autoOptimize || profile.gameModeEnabled) {
            optimizer.applySystemGameMode(packageName, profile.performanceMode)
        }
        if (profile.backgroundCleanup || profile.autoOptimize) {
            optimizer.cleanBackgroundProcesses(packageName)
        }
        if (profile.unlockFpsEnabled) {
            optimizer.applyFpsUnlock(profile.customRefreshRate, packageName)
        }
        if (profile.advancedGovernorTuning) {
            optimizer.applyAdvancedPerformanceTuning()
        }
        if (profile.dndModeEnabled) {
            optimizer.setDndMode(true)
        }
        if (profile.lockBrightness) {
            optimizer.setBrightnessLock(true)
        }

        if (profile.fpsOverlay && Settings.canDrawOverlays(context)) {
            val serviceIntent = Intent(context, FpsOverlayService::class.java).apply {
                putExtra(FpsOverlayService.EXTRA_TARGET_PACKAGE, packageName)
                putExtra(FpsOverlayService.EXTRA_CROSSHAIR_ENABLED, profile.crosshairEnabled)
                putExtra(FpsOverlayService.EXTRA_CROSSHAIR_STYLE, profile.crosshairStyle)
                putExtra(FpsOverlayService.EXTRA_CROSSHAIR_COLOR, profile.crosshairColor)
                putExtra(FpsOverlayService.EXTRA_DND_ENABLED, profile.dndModeEnabled)
                putExtra(FpsOverlayService.EXTRA_LOCK_BRIGHTNESS, profile.lockBrightness)
                putExtra(FpsOverlayService.EXTRA_SHOW_MINI_GRAPH, profile.showMiniGraphInOverlay)
                putExtra(FpsOverlayService.EXTRA_THERMAL_ALERT, profile.thermalHapticAlert)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }

        val (_, batteryPct, _) = thermalTracker.getBatteryTelemetry()
        val session = GameSession(
            id = UUID.randomUUID().toString(),
            packageName = packageName,
            gameName = gameName,
            startTime = System.currentTimeMillis(),
            batteryStart = batteryPct,
            batteryEnd = batteryPct
        )
        _activeSessionFlow.value = session

        return try {
            context.startActivity(launchIntent)
            true
        } catch (e: Exception) {
            _activeSessionFlow.value = null
            false
        }
    }

    fun finishActiveSession(
        avgFps: Double = 0.0,
        minFps: Int = 0,
        maxFps: Int = 0,
        onePercentLow: Int = 0,
        avgFrameTime: Double = 0.0,
        maxTemp: Double = 0.0
    ) {
        val current = _activeSessionFlow.value ?: return
        val (_, currentBattery, _) = thermalTracker.getBatteryTelemetry()

        optimizer.resetFpsUnlock(current.packageName)
        optimizer.resetAdvancedPerformanceTuning()
        optimizer.setDndMode(false)
        optimizer.setBrightnessLock(false)

        val completed = current.copy(
            endTime = System.currentTimeMillis(),
            averageFps = avgFps,
            minFps = minFps,
            maxFps = maxFps,
            onePercentLow = onePercentLow,
            averageFrameTime = avgFrameTime,
            maxTemperature = maxTemp,
            batteryEnd = currentBattery
        )

        sessionStore.saveSession(completed)
        _activeSessionFlow.value = null
    }
}
