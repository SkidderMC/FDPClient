/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.core

/**
 * Pure text helpers shared by every style.
 */
object TextUtil {

    /**
     * Truncates [text] to [maxLen] characters followed by an ellipsis when it is
     * longer than [maxLen]; otherwise returns [text] unchanged.
     */
    fun abbreviate(text: String, maxLen: Int): String =
        if (text.length > maxLen) text.substring(0, maxLen) + "..." else text
}
