/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.rotation.point

import net.ccbluex.liquidbounce.utils.extensions.getNearestPointBB
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3

data class PointInsideBox(val pos: Vec3, val box: AxisAlignedBB) {

    fun distanceTo(point: PointInsideBox): Double = pos.distanceTo(point.pos)

    fun distanceTo(point: Vec3): Double = pos.distanceTo(point)

    fun distanceToSqr(point: PointInsideBox): Double = pos.squareDistanceTo(point.pos)

    fun distanceToSqr(point: Vec3): Double = pos.squareDistanceTo(point)

    operator fun plus(offset: Vec3): PointInsideBox =
        snapped(pos.addVector(offset.xCoord, offset.yCoord, offset.zCoord), box)

    companion object {
        fun snapped(pos: Vec3, box: AxisAlignedBB): PointInsideBox =
            PointInsideBox(getNearestPointBB(pos, box), box)
    }
}
