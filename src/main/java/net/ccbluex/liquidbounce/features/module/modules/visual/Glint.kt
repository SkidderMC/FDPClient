/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import java.awt.Color

object Glint: Module("Glint", Category.VISUAL, hideModule = false) {

    private val modeValue by ListValue("Mode", arrayOf("Rainbow", "Custom"), "Custom")
    private val redValue by IntegerValue("Red", 255, 0.. 255) { modeValue == "Custom" }
    private val greenValue by IntegerValue("Green", 0, 0.. 255) { modeValue == "Custom" }
    private val blueValue by IntegerValue("Blue", 0, 0.. 255) { modeValue == "Custom" }

    fun getColor(): Color {
        return when (modeValue.lowercase()) {
            "rainbow" -> ColorUtils.rainbow(10, 0.9F)
            else -> Color(redValue, greenValue, blueValue)
        }
    }
}