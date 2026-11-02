package com.example.edgejump

import com.example.edgejump.model.Direction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DirectionTest {

    @Test
    fun testOppositeDirections() {
        assertEquals(Direction.RIGHT, Direction.LEFT.opposite)
        assertEquals(Direction.LEFT, Direction.RIGHT.opposite)
        assertEquals(Direction.DOWN, Direction.UP.opposite)
        assertEquals(Direction.UP, Direction.DOWN.opposite)
    }

    @Test
    fun testPerpendicularDirections() {
        assertTrue(Direction.LEFT.isPerpendicular(Direction.UP))
        assertTrue(Direction.LEFT.isPerpendicular(Direction.DOWN))
        assertFalse(Direction.LEFT.isPerpendicular(Direction.RIGHT))
        assertFalse(Direction.LEFT.isPerpendicular(Direction.LEFT))

        assertTrue(Direction.UP.isPerpendicular(Direction.LEFT))
        assertTrue(Direction.UP.isPerpendicular(Direction.RIGHT))
        assertFalse(Direction.UP.isPerpendicular(Direction.DOWN))
        assertFalse(Direction.UP.isPerpendicular(Direction.UP))
    }

    @Test
    fun testFromDeltaParsing() {
        // Horizontal swipes
        assertEquals(Direction.RIGHT, Direction.fromDelta(60f, 10f))
        assertEquals(Direction.LEFT, Direction.fromDelta(-60f, -5f))

        // Vertical swipes
        assertEquals(Direction.DOWN, Direction.fromDelta(5f, 70f))
        assertEquals(Direction.UP, Direction.fromDelta(-10f, -65f))

        // Deadzone threshold (under 30f)
        assertNull(Direction.fromDelta(15f, 20f))
        assertNull(Direction.fromDelta(0f, 0f))
    }
}
