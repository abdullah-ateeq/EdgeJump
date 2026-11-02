package com.example.edgejump.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edgejump.model.ControlMode
import com.example.edgejump.model.GamePhase
import com.example.edgejump.ui.GameViewModel
import com.example.edgejump.ui.components.AppIcons
import com.example.edgejump.ui.components.DirectionPad
import com.example.edgejump.ui.components.PlayfieldView
import com.example.edgejump.ui.components.ScoreDisplay
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onGameOver: () -> Unit,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()
    val settings by viewModel.settings.collectAsState()

    // Handle system back button -> pause game
    BackHandler {
        if (gameState.phase == GamePhase.WAITING_FOR_INPUT) {
            viewModel.pauseGame()
        } else if (gameState.phase == GamePhase.PAUSED) {
            onNavigateHome()
        }
    }

    // When game over occurs, delay briefly for fall animation then navigate to Result
    LaunchedEffect(gameState.phase) {
        if (gameState.phase == GamePhase.GAME_OVER) {
            delay(400L)
            onGameOver()
        }
    }

    val enableSwipe = settings.controlMode == ControlMode.BOTH || settings.controlMode == ControlMode.SWIPE_ONLY
    val showDirectionPad = settings.controlMode == ControlMode.BOTH || settings.controlMode == ControlMode.BUTTONS_ONLY

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // HUD: Score, Streak, Timer, Pause
            ScoreDisplay(
                gameState = gameState,
                onPauseClick = { viewModel.pauseGame() }
            )

            // Arena / Playfield
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                PlayfieldView(
                    gameState = gameState,
                    onDirectionInput = { viewModel.onDirectionInput(it) },
                    enableSwipe = enableSwipe
                )
            }

            // Direction Pad for touch controls (if enabled)
            if (showDirectionPad) {
                DirectionPad(
                    onDirectionClick = { viewModel.onDirectionInput(it) },
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                    enabled = !gameState.isInputLocked
                )
            } else {
                // Spacer to keep layout balanced when swipe-only
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Pause Overlay
        AnimatedVisibility(
            visible = gameState.phase == GamePhase.PAUSED,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.88f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "PAUSED",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = "Current Score: ${gameState.score}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Resume Button
                        Button(
                            onClick = { viewModel.resumeGame() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(AppIcons.Play, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("RESUME", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Restart Button
                        OutlinedButton(
                            onClick = { viewModel.startNewGame() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(AppIcons.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("RESTART", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Exit to Home
                        OutlinedButton(
                            onClick = onNavigateHome,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(AppIcons.Home, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("HOME", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
