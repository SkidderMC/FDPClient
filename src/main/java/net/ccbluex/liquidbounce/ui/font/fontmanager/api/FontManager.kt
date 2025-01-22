/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.font.fontmanager.api

fun interface FontManager {
    fun fontFamily(name: String): FontFamily

    fun font(name: String, size: Int): FontRenderer =
        fontFamily(name).ofSize(size)
}
