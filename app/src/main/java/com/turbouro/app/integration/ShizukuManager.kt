package com.turbouro.app.integration

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.turbouro.app.model.CapabilityState
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

object ShizukuManager {

    const val SHIZUKU_PACKAGE_NAME = "moe.shizuku.privileged.api"
    const val REQUEST_CODE_SHIZUKU_PERMISSION = 9001

    fun isAppInstalled(context: Context): Boolean {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    SHIZUKU_PACKAGE_NAME,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(SHIZUKU_PACKAGE_NAME, 0)
            }
            true
        } catch (e: Throwable) {
            false
        }
    }

    fun isServiceRunning(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Throwable) {
            false
        }
    }

    fun isInstalled(context: Context? = null): Boolean {
        if (isServiceRunning()) return true
        if (context != null) {
            return isAppInstalled(context)
        }
        return false
    }

    fun isInstalled(): Boolean {
        return isServiceRunning()
    }

    fun hasPermission(): Boolean {
        return try {
            if (isServiceRunning()) {
                if (!Shizuku.isPreV11()) {
                    Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Throwable) {
            false
        }
    }

    fun getShizukuState(): CapabilityState {
        return try {
            if (hasPermission()) {
                CapabilityState.AVAILABLE
            } else if (isServiceRunning()) {
                CapabilityState.LIMITED
            } else {
                CapabilityState.UNSUPPORTED
            }
        } catch (e: Throwable) {
            CapabilityState.UNSUPPORTED
        }
    }

    fun addBinderReceivedListenerSticky(listener: Shizuku.OnBinderReceivedListener) {
        try {
            Shizuku.addBinderReceivedListenerSticky(listener)
        } catch (e: Throwable) {
        }
    }

    fun removeBinderReceivedListener(listener: Shizuku.OnBinderReceivedListener) {
        try {
            Shizuku.removeBinderReceivedListener(listener)
        } catch (e: Throwable) {
        }
    }

    fun addRequestPermissionResultListener(listener: Shizuku.OnRequestPermissionResultListener) {
        try {
            Shizuku.addRequestPermissionResultListener(listener)
        } catch (e: Throwable) {
        }
    }

    fun removeRequestPermissionResultListener(listener: Shizuku.OnRequestPermissionResultListener) {
        try {
            Shizuku.removeRequestPermissionResultListener(listener)
        } catch (e: Throwable) {
        }
    }

    fun requestPermission(requestCode: Int = REQUEST_CODE_SHIZUKU_PERMISSION): Boolean {
        return try {
            if (isServiceRunning() && !hasPermission()) {
                Shizuku.requestPermission(requestCode)
                true
            } else {
                false
            }
        } catch (e: Throwable) {
            false
        }
    }

    fun openShizuku(context: Context): Boolean {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(SHIZUKU_PACKAGE_NAME)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$SHIZUKU_PACKAGE_NAME")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(marketIntent)
                    true
                } catch (e: Throwable) {
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(webIntent)
                    true
                }
            }
        } catch (e: Throwable) {
            false
        }
    }

    fun executeShell(command: String): String? {
        if (!hasPermission()) return null
        return try {
            val method = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true
            val process = method.invoke(
                null,
                arrayOf("sh", "-c", command),
                null,
                null
            ) as? Process ?: return null

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                process.waitFor(2, TimeUnit.SECONDS)
            } else {
                process.waitFor()
            }
            output.toString().trim()
        } catch (e: Throwable) {
            null
        }
    }
}
