/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.value.BoolValue

object NoSlowBreak : Module(name = "NoSlowBreak", category = ModuleCategory.WORLD, defaultOn = false) {
    val airValue = BoolValue("Air", true)
    val waterValue = BoolValue("Water", false)
}
