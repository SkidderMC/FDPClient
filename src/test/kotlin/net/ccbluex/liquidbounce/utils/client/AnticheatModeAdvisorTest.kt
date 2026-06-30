/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.client

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class AnticheatModeAdvisorTest {

    @Test
    fun `auto profile resolves observed anti cheats`() {
        assertEquals(AnticheatProfile.GRIM, AnticheatModeAdvisor.resolve("Auto", "GrimAC"))
        assertEquals(AnticheatProfile.NCP, AnticheatModeAdvisor.resolve("Auto", "Watchdog"))
        assertEquals(AnticheatProfile.VULCAN, AnticheatModeAdvisor.resolve("Auto", "Vulcan"))
    }

    @Test
    fun `explicit profile filters unsafe legacy modes`() {
        val modes = arrayOf("Simple", "Grim", "GrimVertical", "Cancel")
        assertArrayEquals(
            arrayOf("Grim", "GrimVertical"),
            AnticheatModeAdvisor.filteredModes("Velocity", "Grim", null, modes)
        )
        assertEquals(
            ModeRisk.LIKELY_DETECTED,
            AnticheatModeAdvisor.assess("Criticals", "Packet", "Grim", null).risk
        )
    }
}
