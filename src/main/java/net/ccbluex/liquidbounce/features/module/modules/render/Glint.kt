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
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import java.awt.Color

@ModuleInfo(name = "Glint", category = ModuleCategory.RENDER)
class Glint : Module() {

    private val modeValue = ListValue("Mode", arrayOf("Rainbow", "AnotherRainbow", "Custom"), "Custom")
    private val redValue = IntegerValue("Red", 255, 0, 255).displayable { modeValue.equals("Custom") }
    private val greenValue = IntegerValue("Green", 0, 0, 255).displayable { modeValue.equals("Custom") }
    private val blueValue = IntegerValue("Blue", 0, 0, 255).displayable { modeValue.equals("Custom") }

    fun getColor(): Color {
        return when (modeValue.get().lowercase()) {
            "rainbow" -> ColorUtils.rainbow()
            "anotherrainbow" -> ColorUtils.skyRainbow(10, 0.9F, 1F, 1.0)
            else -> Color(redValue.get(), greenValue.get(), blueValue.get())
        }
    }
}