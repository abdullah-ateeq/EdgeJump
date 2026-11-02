package com.example.edgejump.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.edgejump.model.ControlMode
import com.example.edgejump.model.GameSettings
import com.example.edgejump.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "edge_jump_prefs")

data class PlayerStats(
    val bestScore: Int = 0,
    val highestPlatforms: Int = 0,
    val highestStreak: Int = 0
)

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val BEST_SCORE = intPreferencesKey("best_score")
        val HIGHEST_PLATFORMS = intPreferencesKey("highest_platforms")
        val HIGHEST_STREAK = intPreferencesKey("highest_streak")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val CONTROL_MODE = stringPreferencesKey("control_mode")
    }

    val statsFlow: Flow<PlayerStats> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            PlayerStats(
                bestScore = prefs[Keys.BEST_SCORE] ?: 0,
                highestPlatforms = prefs[Keys.HIGHEST_PLATFORMS] ?: 0,
                highestStreak = prefs[Keys.HIGHEST_STREAK] ?: 0
            )
        }

    val settingsFlow: Flow<GameSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val themeStr = prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
            val themeMode = try {
                ThemeMode.valueOf(themeStr)
            } catch (_: Exception) {
                ThemeMode.SYSTEM
            }

            val controlStr = prefs[Keys.CONTROL_MODE] ?: ControlMode.BOTH.name
            val controlMode = try {
                ControlMode.valueOf(controlStr)
            } catch (_: Exception) {
                ControlMode.BOTH
            }

            GameSettings(
                themeMode = themeMode,
                soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
                hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
                controlMode = controlMode
            )
        }

    suspend fun recordRunResults(score: Int, platforms: Int, streak: Int) {
        context.dataStore.edit { prefs ->
            val currentBest = prefs[Keys.BEST_SCORE] ?: 0
            if (score > currentBest) {
                prefs[Keys.BEST_SCORE] = score
            }

            val currentPlatforms = prefs[Keys.HIGHEST_PLATFORMS] ?: 0
            if (platforms > currentPlatforms) {
                prefs[Keys.HIGHEST_PLATFORMS] = platforms
            }

            val currentStreak = prefs[Keys.HIGHEST_STREAK] ?: 0
            if (streak > currentStreak) {
                prefs[Keys.HIGHEST_STREAK] = streak
            }
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun setControlMode(mode: ControlMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CONTROL_MODE] = mode.name
        }
    }

    suspend fun resetStats() {
        context.dataStore.edit { prefs ->
            prefs[Keys.BEST_SCORE] = 0
            prefs[Keys.HIGHEST_PLATFORMS] = 0
            prefs[Keys.HIGHEST_STREAK] = 0
        }
    }
}
