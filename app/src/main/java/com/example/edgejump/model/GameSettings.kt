package com.example.edgejump.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class ControlMode(val label: String) {
    BOTH("Swipe + Buttons"),
    BUTTONS_ONLY("Buttons Only"),
    SWIPE_ONLY("Swipe Only")
}

data class GameSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val controlMode: ControlMode = ControlMode.BOTH
)
