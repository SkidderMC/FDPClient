/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue

@ModuleInfo(name = "Chams", category = ModuleCategory.RENDER)
class Chams : Module() {
    val targetsValue = BoolValue("Targets", true)
    val chestsValue = BoolValue("Chests", true)
    val itemsValue = BoolValue("Items", true)

    val localPlayerValue = BoolValue("LocalPlayer", true)
    val legacyMode = BoolValue("Legacy-Mode", false)
    val texturedValue = BoolValue("Textured", false).displayable { legacyMode.get() }
    val colorModeValue = ListValue("Color", arrayOf("Custom", "Slowly", "Fade"), "Custom").displayable { legacyMode.get() }
    val behindColorModeValue = ListValue("Behind-Color", arrayOf("Same", "Opposite", "Red"), "Red").displayable { legacyMode.get() }
    val redValue = IntegerValue("Red", 0, 0, 255).displayable { legacyMode.get() && (colorModeValue.equals("Custom") || colorModeValue.equals("Fade")) }
    val greenValue = IntegerValue("Green", 200, 0, 255).displayable { legacyMode.get() && (colorModeValue.equals("Custom") || colorModeValue.equals("Fade")) }
    val blueValue = IntegerValue("Blue", 0, 0, 255).displayable { legacyMode.get() && (colorModeValue.equals("Custom") || colorModeValue.equals("Fade")) }
    val alphaValue = IntegerValue("Alpha", 255, 0, 255).displayable { legacyMode.get() }
    val saturationValue = FloatValue("Saturation", 1F, 0F, 1F).displayable { legacyMode.get() && colorModeValue.equals("Slowly") }
    val brightnessValue = FloatValue("Brightness", 1F, 0F, 1F).displayable { legacyMode.get() && colorModeValue.equals("Slowly") }
}