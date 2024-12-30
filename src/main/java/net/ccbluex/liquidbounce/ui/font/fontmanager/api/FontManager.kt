/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.font.fontmanager.api

fun interface FontManager {
    fun fontFamily(fontType: FontType): FontFamily

    fun font(fontType: FontType, size: Int): FontRenderer =
        fontFamily(fontType).ofSize(size)
}
