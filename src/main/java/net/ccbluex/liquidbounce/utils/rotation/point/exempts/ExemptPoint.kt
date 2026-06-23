/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.rotation.point.exempts

import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3

data class ExemptContext(val box: AxisAlignedBB, val bestHitVector: Vec3, val worstHitVector: Vec3)

interface ExemptPoint {
    fun predicate(context: ExemptContext, point: Vec3): Boolean
}
