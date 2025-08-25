/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import org.lwjgl.opengl.GL11
import java.awt.Color

object Chams : Module("Chams", Category.VISUAL) {
    val targets by boolean("Targets", true)
    val chests by boolean("Chests", true)
    val items by boolean("Items", true)
    private val handValue by boolean("Hand", false)

    val localPlayerValue by boolean("LocalPlayer", true)
    val colorModeValue by choices("Color", arrayOf("Custom", "Fade"), "Custom") { !legacyMode }

    val texturedValue by boolean("Textured", true) { !legacyMode }
    val legacyMode by boolean("Legacy-Mode", false)

    val behindColorModeValue by choices("Behind-Color", arrayOf("Same", "Opposite", "Custom"), "Same") { !legacyMode }
    val color by color("Color", Color(0, 0, 120)) { !legacyMode && (colorModeValue == "Custom" || colorModeValue == "Fade") }
    val behindColor by color("Behind", Color(255, 0, 0)) { !legacyMode && (colorModeValue== "Custom" || colorModeValue== "Fade") && behindColorModeValue == "Custom" }

    fun preHandRender() {
        GL11.glDisable(3553)
        GL11.glEnable(3042)
        GL11.glDisable(2896)
        Color(255, 255, 255, 55)
    }

    fun postHandRender() {
        GL11.glEnable(2896)
        GL11.glEnable(3553)
        GL11.glDisable(3042)
    }

    fun shouldRenderHand(): Boolean {
        return handValue
    }
}
