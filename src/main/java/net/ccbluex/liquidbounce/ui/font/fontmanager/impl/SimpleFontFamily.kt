/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.font.fontmanager.impl

import net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontFamily
import net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontRenderer
import net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontType
import java.awt.Font

class SimpleFontFamily private constructor(
    override val font: FontType,
    private val awtFont: Font
) : FontFamily {

    override fun ofSize(size: Int): FontRenderer {
        return SimpleFontRenderer.create(awtFont.deriveFont(Font.PLAIN, size.toFloat()))
    }

    companion object {
        fun create(fontType: FontType, awtFont: Font): FontFamily =
            SimpleFontFamily(fontType, awtFont)
    }
}
