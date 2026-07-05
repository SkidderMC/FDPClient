/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.utils.client.ClassUtils
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Links the complete built-in module surface without initializing Minecraft-dependent objects.
 * Incompatible bytecode and missing transitive classes become CI failures instead of a partial
 * module list at runtime.
 */
class ModuleRegistryAuditTest {

    @Test
    fun `all built-in module classes link on the supported Java runtime`() {
        val moduleClasses = ClassUtils.resolvePackage(
            "net.ccbluex.liquidbounce.features.module.modules",
            Module::class.java
        )
        assertEquals("The audited built-in module inventory changed", EXPECTED_MODULE_COUNT, moduleClasses.size)
    }

    private companion object {
        const val EXPECTED_MODULE_COUNT = 297
    }
}
