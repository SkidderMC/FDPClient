/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.kotlin

import org.junit.Assert.assertEquals
import org.junit.Test

class CollectionExtensionTest {

    @Test
    fun `removeEach respects the maximum number of removals`() {
        val values = mutableListOf(1, 2, 3)

        values.removeEach(max = 1) { true }

        assertEquals(listOf(2, 3), values)
    }

    @Test
    fun `removeEach with zero maximum preserves the collection`() {
        val values = mutableListOf(1, 2, 3)

        values.removeEach(max = 0) { true }

        assertEquals(listOf(1, 2, 3), values)
    }
}
