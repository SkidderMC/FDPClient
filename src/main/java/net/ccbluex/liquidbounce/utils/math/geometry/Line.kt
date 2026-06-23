/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.math.geometry

import net.minecraft.util.Vec3

/** Infinite three-dimensional line with a normalized direction. */
class Line(origin: Vec3, direction: Vec3) {
    val origin: Vec3 = origin.also { requireFiniteVector(it, "Line origin") }
    val direction: Vec3 = direction.normalizedOrNull()
        ?: throw IllegalArgumentException("Line direction must be finite and non-zero")

    fun pointAt(parameter: Double): Vec3 {
        require(parameter.isFinite()) { "Line parameter must be finite" }
        return origin.addVector(
            direction.xCoord * parameter,
            direction.yCoord * parameter,
            direction.zCoord * parameter
        )
    }

    fun projectionParameter(point: Vec3): Double {
        requireFiniteVector(point, "Projected point")
        return point.subtract(origin).dotProduct(direction)
    }

    fun project(point: Vec3): Vec3 = pointAt(projectionParameter(point))

    fun distanceSquared(point: Vec3): Double = point.squareDistanceTo(project(point))
}
