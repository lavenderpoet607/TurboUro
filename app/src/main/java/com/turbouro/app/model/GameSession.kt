package com.turbouro.app.model

data class GameSession(
    val id: String,
    val packageName: String,
    val gameName: String,
    val startTime: Long,
    val endTime: Long? = null,
    val averageFps: Double = 0.0,
    val minFps: Int = 0,
    val maxFps: Int = 0,
    val onePercentLow: Int = 0,
    val zeroPointOnePercentLow: Int = 0,
    val averageFrameTime: Double = 0.0,
    val maxTemperature: Double = 0.0,
    val batteryStart: Int = 100,
    val batteryEnd: Int = 100
) {
    val durationMillis: Long
        get() = (endTime ?: System.currentTimeMillis()) - startTime

    val durationMinutes: Long
        get() = (durationMillis / 60000L).coerceAtLeast(1L)

    val formattedDuration: String
        get() = "${durationMinutes} min"
}
