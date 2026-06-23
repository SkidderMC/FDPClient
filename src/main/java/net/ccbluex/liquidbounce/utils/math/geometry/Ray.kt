/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.math.geometry

import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import kotlin.math.max
import kotlin.math.min

/** Half-line whose valid parameters are greater than or equal to zero. */
class Ray(origin: Vec3, direction: Vec3) {
    private val backingLine = Line(origin, direction)

    val origin: Vec3 get() = backingLine.origin
    val direction: Vec3 get() = backingLine.direction

    fun pointAt(distance: Double): Vec3 {
        require(distance >= 0.0) { "Ray distance must be non-negative" }
        return backingLine.pointAt(distance)
    }

    /** Returns the entry distance into [box], or `null` when the ray misses it. */
    fun intersectionDistance(box: AxisAlignedBB, epsilon: Double = GEOMETRY_EPSILON): Double? {
        require(epsilon >= 0.0 && epsilon.isFinite()) { "Epsilon must be finite and non-negative" }

        var entry = 0.0
        var exit = Double.POSITIVE_INFINITY

        fun includeSlab(originValue: Double, directionValue: Double, lower: Double, upper: Double): Boolean {
            if (kotlin.math.abs(directionValue) <= epsilon) {
                return originValue >= lower - epsilon && originValue <= upper + epsilon
            }

            var first = (lower - originValue) / directionValue
            var second = (upper - originValue) / directionValue
            if (first > second) {
                val swap = first
                first = second
                second = swap
            }

            entry = max(entry, first)
            exit = min(exit, second)
            return entry <= exit + epsilon
        }

        if (!includeSlab(origin.xCoord, direction.xCoord, box.minX, box.maxX)) return null
        if (!includeSlab(origin.yCoord, direction.yCoord, box.minY, box.maxY)) return null
        if (!includeSlab(origin.zCoord, direction.zCoord, box.minZ, box.maxZ)) return null

        return entry.takeIf { exit >= -epsilon }
    }

    fun intersect(box: AxisAlignedBB, epsilon: Double = GEOMETRY_EPSILON): Vec3? =
        intersectionDistance(box, epsilon)?.let(::pointAt)
}
