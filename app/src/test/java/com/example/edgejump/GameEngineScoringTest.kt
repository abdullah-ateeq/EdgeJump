package com.example.edgejump

import com.example.edgejump.game.GameEngine
import com.example.edgejump.model.Direction
import com.example.edgejump.model.Platform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineScoringTest {

    @Test
    fun testValidateDirection() {
        val target = Platform(id = 10L, edge = Direction.LEFT)

        assertTrue(GameEngine.validateDirection(Direction.LEFT, target))
        assertFalse(GameEngine.validateDirection(Direction.RIGHT, target))
        assertFalse(GameEngine.validateDirection(Direction.UP, target))
        assertFalse(GameEngine.validateDirection(Direction.DOWN, target))
    }

    @Test
    fun testScoreCalculationBaseAndMultipliers() {
        // Streak 0, normal reaction speed
        val res0 = GameEngine.calculateScore(streak = 0, timeRemainingFraction = 0.3f)
        assertEquals(100, res0.pointsAwarded)
        assertFalse(res0.isBonus)

        // Streak 1 (multiplier 1.15)
        val res1 = GameEngine.calculateScore(streak = 1, timeRemainingFraction = 0.3f)
        assertEquals(115, res1.pointsAwarded)

        // Streak 5 (multiplier 1.75)
        val res5 = GameEngine.calculateScore(streak = 5, timeRemainingFraction = 0.3f)
        assertEquals(175, res5.pointsAwarded)
    }

    @Test
    fun testReactionSpeedBonus() {
        // Fast reaction bonus (+25)
        val resFast = GameEngine.calculateScore(streak = 0, timeRemainingFraction = 0.5f)
        assertEquals(125, resFast.pointsAwarded)
        assertTrue(resFast.isBonus)
        assertTrue(resFast.feedbackMessage.contains("FAST"))

        // Lightning reaction bonus (+50)
        val resLightning = GameEngine.calculateScore(streak = 0, timeRemainingFraction = 0.85f)
        assertEquals(150, resLightning.pointsAwarded)
        assertTrue(resLightning.isBonus)
        assertTrue(resLightning.feedbackMessage.contains("LIGHTNING"))
    }

    @Test
    fun testJumpPositionArc() {
        val startX = 0.5f
        val startY = 0.95f
        val targetX = 0.5f
        val targetY = 0.05f

        val atStart = GameEngine.computeJumpPosition(startX, startY, targetX, targetY, 0f)
        assertEquals(startX, atStart.first, 0.001f)
        assertEquals(startY, atStart.second, 0.001f)

        val atEnd = GameEngine.computeJumpPosition(startX, startY, targetX, targetY, 1f)
        assertEquals(targetX, atEnd.first, 0.001f)
        assertEquals(targetY, atEnd.second, 0.001f)

        // Midpoint should have arc elevation toward center
        val atMid = GameEngine.computeJumpPosition(startX, startY, targetX, targetY, 0.5f)
        assertTrue("Midpoint Y should be between start and end", atMid.second in 0.05f..0.95f)
    }

    @Test
    fun testMissPositionOffScreen() {
        val startX = 0.5f
        val startY = 0.5f
        val missPos = GameEngine.computeMissPosition(startX, startY, Direction.RIGHT, 1.0f)

        // Character should move right and fall with gravity
        assertTrue("Miss position should move right", missPos.first > startX)
        assertTrue("Miss position should fall downward with gravity", missPos.second > startY)
    }
}
