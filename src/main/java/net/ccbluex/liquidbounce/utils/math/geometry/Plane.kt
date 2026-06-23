/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.math.geometry

import net.minecraft.util.Vec3
import kotlin.math.abs

/** Infinite plane represented by one point and a normalized normal. */
class Plane(point: Vec3, normal: Vec3) {
    val point: Vec3 = point.also { requireFiniteVector(it, "Plane point") }
    val normal: Vec3 = normal.normalizedOrNull()
        ?: throw IllegalArgumentException("Plane normal must be finite and non-zero")

    fun signedDistance(point: Vec3): Double {
        requireFiniteVector(point, "Distance point")
        return point.subtract(this.point).dotProduct(normal)
    }

    fun project(point: Vec3): Vec3 {
        val distance = signedDistance(point)
        return point.addVector(-normal.xCoord * distance, -normal.yCoord * distance, -normal.zCoord * distance)
    }

    fun intersectionParameter(line: Line, epsilon: Double = GEOMETRY_EPSILON): Double? {
        require(epsilon >= 0.0 && epsilon.isFinite()) { "Epsilon must be finite and non-negative" }
        val denominator = normal.dotProduct(line.direction)
        if (abs(denominator) <= epsilon) return null
        return normal.dotProduct(point.subtract(line.origin)) / denominator
    }

    fun intersect(line: Line, epsilon: Double = GEOMETRY_EPSILON): Vec3? =
        intersectionParameter(line, epsilon)?.let(line::pointAt)

    fun intersectionDistance(ray: Ray, epsilon: Double = GEOMETRY_EPSILON): Double? {
        val denominator = normal.dotProduct(ray.direction)
        if (abs(denominator) <= epsilon) return null

        val distance = normal.dotProduct(point.subtract(ray.origin)) / denominator
        return distance.takeIf { it >= -epsilon }?.coerceAtLeast(0.0)
    }

    fun intersect(ray: Ray, epsilon: Double = GEOMETRY_EPSILON): Vec3? =
        intersectionDistance(ray, epsilon)?.let(ray::pointAt)
}
