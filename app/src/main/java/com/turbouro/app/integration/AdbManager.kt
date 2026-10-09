package com.turbouro.app.integration

import com.turbouro.app.model.CapabilityState
import java.io.BufferedReader
import java.io.InputStreamReader

object AdbManager {

    fun isAdbAvailable(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("which sh")
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Throwable) {
            false
        }
    }

    fun getAdbState(): CapabilityState {
        return if (isAdbAvailable()) CapabilityState.AVAILABLE else CapabilityState.UNSUPPORTED
    }

    fun runCommand(cmd: String): String? {
        return try {
            val process = Runtime.getRuntime().exec(cmd)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append("\n")
            }
            process.waitFor()
            sb.toString().trim()
        } catch (e: Throwable) {
            null
        }
    }
}
