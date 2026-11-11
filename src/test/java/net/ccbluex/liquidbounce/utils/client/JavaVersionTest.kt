/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft.
 * https://github.com/SkidderMC/FDPClient/
 * Lunar compatibility by opZywl
 * Reference: https://lucas-lima.xyz
 */
package net.ccbluex.liquidbounce.utils.client

import org.junit.Assert.*
import org.junit.Test

class JavaVersionTest {
    @Test
    fun `Lunar Zulu version retains its Java major`() {
        val version = parseJavaVersion("17.0.21.0.3")!!
        assertEquals(17, version.major)
        assertEquals(0, version.minor)
        assertEquals("21", version.patch)
    }

    @Test
    fun `legacy Forge Java 8 update remains available`() {
        val version = parseJavaVersion("1.8.0_504")!!
        assertEquals(1, version.major)
        assertEquals(8, version.minor)
        assertEquals(504, version.update)
    }

    @Test
    fun `modern build and early access suffixes are accepted`() {
        assertEquals(21, parseJavaVersion("21.0.8+9-LTS")!!.major)
        assertEquals(25, parseJavaVersion("25-ea")!!.major)
        assertEquals(17, parseJavaVersion("17")!!.major)
        assertNull(parseJavaVersion("unknown"))
    }
}
