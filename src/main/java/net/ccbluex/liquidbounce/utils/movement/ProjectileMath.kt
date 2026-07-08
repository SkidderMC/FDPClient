/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.movement

import net.minecraft.util.Vec3

fun distanceSqPointToSegment(point: Vec3, start: Vec3, end: Vec3): Double {
    val abX = end.xCoord - start.xCoord
    val abY = end.yCoord - start.yCoord
    val abZ = end.zCoord - start.zCoord
    val lengthSq = abX * abX + abY * abY + abZ * abZ

    if (lengthSq == 0.0) return point.squareDistanceTo(start)

    val projectionFactor = (
        (point.xCoord - start.xCoord) * abX +
            (point.yCoord - start.yCoord) * abY +
            (point.zCoord - start.zCoord) * abZ
        ) / lengthSq
    val clamped = projectionFactor.coerceIn(0.0, 1.0)
    val projection = Vec3(
        start.xCoord + abX * clamped,
        start.yCoord + abY * clamped,
        start.zCoord + abZ * clamped,
    )
    return point.squareDistanceTo(projection)
}
