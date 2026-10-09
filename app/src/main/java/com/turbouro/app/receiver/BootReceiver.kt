package com.turbouro.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.turbouro.app.service.FpsOverlayService
import com.turbouro.app.storage.TurboUroPreferences

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val prefs = TurboUroPreferences(context)
            val appSettings = prefs.loadAppSettings()
            val overlaySettings = prefs.loadOverlaySettings()

            if (appSettings.startOnBoot && overlaySettings.enabled) {
                if (Settings.canDrawOverlays(context)) {
                    val serviceIntent = Intent(context, FpsOverlayService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try {
                            context.startForegroundService(serviceIntent)
                        } catch (e: Exception) {
                        }
                    } else {
                        context.startService(serviceIntent)
                    }
                }
            }
        }
    }
}
