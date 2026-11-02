package com.example.edgejump.model

enum class Direction(val symbol: String, val displayName: String) {
    LEFT("←", "Left"),
    UP("↑", "Up"),
    RIGHT("→", "Right"),
    DOWN("↓", "Down");

    val opposite: Direction
        get() = when (this) {
            LEFT -> RIGHT
            RIGHT -> LEFT
            UP -> DOWN
            DOWN -> UP
        }

    fun isPerpendicular(other: Direction): Boolean =
        this != other && this != other.opposite

    companion object {
        fun fromDelta(dx: Float, dy: Float, threshold: Float = 30f): Direction? {
            val absX = kotlin.math.abs(dx)
            val absY = kotlin.math.abs(dy)
            if (absX < threshold && absY < threshold) return null
            return if (absX > absY) {
                if (dx > 0) RIGHT else LEFT
            } else {
                if (dy > 0) DOWN else UP
            }
        }
    }
}
