package net.ccbluex.liquidbounce.features.module.modules.movement

import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClipPathFinderTest {

    private val origin = BlockPos(0, 64, 0)

    @Test
    fun `does not clip through a completely open path`() {
        val destination = ClipPathFinder.findDestination(origin, EnumFacing.NORTH, 6) { true }

        assertNull(destination)
    }

    @Test
    fun `returns first safe location after a wall`() {
        val destination = ClipPathFinder.findDestination(origin, EnumFacing.EAST, 6) { position ->
            position.x >= 3
        }

        assertEquals(BlockPos(3, 64, 0), destination)
    }

    @Test
    fun `rejects wall when no safe location exists within range`() {
        val destination = ClipPathFinder.findDestination(origin, EnumFacing.DOWN, 3) { false }

        assertNull(destination)
    }

    @Test
    fun `zero range never evaluates occupancy`() {
        var evaluated = false
        val destination = ClipPathFinder.findDestination(origin, EnumFacing.UP, 0) {
            evaluated = true
            true
        }

        assertNull(destination)
        assertEquals(false, evaluated)
    }
}
