/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

/**
 * UnlimitedValues module
 *
 * Removes limits from all module settings, allowing users to set any value without bounds checking.
 * This enables extreme configurations like AutoClicker CPS 100-100 or KillAura CPS 200-200.
 *
 * Usage: .unlimitedvalues or enable via GUI
 * Then set any value: .autoclicker cps 100-100
 *
 * WARNING: Use with caution! Removing limits may cause unexpected behavior or performance issues.
 */
object UnlimitedValues : Module("UnlimitedValues", Category.OTHER, Category.SubCategory.MISCELLANEOUS, gameDetecting = false) {

    val removeLimits by boolean("RemoveLimits", true)

    override val tag: String
        get() = if (removeLimits) "ON" else "OFF"
}
