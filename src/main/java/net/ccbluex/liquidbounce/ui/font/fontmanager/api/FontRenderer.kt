/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.font.fontmanager.api

interface FontRenderer {

    fun drawString(text: CharSequence, x: Float, y: Float, color: Int, dropShadow: Boolean): Float
    fun drawString(text: CharSequence, x: Double, y: Double, color: Int, dropShadow: Boolean): Float
    fun trimStringToWidth(text: CharSequence, width: Int, reverse: Boolean): String
    fun stringWidth(text: CharSequence): Int
    fun charWidth(ch: Char): Float

    val name: String
    val height: Int
    val isAntiAlias: Boolean
    val isFractionalMetrics: Boolean

    fun drawString(text: CharSequence, x: Float, y: Float, color: Int): Float =
        drawString(text, x, y, color, false)

    fun drawString(text: CharSequence, x: Int, y: Int, color: Int): Float =
        drawString(text, x.toFloat(), y.toFloat(), color, false)

    fun trimStringToWidth(text: CharSequence, width: Int): String =
        trimStringToWidth(text, width, false)

    fun drawCenteredString(
        text: CharSequence,
        x: Float,
        y: Float,
        color: Int,
        dropShadow: Boolean
    ): Float {
        return drawString(text, x - stringWidth(text) / 2.0f, y, color, dropShadow)
    }

    fun getMiddleOfBox(boxHeight: Float): Float =
        boxHeight / 2f - height / 2f

    fun drawCenteredString(text: CharSequence, x: Float, y: Float, color: Int): Float =
        drawCenteredString(text, x, y, color, false)

    fun drawCenteredStringShadow(text: CharSequence, x: Float, y: Float, color: Int): Float =
        drawCenteredString(text, x, y, color, true)
}
