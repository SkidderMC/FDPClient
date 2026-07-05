/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfigurableLookupTest {

    @Test
    fun `homonymous leaf wins over its containing group`() {
        val root = Configurable("Root")
        val toggle = root.boolean("SmartAutoBlock", false)
        val group = root.group("SmartAutoBlock", "SmartAutoBlock")

        assertSame(toggle, root.findDeep("SmartAutoBlock"))
        assertSame(toggle, group.findDeep("SmartAutoBlock"))
    }

    @Test
    fun `group remains addressable when it has no homonymous leaf`() {
        val root = Configurable("Root")
        val child = Configurable("Rotations").also(root::addValue)
        child.boolean("Enabled", true)

        assertSame(child, root.findDeep("Rotations"))
    }

    @Test
    fun `lookup is case insensitive at every depth`() {
        val root = Configurable("Root")
        val group = Configurable("Targeting").also(root::addValue)
        val range = group.float("Range", 3.5F, 1F..6F)

        assertSame(range, root.findDeep("rAnGe"))
        assertEquals(3.5F, range.get())
        assertTrue(range.owner === group)
    }

}
