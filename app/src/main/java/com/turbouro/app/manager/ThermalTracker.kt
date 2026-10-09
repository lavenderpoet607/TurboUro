package com.turbouro.app.manager

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import com.turbouro.app.model.ThermalStatus

class ThermalTracker(private val context: Context) {

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    private var thermalListener: PowerManager.OnThermalStatusChangedListener? = null
    private var currentThermalStatus: ThermalStatus = ThermalStatus.NORMAL

    fun startListening(onStatusChanged: (ThermalStatus) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null) {
            val listener = PowerManager.OnThermalStatusChangedListener { status ->
                val mapped = when (status) {
                    PowerManager.THERMAL_STATUS_NONE -> ThermalStatus.NORMAL
                    PowerManager.THERMAL_STATUS_LIGHT -> ThermalStatus.WARM
                    PowerManager.THERMAL_STATUS_MODERATE -> ThermalStatus.WARM
                    PowerManager.THERMAL_STATUS_SEVERE -> ThermalStatus.HOT
                    PowerManager.THERMAL_STATUS_CRITICAL,
                    PowerManager.THERMAL_STATUS_EMERGENCY,
                    PowerManager.THERMAL_STATUS_SHUTDOWN -> ThermalStatus.CRITICAL
                    else -> ThermalStatus.NORMAL
                }
                currentThermalStatus = mapped
                onStatusChanged(mapped)
            }
            thermalListener = listener
            powerManager.addThermalStatusListener(listener)
        }
    }

    fun stopListening() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null) {
            thermalListener?.let { powerManager.removeThermalStatusListener(it) }
            thermalListener = null
        }
    }

    fun getBatteryTelemetry(): Triple<Float?, Int, Boolean> {
        return try {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, intentFilter)
            val tempRaw = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
            val tempC = if (tempRaw > 0) tempRaw / 10.0f else null

            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = if (level >= 0 && scale > 0) (level * 100) / scale else 100

            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            Triple(tempC, batteryPct, isCharging)
        } catch (e: Exception) {
            Triple(null, 100, false)
        }
    }

    fun getCurrentThermalStatus(batteryTempC: Float?): ThermalStatus {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null) {
            val status = powerManager.currentThermalStatus
            return when (status) {
                PowerManager.THERMAL_STATUS_NONE -> ThermalStatus.NORMAL
                PowerManager.THERMAL_STATUS_LIGHT -> ThermalStatus.WARM
                PowerManager.THERMAL_STATUS_MODERATE -> ThermalStatus.WARM
                PowerManager.THERMAL_STATUS_SEVERE -> ThermalStatus.HOT
                PowerManager.THERMAL_STATUS_CRITICAL,
                PowerManager.THERMAL_STATUS_EMERGENCY,
                PowerManager.THERMAL_STATUS_SHUTDOWN -> ThermalStatus.CRITICAL
                else -> ThermalStatus.NORMAL
            }
        }

        return if (batteryTempC != null) {
            when {
                batteryTempC >= 46f -> ThermalStatus.CRITICAL
                batteryTempC >= 42f -> ThermalStatus.HOT
                batteryTempC >= 38f -> ThermalStatus.WARM
                batteryTempC <= 20f -> ThermalStatus.COOL
                else -> ThermalStatus.NORMAL
            }
        } else {
            ThermalStatus.NORMAL
        }
    }
}
