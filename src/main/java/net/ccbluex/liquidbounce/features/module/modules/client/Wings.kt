/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.render.RenderWings
import java.awt.Color

object Wings : Module("Wings", Category.CLIENT) {
    private val onlyThirdPerson by boolean("OnlyThirdPerson", true)
    val colorType by choices("Color Type", arrayOf("Custom", "Theme", "None"), "Custom")

    val color by color("Color", Color(0xFF0054)) { colorType == "Custom" }

    val wingStyle by choices("Wing Style", arrayOf("Dragon", "Simple"), "Dragon")

    val onRender3D = handler<Render3DEvent> { event ->
        if (onlyThirdPerson && mc.gameSettings.thirdPersonView == 0) return@handler
        val renderWings = RenderWings()
        renderWings.renderWings(event.partialTicks)
    }
}