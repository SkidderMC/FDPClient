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
    private val color by color("Color", Color.WHITE) { modeValue == "Custom" }

    fun getGlintColor(): Color {
        return when (modeValue.lowercase()) {
            "rainbow" -> ColorUtils.rainbow(10, 0.9F)
            else -> Color(color.rgb)
        }
    }
}