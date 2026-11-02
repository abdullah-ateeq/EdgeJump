package com.example.edgejump.game

import com.example.edgejump.model.Direction
import com.example.edgejump.model.Platform
import kotlin.math.PI
import kotlin.math.sin

data class ScoreResult(
    val pointsAwarded: Int,
    val isBonus: Boolean,
    val feedbackMessage: String
)

object GameEngine {

    const val BASE_SCORE = 100

    /**
     * Validates whether player's input direction reaches the target platform.
     */
    fun validateDirection(input: Direction, target: Platform): Boolean {
        return input == target.edge
    }

    /**
     * Calculates score awarded for a successful jump based on current streak
     * and reaction speed (timeRemainingFraction 0.0 .. 1.0).
     */
    fun calculateScore(streak: Int, timeRemainingFraction: Float): ScoreResult {
        val multiplier = 1.0f + ((streak * 0.15f).coerceAtMost(3.0f))
        val basePoints = (BASE_SCORE * multiplier).toInt()

        val (bonus, message) = when {
            timeRemainingFraction >= 0.70f -> Pair(50, "LIGHTNING! +50")
            timeRemainingFraction >= 0.45f -> Pair(25, "FAST! +25")
            streak > 0 && streak % 5 == 0 -> Pair(0, "${streak}X STREAK!")
            else -> Pair(0, "PERFECT!")
        }

        val totalPoints = basePoints + bonus
        return ScoreResult(
            pointsAwarded = totalPoints,
            isBonus = bonus > 0,
            feedbackMessage = message
        )
    }

    /**
     * Computes the (x, y) coordinates for a character jumping along an arc
     * from [startX], [startY] to [targetX], [targetY] at [progress] in [0..1].
     */
    fun computeJumpPosition(
        startX: Float,
        startY: Float,
        targetX: Float,
        targetY: Float,
        progress: Float
    ): Pair<Float, Float> {
        val clampedProgress = progress.coerceIn(0f, 1f)

        // Linear interpolation
        val curX = startX + (targetX - startX) * clampedProgress
        val curY = startY + (targetY - startY) * clampedProgress

        // Parabolic arc toward screen center (0.5, 0.5)
        val arcFactor = sin(clampedProgress * PI.toFloat()) * 0.12f
        val centerX = 0.5f
        val centerY = 0.5f

        val arcX = curX + (centerX - curX) * arcFactor
        val arcY = curY + (centerY - curY) * arcFactor

        return Pair(arcX, arcY)
    }

    /**
     * Computes the miss position when player jumps in an incorrect direction.
     * Character launches in [inputDirection] and plunges off the playfield.
     */
    fun computeMissPosition(
        startX: Float,
        startY: Float,
        inputDirection: Direction,
        progress: Float
    ): Pair<Float, Float> {
        val clampedProgress = progress.coerceIn(0f, 1.5f)
        val (dx, dy) = when (inputDirection) {
            Direction.LEFT -> Pair(-0.45f, 0.25f)
            Direction.RIGHT -> Pair(0.45f, 0.25f)
            Direction.UP -> Pair(0f, -0.45f)
            Direction.DOWN -> Pair(0f, 0.55f)
        }

        val launchX = startX + dx * clampedProgress
        // Gravity pulls downward during miss
        val gravityY = clampedProgress * clampedProgress * 0.6f
        val launchY = startY + dy * clampedProgress + gravityY

        return Pair(launchX, launchY)
    }
}
