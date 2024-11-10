/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.RenderWings
import net.ccbluex.liquidbounce.value.*

object Wings : Module("Wings", Category.CLIENT, hideModule = false) {
    private val onlyThirdPerson by boolean("OnlyThirdPerson", true)
    val colorType by choices("Color Type", arrayOf("Custom", "Chroma", "None"), "Chroma")
    val customRed by int("Red", 255, 0.. 255) { colorType == "Custom" }
    val customGreen by int("Green", 255, 0.. 255) { colorType == "Custom" }
    val customBlue by int("Blue", 255, 0.. 255) { colorType == "Custom" }
    val wingStyle by choices("Wing Style", arrayOf("Dragon", "Simple"), "Dragon")

    @EventTarget
    fun onRenderPlayer(event: Render3DEvent) {
        if (onlyThirdPerson && mc.gameSettings.thirdPersonView == 0) return
        val renderWings = RenderWings()
        renderWings.renderWings(event.partialTicks)
    }
}