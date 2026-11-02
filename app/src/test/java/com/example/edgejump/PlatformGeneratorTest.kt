package com.example.edgejump

import com.example.edgejump.game.PlatformGenerator
import com.example.edgejump.model.Direction
import com.example.edgejump.model.Platform
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class PlatformGeneratorTest {

    @Test
    fun testNeverGeneratesSameEdgeAsCurrent() {
        val initial = PlatformGenerator.createInitialPlatform()
        val random = Random(42)
        var current = initial
        val history = mutableListOf(current.edge)

        for (i in 0 until 100) {
            val next = PlatformGenerator.generateNext(
                currentPlatform = current,
                historyEdges = history,
                jumpCount = i,
                random = random,
                nextId = (i + 2).toLong()
            )

            assertNotEquals(
                "Target platform edge must never equal current platform edge",
                current.edge,
                next.edge
            )
            assertTrue(
                "Generated platform must be reachable",
                PlatformGenerator.isReachable(current, next)
            )

            current = next
            history.add(next.edge)
        }
    }

    @Test
    fun testPlatformOffsetBounds() {
        val initial = PlatformGenerator.createInitialPlatform()
        val random = Random(123)
        var current = initial
        val history = mutableListOf(current.edge)

        for (i in 0 until 50) {
            val next = PlatformGenerator.generateNext(
                currentPlatform = current,
                historyEdges = history,
                jumpCount = i,
                random = random,
                nextId = (i + 2).toLong()
            )

            assertTrue(
                "Offset fraction should stay between 0.22 and 0.78, but was ${next.offsetFraction}",
                next.offsetFraction in 0.22f..0.78f
            )

            current = next
            history.add(next.edge)
        }
    }

    @Test
    fun testReachabilityValidation() {
        val p1 = Platform(id = 1L, edge = Direction.DOWN, offsetFraction = 0.5f, widthFraction = 0.25f)
        val p2 = Platform(id = 2L, edge = Direction.UP, offsetFraction = 0.5f, widthFraction = 0.25f)
        val sameEdge = Platform(id = 3L, edge = Direction.DOWN, offsetFraction = 0.5f, widthFraction = 0.25f)
        val outOfBounds = Platform(id = 4L, edge = Direction.LEFT, offsetFraction = 0.95f, widthFraction = 0.25f)
        val tooSmall = Platform(id = 5L, edge = Direction.RIGHT, offsetFraction = 0.5f, widthFraction = 0.05f)

        assertTrue(PlatformGenerator.isReachable(p1, p2))
        assertFalse("Same edge platforms are unreachable", PlatformGenerator.isReachable(p1, sameEdge))
        assertFalse("Out of bounds platform is unreachable", PlatformGenerator.isReachable(p1, outOfBounds))
        assertFalse("Too narrow platform is unreachable", PlatformGenerator.isReachable(p1, tooSmall))
    }

    @Test
    fun testCandidateEdgesAvoidsClustering() {
        val history = listOf(Direction.UP, Direction.UP)
        val candidates = PlatformGenerator.getCandidateEdges(Direction.DOWN, history)

        assertFalse("Edge that appeared 2 times in recent history should be avoided", candidates.contains(Direction.UP))
        assertFalse("Current edge should never be a candidate", candidates.contains(Direction.DOWN))
        assertTrue(candidates.contains(Direction.LEFT))
        assertTrue(candidates.contains(Direction.RIGHT))
    }
}
