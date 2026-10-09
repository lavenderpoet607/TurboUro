package com.turbouro.app.manager

import android.view.Choreographer
import com.turbouro.app.integration.ShizukuManager
import java.util.LinkedList

data class FpsMetrics(
    val currentFps: Int?,
    val averageFps: Double?,
    val minFps: Int?,
    val maxFps: Int?,
    val onePercentLow: Int?,
    val zeroPointOnePercentLow: Int?,
    val frameTimeMs: Double?,
    val frameDropCount: Int,
    val isGameFps: Boolean,
    val sourceDescription: String
)

class FpsCalculator(
    private val onMetricsUpdated: (FpsMetrics) -> Unit
) : Choreographer.FrameCallback {

    private var isRunning = false
    private var frameCount = 0
    private var lastFpsTimestampNanos = 0L
    private var previousFrameTimestampNanos = 0L
    private val frameDurationsMs = LinkedList<Double>()
    private val fpsHistory = LinkedList<Int>()
    private var totalFrameDrops = 0
    private var targetPackage: String? = null
    private var cachedLayerName: String? = null
    private var lastLayerScanTime = 0L

    fun start(targetGamePackage: String? = null) {
        if (isRunning) return
        isRunning = true
        targetPackage = targetGamePackage
        frameCount = 0
        totalFrameDrops = 0
        cachedLayerName = null
        lastLayerScanTime = 0L
        frameDurationsMs.clear()
        fpsHistory.clear()
        val now = System.nanoTime()
        lastFpsTimestampNanos = now
        previousFrameTimestampNanos = now

        Choreographer.getInstance().postFrameCallback(this)
    }

    fun stop() {
        isRunning = false
        Choreographer.getInstance().removeFrameCallback(this)
        frameDurationsMs.clear()
        fpsHistory.clear()
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!isRunning) return

        val prevFrameTime = previousFrameTimestampNanos
        previousFrameTimestampNanos = frameTimeNanos

        if (prevFrameTime > 0L) {
            val frameIntervalNanos = frameTimeNanos - prevFrameTime
            val frameIntervalMs = frameIntervalNanos / 1_000_000.0
            if (frameIntervalMs in 1.0..250.0) {
                frameDurationsMs.add(frameIntervalMs)
                if (frameDurationsMs.size > 120) {
                    frameDurationsMs.removeFirst()
                }
                if (frameIntervalMs > 28.0) {
                    totalFrameDrops++
                }
            }
        }

        frameCount++
        val elapsedNanos = frameTimeNanos - lastFpsTimestampNanos

        if (elapsedNanos >= 1_000_000_000L) {
            val measuredFps = (frameCount * 1_000_000_000.0 / elapsedNanos).toInt().coerceIn(1, 240)
            fpsHistory.add(measuredFps)
            if (fpsHistory.size > 60) {
                fpsHistory.removeFirst()
            }

            val currentTarget = targetPackage
            var emitted = false

            if (!currentTarget.isNullOrBlank() && ShizukuManager.hasPermission()) {
                val gameFps = tryReadGameFps(currentTarget)
                if (gameFps != null && gameFps > 0) {
                    emitMetrics(gameFps, isGameFps = true, source = "SurfaceFlinger (Shizuku)")
                    emitted = true
                }
            }

            if (!emitted) {
                val activeFps = if (measuredFps > 0) measuredFps else 60
                val sourceDesc = if (!currentTarget.isNullOrBlank()) {
                    "Vsync Pipeline (Mode Standar)"
                } else {
                    "TurboUro Frame Engine"
                }
                emitMetrics(activeFps, isGameFps = false, source = sourceDesc)
            }

            frameCount = 0
            lastFpsTimestampNanos = frameTimeNanos
        }

        Choreographer.getInstance().postFrameCallback(this)
    }

    private fun tryReadGameFps(packageName: String): Int? {
        val now = System.currentTimeMillis()
        if (cachedLayerName == null || now - lastLayerScanTime > 15000L) {
            cachedLayerName = findLayerName(packageName)
            lastLayerScanTime = now
        }

        val layer = cachedLayerName
        if (layer != null) {
            val fpsFromLayer = readFpsFromLayer(layer)
            if (fpsFromLayer != null && fpsFromLayer > 0) {
                return fpsFromLayer
            }
        }

        val directFps = readFpsFromLayer(packageName)
        if (directFps != null && directFps > 0) {
            return directFps
        }

        return readFpsFromGfxInfo(packageName)
    }

    private fun findLayerName(packageName: String): String? {
        val listOutput = ShizukuManager.executeShell("dumpsys SurfaceFlinger --list") ?: return null
        val lines = listOutput.lines()
        val matchingLayers = lines.filter { it.contains(packageName, ignoreCase = true) }
        val preferred = matchingLayers.firstOrNull { it.contains("SurfaceView", ignoreCase = true) }
            ?: matchingLayers.firstOrNull { !it.contains("Background", ignoreCase = true) && !it.contains("Snapshot", ignoreCase = true) }
            ?: matchingLayers.firstOrNull()
        return preferred?.trim()
    }

    private fun readFpsFromLayer(layerName: String): Int? {
        val output = ShizukuManager.executeShell("dumpsys SurfaceFlinger --latency \"$layerName\"") ?: return null
        val lines = output.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.size < 3) return null

        val refreshPeriodNanos = lines.firstOrNull()?.toLongOrNull() ?: return null
        if (refreshPeriodNanos <= 0L) return null

        var validFrames = 0
        var minTime = Long.MAX_VALUE
        var maxTime = Long.MIN_VALUE

        for (i in 1 until lines.size) {
            val parts = lines[i].split(Regex("\\s+"))
            if (parts.size >= 3) {
                val presentTime = parts[1].toLongOrNull() ?: 0L
                if (presentTime > 0L && presentTime != Long.MAX_VALUE) {
                    validFrames++
                    if (presentTime < minTime) minTime = presentTime
                    if (presentTime > maxTime) maxTime = presentTime
                }
            }
        }

        if (validFrames >= 2 && maxTime > minTime) {
            val diffNanos = maxTime - minTime
            if (diffNanos in 100_000_000L..3_000_000_000L) {
                val calculated = ((validFrames - 1) * 1_000_000_000.0 / diffNanos).toInt()
                return calculated.coerceIn(1, 165)
            }
        }

        if (validFrames > 0) {
            val calculated = (validFrames * (1_000_000_000.0 / (refreshPeriodNanos * 127.0))).toInt()
            if (calculated in 1..165) return calculated
        }

        return null
    }

    private fun readFpsFromGfxInfo(packageName: String): Int? {
        val output = ShizukuManager.executeShell("dumpsys gfxinfo $packageName framestats") ?: return null
        val lines = output.lines()
        var profileDataStarted = false
        var count = 0

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed == "---PROFILEDATA---") {
                profileDataStarted = true
                continue
            }
            if (profileDataStarted) {
                if (trimmed.startsWith("Flags")) continue
                val parts = trimmed.split(",")
                if (parts.size >= 13) {
                    val flags = parts[0].toLongOrNull() ?: 0L
                    if (flags == 0L) {
                        count++
                    }
                }
            }
        }

        return if (count in 1..165) count else null
    }

    private fun emitMetrics(currentFps: Int, isGameFps: Boolean, source: String) {
        val avgFps = if (fpsHistory.isNotEmpty()) {
            fpsHistory.average()
        } else currentFps.toDouble()

        val minFps = if (fpsHistory.isNotEmpty()) {
            fpsHistory.minOrNull() ?: currentFps
        } else currentFps

        val maxFps = if (fpsHistory.isNotEmpty()) {
            fpsHistory.maxOrNull() ?: currentFps
        } else currentFps

        val onePercentLow = if (fpsHistory.size >= 10) {
            val sorted = fpsHistory.sorted()
            val index = (sorted.size * 0.01).toInt().coerceIn(0, sorted.size - 1)
            sorted[index]
        } else minFps

        val zeroPointOnePercentLow = if (fpsHistory.size >= 20) {
            val sorted = fpsHistory.sorted()
            val index = (sorted.size * 0.001).toInt().coerceIn(0, sorted.size - 1)
            sorted[index]
        } else onePercentLow

        val avgFrameTime = if (frameDurationsMs.isNotEmpty()) {
            frameDurationsMs.average()
        } else {
            1000.0 / currentFps.coerceAtLeast(1)
        }

        val metrics = FpsMetrics(
            currentFps = currentFps,
            averageFps = avgFps,
            minFps = minFps,
            maxFps = maxFps,
            onePercentLow = onePercentLow,
            zeroPointOnePercentLow = zeroPointOnePercentLow,
            frameTimeMs = avgFrameTime,
            frameDropCount = totalFrameDrops,
            isGameFps = isGameFps,
            sourceDescription = source
        )

        onMetricsUpdated(metrics)
    }
}
