package com.turbouro.app.model

enum class AppTheme {
    SYSTEM,
    DARK,
    LIGHT;

    val displayName: String
        get() = when (this) {
            SYSTEM -> "Sistem"
            DARK -> "Gelap"
            LIGHT -> "Terang"
        }
}

data class AppSettings(
    val theme: AppTheme = AppTheme.DARK,
    val startOnBoot: Boolean = false,
    val autoDetectGame: Boolean = true,
    val defaultProfile: ProfileType = ProfileType.BALANCED,
    val thermalProtection: Boolean = true,
    val backgroundCleanup: Boolean = true,
    val saveSessionHistory: Boolean = true,
    val isOnboardingCompleted: Boolean = false
)
