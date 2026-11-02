package com.example.edgejump.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.edgejump.data.PlayerStats
import com.example.edgejump.data.PreferencesRepository
import com.example.edgejump.model.ControlMode
import com.example.edgejump.model.GameSettings
import com.example.edgejump.model.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PreferencesRepository(application)

    val settings: StateFlow<GameSettings> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, GameSettings())

    val stats: StateFlow<PlayerStats> = repository.statsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, PlayerStats())

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSoundEnabled(enabled)
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setHapticsEnabled(enabled)
        }
    }

    fun setControlMode(mode: ControlMode) {
        viewModelScope.launch {
            repository.setControlMode(mode)
        }
    }

    fun resetStats() {
        viewModelScope.launch {
            repository.resetStats()
        }
    }
}
