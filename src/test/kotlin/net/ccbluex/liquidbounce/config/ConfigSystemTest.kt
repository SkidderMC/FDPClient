/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.config

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfigSystemTest {

    @Test
    fun `nested loading scopes restore their previous state`() {
        ConfigSystem.completeInitialLoading()
        assertFalse(ConfigSystem.isLoadingConfig)

        ConfigSystem.withConfigLoading {
            assertTrue(ConfigSystem.isLoadingConfig)
            ConfigSystem.withConfigLoading {
                assertTrue(ConfigSystem.isLoadingConfig)
            }
            assertTrue(ConfigSystem.isLoadingConfig)
        }

        assertFalse(ConfigSystem.isLoadingConfig)
    }

    @Test
    fun `failed loading scope never leaks the loading state`() {
        ConfigSystem.completeInitialLoading()

        runCatching {
            ConfigSystem.withConfigLoading {
                error("expected verification failure")
            }
        }

        assertFalse(ConfigSystem.isLoadingConfig)
    }
}
