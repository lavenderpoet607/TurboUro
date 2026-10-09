package com.turbouro.app.storage

import android.content.Context
import com.turbouro.app.model.GameSession
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class SessionHistoryStore(context: Context) {

    private val sessionFile = File(context.filesDir, "turbouro_sessions.json")

    @Synchronized
    fun getAllSessions(): List<GameSession> {
        if (!sessionFile.exists()) return emptyList()
        return try {
            val content = sessionFile.readText()
            val jsonArray = JSONArray(content)
            val list = mutableListOf<GameSession>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    GameSession(
                        id = obj.getString("id"),
                        packageName = obj.getString("packageName"),
                        gameName = obj.getString("gameName"),
                        startTime = obj.getLong("startTime"),
                        endTime = if (obj.has("endTime") && !obj.isNull("endTime")) obj.getLong("endTime") else null,
                        averageFps = obj.optDouble("averageFps", 0.0),
                        minFps = obj.optInt("minFps", 0),
                        maxFps = obj.optInt("maxFps", 0),
                        onePercentLow = obj.optInt("onePercentLow", 0),
                        zeroPointOnePercentLow = obj.optInt("zeroPointOnePercentLow", 0),
                        averageFrameTime = obj.optDouble("averageFrameTime", 0.0),
                        maxTemperature = obj.optDouble("maxTemperature", 0.0),
                        batteryStart = obj.optInt("batteryStart", 100),
                        batteryEnd = obj.optInt("batteryEnd", 100)
                    )
                )
            }
            list.sortedByDescending { it.startTime }
        } catch (e: Exception) {
            emptyList()
        }
    }

    @Synchronized
    fun saveSession(session: GameSession) {
        val current = getAllSessions().toMutableList()
        current.removeAll { it.id == session.id }
        current.add(0, session)
        persist(current)
    }

    @Synchronized
    fun getSessionById(id: String): GameSession? {
        return getAllSessions().firstOrNull { it.id == id }
    }

    @Synchronized
    fun deleteSession(id: String) {
        val current = getAllSessions().toMutableList()
        val changed = current.removeAll { it.id == id }
        if (changed) {
            persist(current)
        }
    }

    @Synchronized
    fun clearAllSessions() {
        if (sessionFile.exists()) {
            sessionFile.delete()
        }
    }

    private fun persist(sessions: List<GameSession>) {
        try {
            val jsonArray = JSONArray()
            for (s in sessions) {
                val obj = JSONObject().apply {
                    put("id", s.id)
                    put("packageName", s.packageName)
                    put("gameName", s.gameName)
                    put("startTime", s.startTime)
                    put("endTime", s.endTime)
                    put("averageFps", s.averageFps)
                    put("minFps", s.minFps)
                    put("maxFps", s.maxFps)
                    put("onePercentLow", s.onePercentLow)
                    put("zeroPointOnePercentLow", s.zeroPointOnePercentLow)
                    put("averageFrameTime", s.averageFrameTime)
                    put("maxTemperature", s.maxTemperature)
                    put("batteryStart", s.batteryStart)
                    put("batteryEnd", s.batteryEnd)
                }
                jsonArray.put(obj)
            }
            sessionFile.writeText(jsonArray.toString())
        } catch (e: Exception) {
        }
    }
}
