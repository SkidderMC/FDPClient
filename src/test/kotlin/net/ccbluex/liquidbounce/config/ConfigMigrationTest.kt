/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.config

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression coverage for issue #1566: a legacy FDPClient b16 profile stores every KillAura
 * setting as a flat key, but the current build nests the AutoBlock settings under an
 * "AutoBlocking" group. Loading the old profile must still apply those grouped values instead
 * of leaving them at their defaults.
 */
class ConfigMigrationTest {

    /** Rebuilds the shape of KillAura's grouped AutoBlock subtree without a Minecraft runtime. */
    private fun killAuraLikeModule(): Configurable = Configurable("KillAura").apply {
        choices("AutoBlock", arrayOf("Off", "Packet", "Fake"), "Off")
        boolean("ReleaseAutoBlock", true)
        boolean("ForceBlockRender", true)
        boolean("IgnoreTickRule", false)
        boolean("BlinkAutoBlock", false)
        boolean("SmartAutoBlock", false)
        boolean("ForceBlockWhenStill", true)

        val smart = group("SmartAutoBlock", "SmartAutoBlock", "ForceBlockWhenStill")
        val autoBlocking = group(
            "AutoBlocking",
            "AutoBlock", "ReleaseAutoBlock", "ForceBlockRender", "IgnoreTickRule", "BlinkAutoBlock"
        )
        autoBlocking.addValue(smart)
    }

    /** Resolves the leaf boolean by name, skipping a homonymous group (e.g. the SmartAutoBlock group). */
    private fun bool(configurable: Configurable, name: String): Boolean {
        fun search(container: Configurable): BoolValue? {
            for (value in container.values) {
                if (value is BoolValue && value.matchesKey(name)) return value
                if (value is Configurable) search(value)?.let { return it }
            }
            return null
        }
        return search(configurable)!!.get()
    }

    private fun load(module: Configurable, flatProfile: String) {
        val legacy = JsonParser().parse(flatProfile).asJsonObject
        ConfigSystem.withConfigLoading {
            for (value in module.values) {
                ConfigMigration.findValue(legacy, value)?.let { value.fromJson(it) }
            }
        }
    }

    @Test
    fun `legacy flat KillAura profile restores grouped AutoBlock settings`() {
        ConfigSystem.completeInitialLoading()
        val module = killAuraLikeModule()

        // Exactly the combination from the report: IgnoreTickRule enabled, BlinkAutoBlock disabled.
        load(
            module,
            """
            {
              "AutoBlock": "Packet",
              "ReleaseAutoBlock": true,
              "ForceBlockRender": true,
              "IgnoreTickRule": true,
              "BlinkAutoBlock": false,
              "SmartAutoBlock": true,
              "ForceBlockWhenStill": false
            }
            """.trimIndent()
        )

        assertEquals("Packet", module.findDeep("AutoBlock")?.get())
        // The two settings called out in the report must both survive the load, in both directions.
        assertTrue("IgnoreTickRule should load as enabled", bool(module, "IgnoreTickRule"))
        assertFalse("BlinkAutoBlock should load as disabled", bool(module, "BlinkAutoBlock"))
        // The homonymous nested group ("SmartAutoBlock" group vs bool) must not shadow the leaf.
        assertTrue("SmartAutoBlock should load as enabled", bool(module, "SmartAutoBlock"))
        assertFalse("ForceBlockWhenStill should load as disabled", bool(module, "ForceBlockWhenStill"))
    }

    @Test
    fun `current nested KillAura profile still loads unchanged`() {
        ConfigSystem.completeInitialLoading()
        val module = killAuraLikeModule()

        load(
            module,
            """
            {
              "AutoBlocking": {
                "AutoBlock": "Fake",
                "ReleaseAutoBlock": false,
                "ForceBlockRender": false,
                "IgnoreTickRule": true,
                "BlinkAutoBlock": true,
                "SmartAutoBlock": {
                  "SmartAutoBlock": true,
                  "ForceBlockWhenStill": false
                }
              }
            }
            """.trimIndent()
        )

        assertEquals("Fake", module.findDeep("AutoBlock")?.get())
        assertTrue(bool(module, "IgnoreTickRule"))
        assertTrue(bool(module, "BlinkAutoBlock"))
        assertTrue(bool(module, "SmartAutoBlock"))
        assertFalse(bool(module, "ForceBlockWhenStill"))
    }
}
