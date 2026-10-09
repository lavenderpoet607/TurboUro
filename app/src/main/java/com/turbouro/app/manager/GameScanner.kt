package com.turbouro.app.manager

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import com.turbouro.app.storage.GameProfileStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class GameItem(
    val packageName: String,
    val name: String,
    val icon: Drawable?,
    val isAutoDetectedGame: Boolean,
    val isManual: Boolean
)

class GameScanner(
    private val context: Context,
    private val profileStore: GameProfileStore
) {
    private val packageManager: PackageManager = context.packageManager

    suspend fun scanGames(): List<GameItem> = withContext(Dispatchers.IO) {
        val manualPackages = profileStore.getTrackedGamePackages()
        val results = mutableListOf<GameItem>()

        try {
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

            for (app in installedApps) {
                if (app.packageName == context.packageName) continue

                val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val isManual = manualPackages.contains(app.packageName)
                val isAutoGame = isGameApplication(app)

                if (isManual || (!isSystem && isAutoGame)) {
                    val label = packageManager.getApplicationLabel(app).toString()
                    val icon = try {
                        packageManager.getApplicationIcon(app)
                    } catch (e: Exception) {
                        null
                    }

                    results.add(
                        GameItem(
                            packageName = app.packageName,
                            name = label,
                            icon = icon,
                            isAutoDetectedGame = isAutoGame,
                            isManual = isManual
                        )
                    )
                }
            }
        } catch (e: Exception) {
        }

        results.sortedBy { it.name.lowercase() }
    }

    suspend fun getAllInstalledApps(): List<GameItem> = withContext(Dispatchers.IO) {
        val results = mutableListOf<GameItem>()
        try {
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            for (app in installedApps) {
                if (app.packageName == context.packageName) continue
                val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                if (!isSystem) {
                    val label = packageManager.getApplicationLabel(app).toString()
                    val icon = try {
                        packageManager.getApplicationIcon(app)
                    } catch (e: Exception) {
                        null
                    }
                    results.add(
                        GameItem(
                            packageName = app.packageName,
                            name = label,
                            icon = icon,
                            isAutoDetectedGame = isGameApplication(app),
                            isManual = profileStore.getTrackedGamePackages().contains(app.packageName)
                        )
                    )
                }
            }
        } catch (e: Exception) {
        }
        results.sortedBy { it.name.lowercase() }
    }

    private fun isGameApplication(appInfo: ApplicationInfo): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (appInfo.category == ApplicationInfo.CATEGORY_GAME) {
                return true
            }
        }
        @Suppress("DEPRECATION")
        if ((appInfo.flags and ApplicationInfo.FLAG_IS_GAME) == ApplicationInfo.FLAG_IS_GAME) {
            return true
        }
        return false
    }

    fun addManualGame(packageName: String) {
        profileStore.addTrackedGame(packageName)
    }

    fun removeGame(packageName: String) {
        profileStore.removeTrackedGame(packageName)
    }
}
