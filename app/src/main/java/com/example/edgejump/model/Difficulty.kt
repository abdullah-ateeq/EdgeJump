package com.example.edgejump.model

enum class DifficultyTier(val label: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard")
}

data class DifficultyConfig(
    val tier: DifficultyTier,
    val reactionTimeMs: Long,
    val platformWidthFraction: Float,
    val jumpDurationMs: Long,
    val scoreMultiplierBase: Float
) {
    companion object {
        fun forJumpCount(jumpCount: Int): DifficultyConfig {
            return when {
                jumpCount < 6 -> DifficultyConfig(
                    tier = DifficultyTier.EASY,
                    reactionTimeMs = 2800L,
                    platformWidthFraction = 0.28f,
                    jumpDurationMs = 320L,
                    scoreMultiplierBase = 1.0f
                )
                jumpCount < 16 -> {
                    // Smooth scaling between 6 and 15
                    val step = (jumpCount - 6) / 10f
                    val reaction = (2400L - (step * 500L)).toLong()
                    val width = 0.24f - (step * 0.04f)
                    DifficultyConfig(
                        tier = DifficultyTier.MEDIUM,
                        reactionTimeMs = reaction,
                        platformWidthFraction = width,
                        jumpDurationMs = 270L,
                        scoreMultiplierBase = 1.5f
                    )
                }
                else -> {
                    // Hard scaling from 16 onwards
                    val step = ((jumpCount - 16) / 15f).coerceAtMost(1f)
                    val reaction = (1800L - (step * 600L)).toLong().coerceAtLeast(1100L)
                    val width = (0.20f - (step * 0.05f)).coerceAtLeast(0.15f)
                    DifficultyConfig(
                        tier = DifficultyTier.HARD,
                        reactionTimeMs = reaction,
                        platformWidthFraction = width,
                        jumpDurationMs = 220L,
                        scoreMultiplierBase = 2.0f
                    )
                }
            }
        }
    }
}
