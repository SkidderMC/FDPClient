package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Animation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Direction

class EaseInOutQuad @JvmOverloads constructor(
    ms: Int,
    endPoint: Double,
    direction: Direction = Direction.FORWARDS
) : Animation(ms, endPoint, direction) {

    override fun getEquation(x1: Double): Double {
        val x = x1 / duration
        return if (x < 0.5) 2 * Math.pow(x, 2.0) else 1 - Math.pow(-2 * x + 2, 2.0) / 2
    }
}
