/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.rotation.point.features

import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextFloat
import net.ccbluex.liquidbounce.utils.rotation.point.PointInsideBox

class PointProcessorLazy : PointProcessor("Lazy", false) {

    private val threshold by gatedFloatRange("Threshold", 0.1f..0.2f, 0.01f..0.4f, "m")

    private var currentThreshold: Float = rollThreshold()
    private var currentPoint: PointInsideBox? = null

    private fun rollThreshold(): Float = nextFloat(threshold.start, threshold.endInclusive)

    override fun process(point: PointInsideBox): PointInsideBox {
        val held = currentPoint ?: run {
            this.currentPoint = point
            return point
        }

        val distSqr = point.distanceToSqr(held)
        val thresholdSqr = (currentThreshold * currentThreshold).toDouble()

        if (distSqr < thresholdSqr) {
            return held
        }

        this.currentPoint = point
        this.currentThreshold = rollThreshold()
        return held
    }
}
