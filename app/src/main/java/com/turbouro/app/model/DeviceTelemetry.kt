package com.turbouro.app.model

enum class ThermalStatus {
    COOL,
    NORMAL,
    WARM,
    HOT,
    CRITICAL;

    val displayText: String
        get() = when (this) {
            COOL -> "Dingin"
            NORMAL -> "Normal"
            WARM -> "Hangat"
            HOT -> "Panas"
            CRITICAL -> "Kritis"
        }
}

data class DeviceTelemetry(
    val cpuFreqKHz: Long? = null,
    val cpuCores: Int = 1,
    val cpuUsagePercent: Float? = null,
    val gpuFreqKHz: Long? = null,
    val cpuTempC: Float? = null,
    val batteryTempC: Float? = null,
    val thermalStatus: ThermalStatus = ThermalStatus.NORMAL,
    val refreshRateHz: Float = 60f,
    val batteryPercent: Int = 100,
    val isCharging: Boolean = false,
    val fps: Int? = null,
    val averageFps: Double? = null,
    val minFps: Int? = null,
    val maxFps: Int? = null,
    val onePercentLow: Int? = null,
    val zeroPointOnePercentLow: Int? = null,
    val frameTimeMs: Double? = null,
    val frameDropCount: Int = 0,
    val isGameFpsAvailable: Boolean = false,
    val fpsSourceDescription: String = "N/A"
) {
    val displayFps: String
        get() = fps?.toString() ?: "N/A"

    val displayFrameTime: String
        get() = frameTimeMs?.let { String.format("%.1f ms", it) } ?: "N/A"

    val displayTemp: String
        get() = batteryTempC?.let { String.format("%.1f°C", it) } ?: "N/A"

    val displayCpuFreq: String
        get() = cpuFreqKHz?.let {
            if (it > 1_000_000) String.format("%.2f GHz", it / 1_000_000.0)
            else String.format("%d MHz", it / 1_000)
        } ?: "N/A"

    val displayGpuFreq: String
        get() = gpuFreqKHz?.let {
            if (it > 1_000_000) String.format("%.2f GHz", it / 1_000_000.0)
            else String.format("%d MHz", it / 1_000)
        } ?: "N/A"
}
