package com.turbouro.app.manager

import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build

class PackageMonitor(private val context: Context) {

    fun getForegroundPackageName(): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) return null
        return try {
            val usageStatsManager =
                context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                    ?: return null

            val endTime = System.currentTimeMillis()
            val beginTime = endTime - 10000L

            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                beginTime,
                endTime
            )

            if (stats.isNullOrEmpty()) return null

            var recentPackage: String? = null
            var lastUsedTime = 0L

            for (stat in stats) {
                if (stat.lastTimeUsed > lastUsedTime) {
                    lastUsedTime = stat.lastTimeUsed
                    recentPackage = stat.packageName
                }
            }

            recentPackage
        } catch (e: Exception) {
            null
        }
    }
}
