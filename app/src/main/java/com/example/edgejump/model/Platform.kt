package com.example.edgejump.model

data class Platform(
    val id: Long,
    val edge: Direction,
    val offsetFraction: Float = 0.5f,
    val widthFraction: Float = 0.24f,
    val thicknessFraction: Float = 0.05f
) {
    /**
     * Center landing coordinate in normalized space (0f..1f, 0f..1f).
     * Where the character stands when resting on this platform.
     */
    val landingX: Float
        get() = when (edge) {
            Direction.LEFT -> thicknessFraction * 0.9f
            Direction.RIGHT -> 1f - thicknessFraction * 0.9f
            Direction.UP -> offsetFraction
            Direction.DOWN -> offsetFraction
        }

    val landingY: Float
        get() = when (edge) {
            Direction.LEFT -> offsetFraction
            Direction.RIGHT -> offsetFraction
            Direction.UP -> thicknessFraction * 0.9f
            Direction.DOWN -> 1f - thicknessFraction * 0.9f
        }

    /**
     * Platform start and end along the edge (normalized 0f..1f).
     */
    val startSpan: Float
        get() = (offsetFraction - widthFraction / 2f).coerceAtLeast(0.04f)

    val endSpan: Float
        get() = (offsetFraction + widthFraction / 2f).coerceAtMost(0.96f)
}
