/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.value.boolean

object TrueSight : Module("TrueSight", Category.VISUAL) {
    val barriers by boolean("Barriers", true)
    val entities by boolean("Entities", true)

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (barriers && mc.gameSettings.particleSetting == 2) {
            mc.gameSettings.particleSetting = 1
        }
    }
}