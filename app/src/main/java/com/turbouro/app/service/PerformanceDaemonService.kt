package com.turbouro.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.turbouro.app.R
import com.turbouro.app.manager.PackageMonitor
import com.turbouro.app.storage.GameProfileStore
import com.turbouro.app.storage.TurboUroPreferences

class PerformanceDaemonService : Service() {

    companion object {
        const val CHANNEL_ID = "TurboUro_Daemon_Channel"
        const val NOTIFICATION_ID = 1002
    }

    private lateinit var packageMonitor: PackageMonitor
    private lateinit var profileStore: GameProfileStore
    private lateinit var prefs: TurboUroPreferences
    private val handler = Handler(Looper.getMainLooper())
    private var monitorRunnable: Runnable? = null
    private var lastForegroundPackage: String? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        packageMonitor = PackageMonitor(this)
        profileStore = GameProfileStore(this)
        prefs = TurboUroPreferences(this)

        startForegroundDaemon()
        startMonitoringLoop()
    }

    private fun startForegroundDaemon() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "TurboUro Monitor Latar Belakang",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TurboUro Pengawas Game")
            .setContentText("Mendeteksi aktivitas game secara otomatis")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startMonitoringLoop() {
        val runnable = object : Runnable {
            override fun run() {
                val appSettings = prefs.loadAppSettings()
                if (appSettings.autoDetectGame) {
                    val currentForeground = packageMonitor.getForegroundPackageName()
                    if (currentForeground != null && currentForeground != lastForegroundPackage) {
                        lastForegroundPackage = currentForeground
                    }
                }
                handler.postDelayed(this, 3000L)
            }
        }
        monitorRunnable = runnable
        handler.post(runnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        monitorRunnable?.let { handler.removeCallbacks(it) }
    }
}
