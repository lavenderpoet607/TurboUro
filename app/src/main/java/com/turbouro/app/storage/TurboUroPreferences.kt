package com.turbouro.app.storage

import android.content.Context
import android.content.SharedPreferences
import com.turbouro.app.model.AppSettings
import com.turbouro.app.model.AppTheme
import com.turbouro.app.model.OverlayMode
import com.turbouro.app.model.OverlaySettings
import com.turbouro.app.model.ProfileType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TurboUroPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("turbouro_settings", Context.MODE_PRIVATE)

    private val _appSettingsFlow = MutableStateFlow(loadAppSettings())
    val appSettingsFlow: StateFlow<AppSettings> = _appSettingsFlow.asStateFlow()

    private val _overlaySettingsFlow = MutableStateFlow(loadOverlaySettings())
    val overlaySettingsFlow: StateFlow<OverlaySettings> = _overlaySettingsFlow.asStateFlow()

    fun loadAppSettings(): AppSettings {
        val themeOrdinal = prefs.getInt("app_theme", AppTheme.DARK.ordinal)
        val theme = AppTheme.entries.getOrElse(themeOrdinal) { AppTheme.DARK }

        val startOnBoot = prefs.getBoolean("start_on_boot", false)
        val autoDetectGame = prefs.getBoolean("auto_detect_game", true)

        val profileOrdinal = prefs.getInt("default_profile", ProfileType.BALANCED.ordinal)
        val defaultProfile = ProfileType.entries.getOrElse(profileOrdinal) { ProfileType.BALANCED }

        val thermalProtection = prefs.getBoolean("thermal_protection", true)
        val backgroundCleanup = prefs.getBoolean("background_cleanup", true)
        val saveSessionHistory = prefs.getBoolean("save_session_history", true)
        val isOnboardingCompleted = prefs.getBoolean("onboarding_completed", false)

        return AppSettings(
            theme = theme,
            startOnBoot = startOnBoot,
            autoDetectGame = autoDetectGame,
            defaultProfile = defaultProfile,
            thermalProtection = thermalProtection,
            backgroundCleanup = backgroundCleanup,
            saveSessionHistory = saveSessionHistory,
            isOnboardingCompleted = isOnboardingCompleted
        )
    }

    fun updateAppSettings(settings: AppSettings) {
        prefs.edit()
            .putInt("app_theme", settings.theme.ordinal)
            .putBoolean("start_on_boot", settings.startOnBoot)
            .putBoolean("auto_detect_game", settings.autoDetectGame)
            .putInt("default_profile", settings.defaultProfile.ordinal)
            .putBoolean("thermal_protection", settings.thermalProtection)
            .putBoolean("background_cleanup", settings.backgroundCleanup)
            .putBoolean("save_session_history", settings.saveSessionHistory)
            .putBoolean("onboarding_completed", settings.isOnboardingCompleted)
            .apply()

        _appSettingsFlow.value = settings
    }

    fun loadOverlaySettings(): OverlaySettings {
        val enabled = prefs.getBoolean("overlay_enabled", false)
        val modeOrdinal = prefs.getInt("overlay_mode", OverlayMode.STANDARD.ordinal)
        val mode = OverlayMode.entries.getOrElse(modeOrdinal) { OverlayMode.STANDARD }

        val showFps = prefs.getBoolean("overlay_show_fps", true)
        val showFrameTime = prefs.getBoolean("overlay_show_frametime", true)
        val showTemp = prefs.getBoolean("overlay_show_temp", true)
        val showBattery = prefs.getBoolean("overlay_show_battery", true)
        val showCpu = prefs.getBoolean("overlay_show_cpu", false)
        val showGpu = prefs.getBoolean("overlay_show_gpu", false)
        val showHz = prefs.getBoolean("overlay_show_hz", false)

        val opacity = prefs.getInt("overlay_opacity", 85)
        val textSize = prefs.getInt("overlay_text_size", 13)
        val interval = prefs.getLong("overlay_interval", 1000L)
        val isLocked = prefs.getBoolean("overlay_locked", false)
        val posX = prefs.getInt("overlay_pos_x", 40)
        val posY = prefs.getInt("overlay_pos_y", 100)

        return OverlaySettings(
            enabled = enabled,
            mode = mode,
            showFps = showFps,
            showFrameTime = showFrameTime,
            showTemperature = showTemp,
            showBattery = showBattery,
            showCpu = showCpu,
            showGpu = showGpu,
            showRefreshRate = showHz,
            opacityPercent = opacity,
            textSizeSp = textSize,
            updateIntervalMillis = interval,
            isLocked = isLocked,
            posX = posX,
            posY = posY
        )
    }

    fun updateOverlaySettings(settings: OverlaySettings) {
        prefs.edit()
            .putBoolean("overlay_enabled", settings.enabled)
            .putInt("overlay_mode", settings.mode.ordinal)
            .putBoolean("overlay_show_fps", settings.showFps)
            .putBoolean("overlay_show_frametime", settings.showFrameTime)
            .putBoolean("overlay_show_temp", settings.showTemperature)
            .putBoolean("overlay_show_battery", settings.showBattery)
            .putBoolean("overlay_show_cpu", settings.showCpu)
            .putBoolean("overlay_show_gpu", settings.showGpu)
            .putBoolean("overlay_show_hz", settings.showRefreshRate)
            .putInt("overlay_opacity", settings.opacityPercent)
            .putInt("overlay_text_size", settings.textSizeSp)
            .putLong("overlay_interval", settings.updateIntervalMillis)
            .putBoolean("overlay_locked", settings.isLocked)
            .putInt("overlay_pos_x", settings.posX)
            .putInt("overlay_pos_y", settings.posY)
            .apply()

        _overlaySettingsFlow.value = settings
    }
}
