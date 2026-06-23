/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.rotation.point.features

import net.ccbluex.liquidbounce.utils.rotation.point.PointInsideBox

class PointProcessorDelay : PointProcessor("Delay", false) {

    private val delay = gatedIntRange("Delay", 2..4, 0..5, "ticks")

    private var currentDelay: Int = delay.random
    private var currentPoint: PointInsideBox? = null

    override fun process(point: PointInsideBox): PointInsideBox {
        if (point == currentPoint) {
            return point
        }

        val held = currentPoint ?: run {
            this.currentPoint = point
            return point
        }

        currentDelay--
        if (currentDelay > 0) {
            return held
        }

        this.currentPoint = point
        this.currentDelay = delay.random
        return held
    }
}
