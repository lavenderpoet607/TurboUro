package com.turbouro.app.model

enum class OverlayMode {
    COMPACT,
    STANDARD,
    FULL;

    val displayName: String
        get() = when (this) {
            COMPACT -> "Ringkas"
            STANDARD -> "Standar"
            FULL -> "Lengkap"
        }
}

data class OverlaySettings(
    val enabled: Boolean = false,
    val mode: OverlayMode = OverlayMode.STANDARD,
    val showFps: Boolean = true,
    val showFrameTime: Boolean = true,
    val showTemperature: Boolean = true,
    val showBattery: Boolean = true,
    val showCpu: Boolean = false,
    val showGpu: Boolean = false,
    val showRefreshRate: Boolean = false,
    val opacityPercent: Int = 85,
    val textSizeSp: Int = 13,
    val updateIntervalMillis: Long = 1000L,
    val isLocked: Boolean = false,
    val posX: Int = 40,
    val posY: Int = 100
)
