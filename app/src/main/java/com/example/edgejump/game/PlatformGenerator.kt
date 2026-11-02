package com.example.edgejump.game

import com.example.edgejump.model.DifficultyConfig
import com.example.edgejump.model.Direction
import com.example.edgejump.model.Platform
import kotlin.random.Random

object PlatformGenerator {

    private const val MIN_OFFSET = 0.22f
    private const val MAX_OFFSET = 0.78f
    private const val THICKNESS_FRACTION = 0.05f

    /**
     * Generates a new target platform reachable from [currentPlatform].
     * Avoids [currentPlatform.edge] and prevents repetitive edge clustering.
     */
    fun generateNext(
        currentPlatform: Platform,
        historyEdges: List<Direction>,
        jumpCount: Int,
        random: Random = Random.Default,
        nextId: Long = System.currentTimeMillis()
    ): Platform {
        val difficulty = DifficultyConfig.forJumpCount(jumpCount)
        val validEdges = getCandidateEdges(currentPlatform.edge, historyEdges)

        // Select an edge from candidate edges
        val chosenEdge = validEdges[random.nextInt(validEdges.size)]

        // Offset along the edge: between 0.22 and 0.78
        // Add subtle variation, keeping it clearly in view
        val range = MAX_OFFSET - MIN_OFFSET
        val offset = MIN_OFFSET + (random.nextFloat() * range)

        // Width fraction shrinks slightly with difficulty
        val widthFraction = difficulty.platformWidthFraction

        val platform = Platform(
            id = nextId,
            edge = chosenEdge,
            offsetFraction = offset,
            widthFraction = widthFraction,
            thicknessFraction = THICKNESS_FRACTION
        )

        require(isReachable(currentPlatform, platform)) {
            "Generated platform must be reachable from current platform"
        }

        return platform
    }

    /**
     * Determines valid edges for the next target platform.
     * Never permits the current platform's edge, and excludes edges that have occurred
     * 2 or more times in the last 3 entries.
     */
    fun getCandidateEdges(
        currentEdge: Direction,
        historyEdges: List<Direction>
    ): List<Direction> {
        val allOtherEdges = Direction.entries.filter { it != currentEdge }

        // Look at the last 2 targets in history
        val lastTwo = historyEdges.takeLast(2)
        val filtered = allOtherEdges.filter { candidate ->
            // Prevent 3-in-a-row of the same target edge
            val countInRecent = lastTwo.count { it == candidate }
            countInRecent < 2
        }

        return if (filtered.isNotEmpty()) filtered else allOtherEdges
    }

    /**
     * Deterministic validation that the jump between two platforms is physically fair and reachable.
     */
    fun isReachable(from: Platform, to: Platform): Boolean {
        if (from.edge == to.edge) return false
        if (to.offsetFraction < 0.15f || to.offsetFraction > 0.85f) return false
        if (to.widthFraction < 0.10f || to.widthFraction > 0.40f) return false
        return true
    }

    /**
     * Creates an initial starter platform.
     */
    fun createInitialPlatform(): Platform {
        return Platform(
            id = 1L,
            edge = Direction.DOWN,
            offsetFraction = 0.5f,
            widthFraction = 0.30f,
            thicknessFraction = THICKNESS_FRACTION
        )
    }
}
