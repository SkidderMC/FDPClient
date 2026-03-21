/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

/**
 * Settings module
 *
 * Removes limits from all module settings, allowing users to set any value without bounds checking.
 * This enables extreme configurations like AutoClicker CPS 100-100 or KillAura CPS 200-200.
 *
 * WARNING: Use with caution! Removing limits may cause unexpected behavior or performance issues.
 */
object Settings : Module("Settings", Category.OTHER, Category.SubCategory.MISCELLANEOUS, gameDetecting = false) {

    val removeLimits by boolean("RemoveLimits", true)

    override val tag: String
        get() = if (removeLimits) "Unlimited" else "Limited"
}
