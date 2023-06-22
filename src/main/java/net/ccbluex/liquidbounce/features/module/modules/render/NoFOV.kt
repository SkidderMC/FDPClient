/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.value.FloatValue

object NoFOV : Module(name = "NoFOV", category = ModuleCategory.RENDER, defaultOn = false) {
    val fovValue = FloatValue("FOV", 1f, 0f, 1.5f)
}
