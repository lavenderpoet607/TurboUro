package com.turbouro.app.model

enum class ProfileType {
    BALANCED,
    PERFORMANCE,
    BATTERY_SAVER,
    CUSTOM;

    val displayName: String
        get() = when (this) {
            BALANCED -> "Seimbang"
            PERFORMANCE -> "Performa"
            BATTERY_SAVER -> "Hemat Baterai"
            CUSTOM -> "Kustom"
        }

    val description: String
        get() = when (this) {
            BALANCED -> "Performa standar dan konsumsi daya stabil"
            PERFORMANCE -> "Memprioritaskan kinerja pada game yang didukung"
            BATTERY_SAVER -> "Mengurangi beban sistem untuk menghemat daya"
            CUSTOM -> "Pengaturan yang disesuaikan pengguna"
        }
}

data class GameProfile(
    val packageName: String,
    val gameName: String,
    val profileType: ProfileType = ProfileType.BALANCED,
    val gameModeEnabled: Boolean = true,
    val performanceMode: Boolean = true,
    val fpsOverlay: Boolean = true,
    val targetFps: Int = 60,
    val autoOptimize: Boolean = true,
    val thermalProtection: Boolean = true,
    val backgroundCleanup: Boolean = true,
    val unlockFpsEnabled: Boolean = false,
    val customRefreshRate: Int = 60,
    val crosshairEnabled: Boolean = false,
    val crosshairStyle: String = "CROSS",
    val crosshairColor: String = "#00E5FF",
    val dndModeEnabled: Boolean = false,
    val lockBrightness: Boolean = false,
    val advancedGovernorTuning: Boolean = false,
    val showMiniGraphInOverlay: Boolean = true,
    val thermalHapticAlert: Boolean = true
) {
    companion object {
        fun defaultFor(packageName: String, gameName: String, type: ProfileType): GameProfile {
            return when (type) {
                ProfileType.BALANCED -> GameProfile(
                    packageName = packageName,
                    gameName = gameName,
                    profileType = ProfileType.BALANCED,
                    gameModeEnabled = true,
                    performanceMode = false,
                    fpsOverlay = true,
                    targetFps = 60,
                    autoOptimize = true,
                    thermalProtection = true,
                    backgroundCleanup = false,
                    unlockFpsEnabled = false,
                    customRefreshRate = 60,
                    crosshairEnabled = false,
                    crosshairStyle = "CROSS",
                    crosshairColor = "#00E5FF",
                    dndModeEnabled = false,
                    lockBrightness = false,
                    advancedGovernorTuning = false,
                    showMiniGraphInOverlay = true,
                    thermalHapticAlert = true
                )
                ProfileType.PERFORMANCE -> GameProfile(
                    packageName = packageName,
                    gameName = gameName,
                    profileType = ProfileType.PERFORMANCE,
                    gameModeEnabled = true,
                    performanceMode = true,
                    fpsOverlay = true,
                    targetFps = 60,
                    autoOptimize = true,
                    thermalProtection = true,
                    backgroundCleanup = true,
                    unlockFpsEnabled = true,
                    customRefreshRate = 120,
                    crosshairEnabled = false,
                    crosshairStyle = "CROSS",
                    crosshairColor = "#00E5FF",
                    dndModeEnabled = true,
                    lockBrightness = true,
                    advancedGovernorTuning = true,
                    showMiniGraphInOverlay = true,
                    thermalHapticAlert = true
                )
                ProfileType.BATTERY_SAVER -> GameProfile(
                    packageName = packageName,
                    gameName = gameName,
                    profileType = ProfileType.BATTERY_SAVER,
                    gameModeEnabled = true,
                    performanceMode = false,
                    fpsOverlay = false,
                    targetFps = 30,
                    autoOptimize = false,
                    thermalProtection = true,
                    backgroundCleanup = false,
                    unlockFpsEnabled = false,
                    customRefreshRate = 60,
                    crosshairEnabled = false,
                    crosshairStyle = "CROSS",
                    crosshairColor = "#00E5FF",
                    dndModeEnabled = false,
                    lockBrightness = false,
                    advancedGovernorTuning = false,
                    showMiniGraphInOverlay = false,
                    thermalHapticAlert = true
                )
                ProfileType.CUSTOM -> GameProfile(
                    packageName = packageName,
                    gameName = gameName,
                    profileType = ProfileType.CUSTOM,
                    gameModeEnabled = true,
                    performanceMode = true,
                    fpsOverlay = true,
                    targetFps = 60,
                    autoOptimize = true,
                    thermalProtection = true,
                    backgroundCleanup = true,
                    unlockFpsEnabled = false,
                    customRefreshRate = 90,
                    crosshairEnabled = false,
                    crosshairStyle = "CROSS",
                    crosshairColor = "#00E5FF",
                    dndModeEnabled = false,
                    lockBrightness = false,
                    advancedGovernorTuning = false,
                    showMiniGraphInOverlay = true,
                    thermalHapticAlert = true
                )
            }
        }
    }
}
