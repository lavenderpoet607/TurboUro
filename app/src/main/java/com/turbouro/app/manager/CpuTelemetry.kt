package com.turbouro.app.manager

import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class CpuTelemetry {

    val coreCount: Int
        get() = Runtime.getRuntime().availableProcessors()

    fun getScalingCurrentFreqKHz(): Long? {
        val paths = listOf(
            "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq",
            "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_cur_freq",
            "/sys/devices/system/cpu/cpu4/cpufreq/scaling_cur_freq"
        )
        for (path in paths) {
            val file = File(path)
            if (file.exists() && file.canRead()) {
                try {
                    val line = BufferedReader(FileReader(file)).use { it.readLine() }
                    val freq = line?.trim()?.toLongOrNull()
                    if (freq != null && freq > 0) return freq
                } catch (e: Exception) {
                    continue
                }
            }
        }
        return null
    }

    fun getCpuTemperatureC(): Float? {
        val thermalPaths = listOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/devices/virtual/thermal/thermal_zone0/temp"
        )
        for (path in thermalPaths) {
            val file = File(path)
            if (file.exists() && file.canRead()) {
                try {
                    val line = BufferedReader(FileReader(file)).use { it.readLine() }
                    val raw = line?.trim()?.toFloatOrNull()
                    if (raw != null && raw > 0) {
                        return if (raw > 1000f) raw / 1000f else raw
                    }
                } catch (e: Exception) {
                    continue
                }
            }
        }
        return null
    }
}
