package com.example.edgejump.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.edgejump.data.PlayerStats
import com.example.edgejump.data.PreferencesRepository
import com.example.edgejump.game.GameEngine
import com.example.edgejump.game.HapticController
import com.example.edgejump.game.PlatformGenerator
import com.example.edgejump.game.SoundPlayer
import com.example.edgejump.model.DifficultyConfig
import com.example.edgejump.model.Direction
import com.example.edgejump.model.GamePhase
import com.example.edgejump.model.GameSettings
import com.example.edgejump.model.GameState
import com.example.edgejump.model.JumpOutcome
import com.example.edgejump.model.Platform
import com.example.edgejump.model.ScorePopup
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PreferencesRepository(application)
    val soundPlayer = SoundPlayer()
    val hapticController = HapticController(application)

    val settings: StateFlow<GameSettings> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, GameSettings())

    val stats: StateFlow<PlayerStats> = repository.statsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, PlayerStats())

    private val _gameState = MutableStateFlow(createInitialState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var timerJob: Job? = null
    private var jumpAnimationJob: Job? = null

    init {
        viewModelScope.launch {
            settings.collect { s ->
                soundPlayer.isSoundEnabled = s.soundEnabled
                hapticController.isHapticsEnabled = s.hapticsEnabled
            }
        }
        viewModelScope.launch {
            stats.collect { st ->
                _gameState.update { it.copy(bestScore = st.bestScore) }
            }
        }
    }

    private fun createInitialState(): GameState {
        val initialPlatform = PlatformGenerator.createInitialPlatform()
        val history = listOf(initialPlatform.edge)
        val target = PlatformGenerator.generateNext(
            currentPlatform = initialPlatform,
            historyEdges = history,
            jumpCount = 0
        )
        return GameState(
            score = 0,
            streak = 0,
            maxStreak = 0,
            platformsReached = 0,
            currentPlatform = initialPlatform,
            targetPlatform = target,
            phase = GamePhase.WAITING_FOR_INPUT,
            characterX = initialPlatform.landingX,
            characterY = initialPlatform.landingY,
            difficulty = DifficultyConfig.forJumpCount(0),
            historyEdges = history + target.edge
        )
    }

    fun startNewGame() {
        timerJob?.cancel()
        jumpAnimationJob?.cancel()

        val initialPlatform = PlatformGenerator.createInitialPlatform()
        val history = listOf(initialPlatform.edge)
        val target = PlatformGenerator.generateNext(
            currentPlatform = initialPlatform,
            historyEdges = history,
            jumpCount = 0
        )

        _gameState.value = GameState(
            score = 0,
            streak = 0,
            maxStreak = 0,
            platformsReached = 0,
            currentPlatform = initialPlatform,
            targetPlatform = target,
            phase = GamePhase.WAITING_FOR_INPUT,
            lastOutcome = JumpOutcome.SUCCESS,
            jumpProgress = 0f,
            characterX = initialPlatform.landingX,
            characterY = initialPlatform.landingY,
            timeRemainingFraction = 1f,
            activePopup = null,
            bestScore = stats.value.bestScore,
            isNewHighScore = false,
            difficulty = DifficultyConfig.forJumpCount(0),
            historyEdges = history + target.edge
        )

        startReactionTimer()
    }

    fun onDirectionInput(input: Direction) {
        val current = _gameState.value
        if (current.isInputLocked) return

        timerJob?.cancel()

        val isCorrect = GameEngine.validateDirection(input, current.targetPlatform)
        if (isCorrect) {
            handleSuccessfulJump(input)
        } else {
            handleFailedJump(JumpOutcome.WRONG_DIRECTION, input)
        }
    }

    private fun handleSuccessfulJump(input: Direction) {
        val current = _gameState.value
        val jumpDuration = current.difficulty.jumpDurationMs
        soundPlayer.playJump()
        hapticController.jump()

        _gameState.update {
            it.copy(
                phase = GamePhase.JUMPING,
                lastOutcome = JumpOutcome.SUCCESS,
                jumpProgress = 0f
            )
        }

        jumpAnimationJob = viewModelScope.launch {
            val startX = current.currentPlatform.landingX
            val startY = current.currentPlatform.landingY
            val targetX = current.targetPlatform.landingX
            val targetY = current.targetPlatform.landingY

            val steps = 24
            val stepDelay = jumpDuration / steps
            for (step in 1..steps) {
                delay(stepDelay)
                val progress = step.toFloat() / steps
                val (curX, curY) = GameEngine.computeJumpPosition(startX, startY, targetX, targetY, progress)
                _gameState.update {
                    it.copy(
                        jumpProgress = progress,
                        characterX = curX,
                        characterY = curY
                    )
                }
            }

            // Landed!
            onLandSuccess()
        }
    }

    private suspend fun onLandSuccess() {
        val current = _gameState.value
        val newStreak = current.streak + 1
        val newMaxStreak = maxOf(current.maxStreak, newStreak)
        val newPlatforms = current.platformsReached + 1

        val scoreResult = GameEngine.calculateScore(
            streak = current.streak,
            timeRemainingFraction = current.timeRemainingFraction
        )
        val newScore = current.score + scoreResult.pointsAwarded
        val isNewBest = newScore > current.bestScore

        soundPlayer.playLand(newStreak)
        hapticController.land()

        val newDifficulty = DifficultyConfig.forJumpCount(newPlatforms)
        val nextTarget = PlatformGenerator.generateNext(
            currentPlatform = current.targetPlatform,
            historyEdges = current.historyEdges,
            jumpCount = newPlatforms
        )

        val updatedHistory = (current.historyEdges + nextTarget.edge).takeLast(6)

        _gameState.update {
            it.copy(
                score = newScore,
                streak = newStreak,
                maxStreak = newMaxStreak,
                platformsReached = newPlatforms,
                currentPlatform = current.targetPlatform,
                targetPlatform = nextTarget,
                phase = GamePhase.LANDED,
                characterX = current.targetPlatform.landingX,
                characterY = current.targetPlatform.landingY,
                jumpProgress = 1f,
                activePopup = ScorePopup(
                    points = scoreResult.pointsAwarded,
                    isBonus = scoreResult.isBonus,
                    message = scoreResult.feedbackMessage
                ),
                bestScore = if (isNewBest) newScore else current.bestScore,
                isNewHighScore = isNewBest || current.isNewHighScore,
                difficulty = newDifficulty,
                historyEdges = updatedHistory,
                timeRemainingFraction = 1f
            )
        }

        delay(90L) // brief tactile landing pause

        _gameState.update { it.copy(phase = GamePhase.WAITING_FOR_INPUT) }
        startReactionTimer()
    }

    private fun handleFailedJump(outcome: JumpOutcome, attemptedDirection: Direction?) {
        val current = _gameState.value
        soundPlayer.playMiss()
        hapticController.miss()

        _gameState.update {
            it.copy(
                phase = GamePhase.JUMPING,
                lastOutcome = outcome,
                jumpProgress = 0f
            )
        }

        jumpAnimationJob = viewModelScope.launch {
            val startX = current.characterX
            val startY = current.characterY
            val dir = attemptedDirection ?: current.targetPlatform.edge.opposite

            val steps = 18
            val stepDelay = 220L / steps
            for (step in 1..steps) {
                delay(stepDelay)
                val progress = step.toFloat() / steps
                val (curX, curY) = GameEngine.computeMissPosition(startX, startY, dir, progress)
                _gameState.update {
                    it.copy(
                        jumpProgress = progress,
                        characterX = curX,
                        characterY = curY
                    )
                }
            }

            delay(280L)

            // Finalize run
            val finalState = _gameState.value
            _gameState.update { it.copy(phase = GamePhase.GAME_OVER) }

            repository.recordRunResults(
                score = finalState.score,
                platforms = finalState.platformsReached,
                streak = finalState.maxStreak
            )
        }
    }

    private fun startReactionTimer() {
        timerJob?.cancel()
        val durationMs = _gameState.value.difficulty.reactionTimeMs

        timerJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            while (true) {
                delay(16L) // ~60fps timer update
                val elapsed = System.currentTimeMillis() - startTime
                val remainingFraction = (1f - (elapsed.toFloat() / durationMs)).coerceIn(0f, 1f)

                _gameState.update { it.copy(timeRemainingFraction = remainingFraction) }

                if (remainingFraction <= 0f) {
                    handleFailedJump(JumpOutcome.TIMEOUT, null)
                    break
                }
            }
        }
    }

    fun pauseGame() {
        if (_gameState.value.phase == GamePhase.WAITING_FOR_INPUT) {
            timerJob?.cancel()
            _gameState.update { it.copy(phase = GamePhase.PAUSED) }
        }
    }

    fun resumeGame() {
        if (_gameState.value.phase == GamePhase.PAUSED) {
            _gameState.update { it.copy(phase = GamePhase.WAITING_FOR_INPUT) }
            val remainingMs = (_gameState.value.timeRemainingFraction * _gameState.value.difficulty.reactionTimeMs).toLong()
            resumeReactionTimer(remainingMs)
        }
    }

    private fun resumeReactionTimer(remainingMs: Long) {
        timerJob?.cancel()
        val totalMs = _gameState.value.difficulty.reactionTimeMs

        timerJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            while (true) {
                delay(16L)
                val elapsed = System.currentTimeMillis() - startTime
                val remainingTime = (remainingMs - elapsed).coerceAtLeast(0L)
                val remainingFraction = (remainingTime.toFloat() / totalMs).coerceIn(0f, 1f)

                _gameState.update { it.copy(timeRemainingFraction = remainingFraction) }

                if (remainingTime <= 0L) {
                    handleFailedJump(JumpOutcome.TIMEOUT, null)
                    break
                }
            }
        }
    }
}
