/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue

object AntiBlind : Module("AntiBlind", Category.VISUAL, gameDetecting = false, hideModule = false) {
    val confusionEffect by BoolValue("Confusion", true)
    val pumpkinEffect by BoolValue("Pumpkin", true)
    val fireEffect by FloatValue("FireAlpha", 0.3f, 0f..1f)
    val bossHealth by BoolValue("BossHealth", true)
}