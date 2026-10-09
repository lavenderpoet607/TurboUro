package com.turbouro.app.model

enum class CapabilityState {
    AVAILABLE,
    LIMITED,
    REQUIRES_PERMISSION,
    REQUIRES_SHIZUKU,
    UNSUPPORTED,
    ERROR;

    val isAvailable: Boolean
        get() = this == AVAILABLE

    val displayText: String
        get() = when (this) {
            AVAILABLE -> "Tersedia"
            LIMITED -> "Terbatas"
            REQUIRES_PERMISSION -> "Perlu Izin"
            REQUIRES_SHIZUKU -> "Perlu Shizuku"
            UNSUPPORTED -> "Tidak Didukung"
            ERROR -> "Kesalahan"
        }

    val symbol: String
        get() = when (this) {
            AVAILABLE -> "✓"
            LIMITED -> "!"
            REQUIRES_PERMISSION -> "!"
            REQUIRES_SHIZUKU -> "!"
            UNSUPPORTED -> "-"
            ERROR -> "×"
        }
}
