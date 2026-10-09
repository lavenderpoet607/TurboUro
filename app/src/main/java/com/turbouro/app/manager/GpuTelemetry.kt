package com.turbouro.app.manager

import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class GpuTelemetry {

    fun getGpuCurrentFreqKHz(): Long? {
        val paths = listOf(
            "/sys/class/kgsl/kgsl-3d0/gpuclk",
            "/sys/class/kgsl/kgsl-3d0/devfreq/cur_freq",
            "/sys/devices/platform/mali-utgard.0/gpu_clk",
            "/sys/devices/platform/13040000.mali/devfreq/13040000.mali/cur_freq",
            "/sys/class/devfreq/gpufreq/cur_freq"
        )
        for (path in paths) {
            val file = File(path)
            if (file.exists() && file.canRead()) {
                try {
                    val line = BufferedReader(FileReader(file)).use { it.readLine() }
                    val raw = line?.trim()?.toLongOrNull()
                    if (raw != null && raw > 0) {
                        return if (raw > 10_000_000L) raw / 1000L else raw
                    }
                } catch (e: Exception) {
                    continue
                }
            }
        }
        return null
    }
}
