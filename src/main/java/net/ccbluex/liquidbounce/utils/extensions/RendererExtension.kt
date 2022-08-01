package net.ccbluex.liquidbounce.utils.extensions

import net.minecraft.client.gui.FontRenderer

fun FontRenderer.drawCenteredString(s: String, x: Float, y: Float, color: Int, shadow: Boolean) = drawString(s, x - getStringWidth(s) / 2F, y, color, shadow)

fun FontRenderer.drawCenteredString(s: String, x: Float, y: Float, color: Int) =
    drawString(s, x - getStringWidth(s) / 2F, y, color, false)