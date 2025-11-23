package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Animation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Direction

class DecelerateAnimation @JvmOverloads constructor(
    ms: Int,
    endPoint: Double,
    direction: Direction = Direction.FORWARDS
) : Animation(ms, endPoint, direction) {

    override fun getEquation(x: Double): Double {
        val x1 = x / duration
        return 1 - (x1 - 1) * (x1 - 1)
    }
}
