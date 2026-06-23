/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.math.geometry

import net.minecraft.util.Vec3
import kotlin.math.abs

const val GEOMETRY_EPSILON: Double = 1.0E-9

fun Vec3.isFiniteVector(): Boolean =
    xCoord.isFinite() && yCoord.isFinite() && zCoord.isFinite()

fun Vec3.lengthSquared(): Double = dotProduct(this)

fun Vec3.normalizedOrNull(epsilon: Double = GEOMETRY_EPSILON): Vec3? {
    require(epsilon >= 0.0 && epsilon.isFinite()) { "Epsilon must be finite and non-negative" }
    if (!isFiniteVector()) return null

    val squaredLength = lengthSquared()
    return if (squaredLength <= epsilon * epsilon) null else normalize()
}

fun Vec3.approximatelyEquals(other: Vec3, epsilon: Double = GEOMETRY_EPSILON): Boolean {
    require(epsilon >= 0.0 && epsilon.isFinite()) { "Epsilon must be finite and non-negative" }
    return abs(xCoord - other.xCoord) <= epsilon &&
        abs(yCoord - other.yCoord) <= epsilon &&
        abs(zCoord - other.zCoord) <= epsilon
}

internal fun requireFiniteVector(vector: Vec3, name: String) {
    require(vector.isFiniteVector()) { "$name must contain only finite coordinates" }
}
