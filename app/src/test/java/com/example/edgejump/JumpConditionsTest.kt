package com.example.edgejump

import com.example.edgejump.game.GameEngine
import com.example.edgejump.model.Direction
import com.example.edgejump.model.GamePhase
import com.example.edgejump.model.GameState
import com.example.edgejump.model.JumpOutcome
import com.example.edgejump.model.Platform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JumpConditionsTest {

    @Test
    fun testSuccessfulJumpDirectionCheck() {
        val currentPlatform = Platform(id = 1L, edge = Direction.DOWN)
        val targetPlatform = Platform(id = 2L, edge = Direction.UP)

        val playerInput = Direction.UP
        val isCorrect = GameEngine.validateDirection(playerInput, targetPlatform)

        assertTrue("Player input matching target edge should be successful", isCorrect)
    }

    @Test
    fun testFailedJumpWrongDirectionCheck() {
        val currentPlatform = Platform(id = 1L, edge = Direction.DOWN)
        val targetPlatform = Platform(id = 2L, edge = Direction.UP)

        val wrongInput = Direction.LEFT
        val isCorrect = GameEngine.validateDirection(wrongInput, targetPlatform)

        assertFalse("Player input not matching target edge should fail", isCorrect)
    }

    @Test
    fun testInputLockDuringTransitions() {
        val waitingState = GameState(phase = GamePhase.WAITING_FOR_INPUT)
        assertFalse("Input should not be locked when waiting for input", waitingState.isInputLocked)

        val jumpingState = GameState(phase = GamePhase.JUMPING)
        assertTrue("Input should be locked while jumping", jumpingState.isInputLocked)

        val landedState = GameState(phase = GamePhase.LANDED)
        assertTrue("Input should be locked during landing pause", landedState.isInputLocked)

        val gameOverState = GameState(phase = GamePhase.GAME_OVER)
        assertTrue("Input should be locked when game is over", gameOverState.isInputLocked)

        val pausedState = GameState(phase = GamePhase.PAUSED)
        assertTrue("Input should be locked when game is paused", pausedState.isInputLocked)
    }

    @Test
    fun testScoreAndStreakAccumulation() {
        var streak = 0
        var score = 0

        // Perform 3 successful jumps with fast reaction
        for (i in 1..3) {
            val res = GameEngine.calculateScore(streak = streak, timeRemainingFraction = 0.8f)
            score += res.pointsAwarded
            streak++
        }

        assertEquals(3, streak)
        // Jump 1: 100 + 50 = 150
        // Jump 2: 115 + 50 = 165
        // Jump 3: 130 + 50 = 180
        // Total = 495
        assertEquals(495, score)
    }
}
