package net.ccbluex.liquidbounce.utils

object MouseUtils {
    @JvmStatic
    fun mouseWithinBounds(mouseX: Int, mouseY: Int, x: Float, y: Float, x2: Float, y2: Float) = mouseX >= x && mouseX < x2 && mouseY >= y && mouseY < y2
}