/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.rotation.point.exempts

import net.ccbluex.liquidbounce.config.ToggleableValueGroup
import net.minecraft.util.Vec3
import kotlin.math.abs
import kotlin.math.hypot

class ExemptBestHitVector : ToggleableValueGroup("ExemptBestHitVector", false), ExemptPoint {

    private val vertical by gatedFloat("Vertical", 0.2f, 0.0f..1f)
    private val horizontal by gatedFloat("Horizontal", 0.1f, 0.0f..1f)

    override fun predicate(context: ExemptContext, point: Vec3): Boolean {
        if (!enabled) return false

        val target = context.bestHitVector
        val verticalDistance = abs(point.yCoord - target.yCoord)
        val horizontalDistance = hypot(point.xCoord - target.xCoord, point.zCoord - target.zCoord)

        return horizontalDistance <= horizontal.toDouble() && verticalDistance <= vertical.toDouble()
    }
}
