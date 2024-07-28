/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import org.lwjgl.opengl.GL11
import java.awt.Color


object Chams : Module("Chams", Category.VISUAL, hideModule = false) {
    val targets by BoolValue("Targets", true)
    val chests by BoolValue("Chests", true)
    val items by BoolValue("Items", true)
    private val handValue by BoolValue("Hand", false)

    val localPlayerValue by BoolValue("LocalPlayer", true)
    val colorModeValue by ListValue("Color", arrayOf("Custom", "Fade"), "Custom") { !legacyMode }

    val texturedValue by BoolValue("Textured", true) { !legacyMode }
    val legacyMode by BoolValue("Legacy-Mode", false)

    val behindColorModeValue by ListValue("Behind-Color", arrayOf("Same", "Opposite", "Custom"), "Same") { !legacyMode }
    val redValue by IntegerValue("Red", 255, 0..255) { !legacyMode && (colorModeValue == "Custom" || colorModeValue == "Fade") }
    val greenValue by IntegerValue("Green", 119, 0..255) { !legacyMode && (colorModeValue == "Custom" || colorModeValue == "Fade") }
    val blueValue by IntegerValue("Blue", 119, 0..255) { !legacyMode && (colorModeValue == "Custom" || colorModeValue == "Fade") }
    val alphaValue by IntegerValue("Alpha", 255, 0..255) { !legacyMode && (colorModeValue == "Custom" || colorModeValue== "Fade") }
    val behindRedValue by IntegerValue("BehindRed", 0, 0..255) { !legacyMode && (colorModeValue== "Custom" || colorModeValue== "Fade") && behindColorModeValue == "Custom" }
    val behindGreenValue by IntegerValue("BehindGreen", 223, 0..255) { !legacyMode && (colorModeValue == "Custom" || colorModeValue == "Fade") && behindColorModeValue == "Custom" }
    val behindBlueValue by IntegerValue("BehindBlue", 255, 0..255) { !legacyMode && (colorModeValue == "Custom" || colorModeValue == "Fade") && behindColorModeValue == "Custom" }
    val behindAlphaValue by IntegerValue("BehindAlpha", 255, 0..255) { !legacyMode && (colorModeValue == "Custom" || colorModeValue == "Fade") && behindColorModeValue == "Custom" }

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
