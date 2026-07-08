/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command

import net.ccbluex.liquidbounce.features.command.CommandUtils.doubleArg
import net.ccbluex.liquidbounce.features.command.CommandUtils.intArg
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CommandUtilsTest {

    @Test
    fun `integer argument accepts inclusive boundaries`() {
        val args = arrayOf("-5", "5")
        assertEquals(-5, args.intArg(0, -5, 5))
        assertEquals(5, args.intArg(1, -5, 5))
    }

    @Test
    fun `integer argument rejects missing invalid and overflowing values`() {
        assertNull(arrayOf("x").intArg(0))
        assertNull(arrayOf("2147483648").intArg(0))
        assertNull(emptyArray<String>().intArg(0))
    }

    @Test
    fun `double argument accepts finite values in range`() {
        assertEquals(1.25, arrayOf("1.25").doubleArg(0, 1.0, 2.0)!!, 0.0)
    }

    @Test
    fun `double argument rejects non finite values`() {
        assertNull(arrayOf("NaN").doubleArg(0))
        assertNull(arrayOf("Infinity").doubleArg(0))
        assertNull(arrayOf("-Infinity").doubleArg(0))
    }

    @Test
    fun `double argument rejects values outside range`() {
        assertNull(arrayOf("0.99").doubleArg(0, 1.0, 2.0))
        assertNull(arrayOf("2.01").doubleArg(0, 1.0, 2.0))
    }
}
