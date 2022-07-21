/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import org.lwjgl.opengl.GL11

@ModuleInfo(name = "Chams", category = ModuleCategory.RENDER)
class Chams : Module() {
    val targetsValue = BoolValue("Targets", true)
    val chestsValue = BoolValue("Chests", true)
    val itemsValue = BoolValue("Items", true)

    val legacyMode = BoolValue("Legacy-Mode", true)
    val texturedValue = BoolValue("Textured", true)
    val colorModeValue = ListValue("Color", arrayOf("Custom", "Slowly", "AnotherRainbow"), "Custom")
    val localPlayerValue = BoolValue("LocalPlayer", true)
    val behindColorModeValue = ListValue("Behind-Color", arrayOf("Same", "Opposite", "Red"), "Same")
    val colorRainbowValue = BoolValue("Rainbow", false)
    val colorRedValue = IntegerValue("Red", 255, 0, 255).displayable { !colorRainbowValue.get() }
    val colorGreenValue = IntegerValue("Green", 255, 0, 255).displayable { !colorRainbowValue.get() }
    val colorBlueValue = IntegerValue("Blue", 255, 0, 255).displayable { !colorRainbowValue.get() }
    val colorAlphaValue = IntegerValue("Alpha", 200, 0, 255)
    val saturationValue = FloatValue("Saturation", 1F, 0F, 1F)
    val brightnessValue = FloatValue("Brightness", 1F, 0F, 1F)

    fun setColor() {
        if (colorRainbowValue.get()) {
            RenderUtils.glColor(ColorUtils.rainbowWithAlpha(colorAlphaValue.get()))
        } else {
            GL11.glColor4f(colorRedValue.get() / 255f, colorGreenValue.get() / 255f, colorBlueValue.get() / 255f, colorAlphaValue.get() / 255f)
        }
    }
}
