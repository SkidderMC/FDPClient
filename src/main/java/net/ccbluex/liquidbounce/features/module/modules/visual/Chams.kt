/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.config.boolean
import net.ccbluex.liquidbounce.config.choices
import net.ccbluex.liquidbounce.config.int
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import org.lwjgl.opengl.GL11
import java.awt.Color

object Chams : Module("Chams", Category.VISUAL, hideModule = false) {
    val targets by boolean("Targets", true)
    val chests by boolean("Chests", true)
    val items by boolean("Items", true)
    private val handValue by boolean("Hand", false)

    val localPlayerValue by boolean("LocalPlayer", true)
    val colorModeValue by choices("Color", arrayOf("Custom", "Fade"), "Custom") { !legacyMode }

    val texturedValue by boolean("Textured", true) { !legacyMode }
    val legacyMode by boolean("Legacy-Mode", false)

    val behindColorModeValue by choices("Behind-Color", arrayOf("Same", "Opposite", "Custom"), "Same") { !legacyMode }
    val redValue by int("Red", 255, 0..255) { !legacyMode && (colorModeValue == "Custom" || colorModeValue == "Fade") }
    val greenValue by int("Green", 119, 0..255) { !legacyMode && (colorModeValue == "Custom" || colorModeValue == "Fade") }
    val blueValue by int("Blue", 119, 0..255) { !legacyMode && (colorModeValue == "Custom" || colorModeValue == "Fade") }
    val alphaValue by int("Alpha", 255, 0..255) { !legacyMode && (colorModeValue == "Custom" || colorModeValue== "Fade") }
    val behindRedValue by int("BehindRed", 0, 0..255) { !legacyMode && (colorModeValue== "Custom" || colorModeValue== "Fade") && behindColorModeValue == "Custom" }
    val behindGreenValue by int("BehindGreen", 223, 0..255) { !legacyMode && (colorModeValue == "Custom" || colorModeValue == "Fade") && behindColorModeValue == "Custom" }
    val behindBlueValue by int("BehindBlue", 255, 0..255) { !legacyMode && (colorModeValue == "Custom" || colorModeValue == "Fade") && behindColorModeValue == "Custom" }
    val behindAlphaValue by int("BehindAlpha", 255, 0..255) { !legacyMode && (colorModeValue == "Custom" || colorModeValue == "Fade") && behindColorModeValue == "Custom" }

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
