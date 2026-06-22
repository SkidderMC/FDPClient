/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import java.awt.Color

object Glint: Module("Glint", Category.VISUAL, Category.SubCategory.RENDER_SELF, gameDetecting = false) {

    private val modeValue by choices("Mode", arrayOf("Rainbow", "Custom"), "Custom")
        .describe("Glint color mode: rainbow or custom.")
    private val color by color("Color", Color.WHITE) { modeValue == "Custom" }
        .describe("Custom color for the enchantment glint.")

    private val rainbowSpeed by float("Speed", 1.0F, 0.1F..5.0F) { modeValue == "Rainbow" }
        .describe("Speed of the rainbow glint cycle.")
    private val rainbowBrightness by float("Brightness", 1.0F, 0.5F..2.0F) { modeValue == "Rainbow" }
        .describe("Brightness of the rainbow glint.")

    fun getGlintColor(): Color {
        return when (modeValue.lowercase()) {
            "rainbow" -> {
                if (rainbowSpeed == 1.0F && rainbowBrightness == 1.0F) {
                    ColorUtils.rainbow(10, 0.9F)
                } else {
                    val hue = (System.nanoTime() + 10L) / (10000000000F / rainbowSpeed) % 1F
                    val brightness = rainbowBrightness.coerceIn(0F, 1F)
                    val rgb = Color(Color.HSBtoRGB(hue, 1F, brightness))
                    Color(rgb.red / 255F, rgb.green / 255F, rgb.blue / 255F, 0.9F)
                }
            }
            else -> Color(color.rgb)
        }
    }
}
