/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.simulation

import net.minecraft.entity.Entity
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.util.Vec3
import kotlin.math.sqrt

/**
 * Steps an arrow forward through vanilla 1.8.9 flight physics (0.99 air drag, 0.05 gravity)
 * without touching the world, so modules can reason about where a shot is going before it
 * lands. Shared by anything that reacts to arrows — dodging, aiming, alerts.
 */
object SimulatedArrow {

    private const val DRAG = 0.99
    private const val GRAVITY = 0.05

    /**
     * Closest the arrow's predicted path comes to [target]'s body over the next [maxTicks]
     * ticks, in blocks. Compares against the target's vertical feet-to-eyes segment.
     */
    fun closestApproach(arrow: EntityArrow, target: Entity, maxTicks: Int = 40): Double {
        var px = arrow.posX
        var py = arrow.posY
        var pz = arrow.posZ
        var mx = arrow.motionX
        var my = arrow.motionY
        var mz = arrow.motionZ

        val tx = target.posX
        val tz = target.posZ
        val tFeet = target.posY
        val tHead = target.posY + target.eyeHeight

        var best = Double.MAX_VALUE
        repeat(maxTicks) {
            px += mx
            py += my
            pz += mz
            mx *= DRAG
            my *= DRAG
            mz *= DRAG
            my -= GRAVITY

            val clampedY = py.coerceIn(tFeet, tHead.toDouble())
            val dx = px - tx
            val dy = py - clampedY
            val dz = pz - tz
            val d = dx * dx + dy * dy + dz * dz
            if (d < best) {
                best = d
            }
        }
        return sqrt(best)
    }

    /**
     * Whether the arrow is predicted to pass within [radius] blocks of [target].
     */
    fun willHit(arrow: EntityArrow, target: Entity, maxTicks: Int = 40, radius: Double = 1.0): Boolean =
        closestApproach(arrow, target, maxTicks) <= radius

    /** Immutable trajectory samples, including the arrow's current position. */
    fun trace(arrow: EntityArrow, maxTicks: Int = 40): List<Vec3> {
        require(maxTicks in 1..200) { "Arrow prediction must stay within 1..200 ticks" }
        var px = arrow.posX
        var py = arrow.posY
        var pz = arrow.posZ
        var mx = arrow.motionX
        var my = arrow.motionY
        var mz = arrow.motionZ
        return ArrayList<Vec3>(maxTicks + 1).apply {
            add(Vec3(px, py, pz))
            repeat(maxTicks) {
                px += mx
                py += my
                pz += mz
                add(Vec3(px, py, pz))
                mx *= DRAG
                my = my * DRAG - GRAVITY
                mz *= DRAG
            }
        }
    }
}
