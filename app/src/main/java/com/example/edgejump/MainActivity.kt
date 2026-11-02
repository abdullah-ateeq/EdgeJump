package com.example.edgejump

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.edgejump.ui.GameViewModel
import com.example.edgejump.ui.SettingsViewModel
import com.example.edgejump.ui.navigation.EdgeJumpNavGraph
import com.example.edgejump.ui.theme.EdgeJumpTheme

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by settingsViewModel.settings.collectAsState()

            EdgeJumpTheme(themeMode = settings.themeMode) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                ) {
                    EdgeJumpNavGraph(
                        gameViewModel = gameViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}
