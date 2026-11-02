package com.example.edgejump

import com.example.edgejump.model.DifficultyConfig
import com.example.edgejump.model.DifficultyTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DifficultyProgressionTest {

    @Test
    fun testEasyTierProgression() {
        val config0 = DifficultyConfig.forJumpCount(0)
        val config5 = DifficultyConfig.forJumpCount(5)

        assertEquals(DifficultyTier.EASY, config0.tier)
        assertEquals(DifficultyTier.EASY, config5.tier)
        assertEquals(2800L, config0.reactionTimeMs)
        assertEquals(0.28f, config0.platformWidthFraction, 0.001f)
    }

    @Test
    fun testMediumTierProgression() {
        val config6 = DifficultyConfig.forJumpCount(6)
        val config15 = DifficultyConfig.forJumpCount(15)

        assertEquals(DifficultyTier.MEDIUM, config6.tier)
        assertEquals(DifficultyTier.MEDIUM, config15.tier)

        // As jump count rises in medium, reaction time decreases
        assertTrue(config15.reactionTimeMs < config6.reactionTimeMs)
        assertTrue(config15.platformWidthFraction < config6.platformWidthFraction)
    }

    @Test
    fun testHardTierProgression() {
        val config16 = DifficultyConfig.forJumpCount(16)
        val config40 = DifficultyConfig.forJumpCount(40)

        assertEquals(DifficultyTier.HARD, config16.tier)
        assertEquals(DifficultyTier.HARD, config40.tier)

        // Reaction time clamped at minimum 1100ms
        assertTrue(config40.reactionTimeMs >= 1100L)
        assertTrue(config40.platformWidthFraction >= 0.15f)
    }
}
