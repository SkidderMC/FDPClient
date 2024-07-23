/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.value.BoolValue

object Chams : Module("Chams", Category.VISUAL, hideModule = false) {
    val targets by BoolValue("Targets", true)
    val chests by BoolValue("Chests", true)
    val items by BoolValue("Items", true)
}
