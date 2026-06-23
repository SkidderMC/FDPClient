/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.math.geometry

import net.minecraft.util.Vec3
import kotlin.math.abs

/** Immutable convex planar polygon. Vertices may use clockwise or counter-clockwise winding. */
class Face(vertices: List<Vec3>, epsilon: Double = GEOMETRY_EPSILON) {
    val vertices: List<Vec3> = vertices.toList()
    val plane: Plane

    init {
        require(epsilon >= 0.0 && epsilon.isFinite()) { "Epsilon must be finite and non-negative" }
        require(this.vertices.size >= 3) { "A face requires at least three vertices" }
        this.vertices.forEachIndexed { index, vertex -> requireFiniteVector(vertex, "Face vertex $index") }

        val origin = this.vertices.first()
        val firstEdge = this.vertices[1].subtract(origin)
        val normal = this.vertices.asSequence()
            .drop(2)
            .map { firstEdge.crossProduct(it.subtract(origin)) }
            .firstOrNull { it.normalizedOrNull(epsilon) != null }
            ?: throw IllegalArgumentException("Face vertices must not be collinear")

        plane = Plane(origin, normal)
        require(this.vertices.all { abs(plane.signedDistance(it)) <= epsilon }) {
            "Face vertices must be coplanar"
        }
        require(isConvex(epsilon)) { "Face vertices must form a convex polygon in winding order" }
    }

    fun contains(point: Vec3, epsilon: Double = GEOMETRY_EPSILON): Boolean {
        if (!point.isFiniteVector() || abs(plane.signedDistance(point)) > epsilon) return false

        var windingSign = 0
        for (index in vertices.indices) {
            val current = vertices[index]
            val next = vertices[(index + 1) % vertices.size]
            val side = next.subtract(current).crossProduct(point.subtract(current)).dotProduct(plane.normal)
            if (abs(side) <= epsilon) continue

            val sign = if (side > 0.0) 1 else -1
            if (windingSign == 0) windingSign = sign else if (windingSign != sign) return false
        }
        return true
    }

    fun intersect(ray: Ray, epsilon: Double = GEOMETRY_EPSILON): Vec3? =
        plane.intersect(ray, epsilon)?.takeIf { contains(it, epsilon) }

    private fun isConvex(epsilon: Double): Boolean {
        var windingSign = 0
        for (index in vertices.indices) {
            val previous = vertices[(index + vertices.size - 1) % vertices.size]
            val current = vertices[index]
            val next = vertices[(index + 1) % vertices.size]
            val turn = current.subtract(previous).crossProduct(next.subtract(current)).dotProduct(plane.normal)
            if (abs(turn) <= epsilon) continue

            val sign = if (turn > 0.0) 1 else -1
            if (windingSign == 0) windingSign = sign else if (windingSign != sign) return false
        }
        return windingSign != 0
    }
}
