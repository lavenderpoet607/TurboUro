package com.turbouro.app.storage

import android.content.Context
import android.content.SharedPreferences
import com.turbouro.app.model.GameProfile
import com.turbouro.app.model.ProfileType
import org.json.JSONObject

class GameProfileStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("turbouro_game_profiles", Context.MODE_PRIVATE)

    fun getProfile(packageName: String, gameName: String): GameProfile {
        val raw = prefs.getString("profile_$packageName", null)
        if (raw != null) {
            try {
                val json = JSONObject(raw)
                val typeOrdinal = json.optInt("profileType", ProfileType.BALANCED.ordinal)
                val profileType = ProfileType.entries.getOrElse(typeOrdinal) { ProfileType.BALANCED }

                return GameProfile(
                    packageName = packageName,
                    gameName = json.optString("gameName", gameName),
                    profileType = profileType,
                    gameModeEnabled = json.optBoolean("gameModeEnabled", true),
                    performanceMode = json.optBoolean("performanceMode", true),
                    fpsOverlay = json.optBoolean("fpsOverlay", true),
                    targetFps = json.optInt("targetFps", 60),
                    autoOptimize = json.optBoolean("autoOptimize", true),
                    thermalProtection = json.optBoolean("thermalProtection", true),
                    backgroundCleanup = json.optBoolean("backgroundCleanup", true),
                    unlockFpsEnabled = json.optBoolean("unlockFpsEnabled", false),
                    customRefreshRate = json.optInt("customRefreshRate", 60),
                    crosshairEnabled = json.optBoolean("crosshairEnabled", false),
                    crosshairStyle = json.optString("crosshairStyle", "CROSS"),
                    crosshairColor = json.optString("crosshairColor", "#00E5FF"),
                    dndModeEnabled = json.optBoolean("dndModeEnabled", false),
                    lockBrightness = json.optBoolean("lockBrightness", false),
                    advancedGovernorTuning = json.optBoolean("advancedGovernorTuning", false),
                    showMiniGraphInOverlay = json.optBoolean("showMiniGraphInOverlay", true),
                    thermalHapticAlert = json.optBoolean("thermalHapticAlert", true)
                )
            } catch (e: Exception) {
            }
        }
        return GameProfile.defaultFor(packageName, gameName, ProfileType.BALANCED)
    }

    fun saveProfile(profile: GameProfile) {
        val json = JSONObject().apply {
            put("packageName", profile.packageName)
            put("gameName", profile.gameName)
            put("profileType", profile.profileType.ordinal)
            put("gameModeEnabled", profile.gameModeEnabled)
            put("performanceMode", profile.performanceMode)
            put("fpsOverlay", profile.fpsOverlay)
            put("targetFps", profile.targetFps)
            put("autoOptimize", profile.autoOptimize)
            put("thermalProtection", profile.thermalProtection)
            put("backgroundCleanup", profile.backgroundCleanup)
            put("unlockFpsEnabled", profile.unlockFpsEnabled)
            put("customRefreshRate", profile.customRefreshRate)
            put("crosshairEnabled", profile.crosshairEnabled)
            put("crosshairStyle", profile.crosshairStyle)
            put("crosshairColor", profile.crosshairColor)
            put("dndModeEnabled", profile.dndModeEnabled)
            put("lockBrightness", profile.lockBrightness)
            put("advancedGovernorTuning", profile.advancedGovernorTuning)
            put("showMiniGraphInOverlay", profile.showMiniGraphInOverlay)
            put("thermalHapticAlert", profile.thermalHapticAlert)
        }
        prefs.edit().putString("profile_${profile.packageName}", json.toString()).apply()
    }

    fun getTrackedGamePackages(): Set<String> {
        val set = prefs.getStringSet("tracked_packages", null)
        return set ?: emptySet()
    }

    fun addTrackedGame(packageName: String) {
        val current = getTrackedGamePackages().toMutableSet()
        current.add(packageName)
        prefs.edit().putStringSet("tracked_packages", current).apply()
    }

    fun removeTrackedGame(packageName: String) {
        val current = getTrackedGamePackages().toMutableSet()
        current.remove(packageName)
        prefs.edit()
            .putStringSet("tracked_packages", current)
            .remove("profile_$packageName")
            .apply()
    }
}
