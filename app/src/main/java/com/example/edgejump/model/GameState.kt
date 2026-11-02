package com.example.edgejump.model

enum class GamePhase {
    WAITING_FOR_INPUT,
    JUMPING,
    LANDED,
    GAME_OVER,
    PAUSED
}

enum class JumpOutcome {
    SUCCESS,
    WRONG_DIRECTION,
    TIMEOUT
}

data class ScorePopup(
    val points: Int,
    val isBonus: Boolean = false,
    val message: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class GameState(
    val score: Int = 0,
    val streak: Int = 0,
    val maxStreak: Int = 0,
    val platformsReached: Int = 0,
    val currentPlatform: Platform = Platform(id = 1L, edge = Direction.DOWN, offsetFraction = 0.5f),
    val targetPlatform: Platform = Platform(id = 2L, edge = Direction.UP, offsetFraction = 0.5f),
    val phase: GamePhase = GamePhase.WAITING_FOR_INPUT,
    val lastOutcome: JumpOutcome = JumpOutcome.SUCCESS,
    val jumpProgress: Float = 0f,
    val characterX: Float = 0.5f,
    val characterY: Float = 0.95f,
    val timeRemainingFraction: Float = 1f,
    val activePopup: ScorePopup? = null,
    val bestScore: Int = 0,
    val isNewHighScore: Boolean = false,
    val difficulty: DifficultyConfig = DifficultyConfig.forJumpCount(0),
    val historyEdges: List<Direction> = listOf(Direction.DOWN, Direction.UP)
) {
    val isInputLocked: Boolean
        get() = phase == GamePhase.JUMPING || phase == GamePhase.LANDED || phase == GamePhase.GAME_OVER || phase == GamePhase.PAUSED
}
