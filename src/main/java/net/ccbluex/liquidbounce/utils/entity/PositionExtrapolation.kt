/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.entity

import net.ccbluex.liquidbounce.utils.extensions.currPos
import net.ccbluex.liquidbounce.utils.extensions.prevPos
import net.ccbluex.liquidbounce.utils.simulation.PlayerSimulation
import net.ccbluex.liquidbounce.utils.simulation.PlayerSimulationCache
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.Vec3
import kotlin.math.floor

/** Predicts the world position of an entity a fractional number of ticks from now. */
fun interface PositionExtrapolation {
    fun getPositionInTicks(ticks: Double): Vec3

    companion object {
        @JvmStatic
        fun getBestForEntity(entity: Entity): PositionExtrapolation =
            if (entity is EntityPlayer) PlayerPositionExtrapolation(entity) else LinearPositionExtrapolation(entity)

        @JvmStatic
        fun constant(position: Vec3) = PositionExtrapolation { position }
    }
}

class LinearPositionExtrapolation(
    private val basePosition: Vec3,
    private val velocity: Vec3,
) : PositionExtrapolation {
    constructor(entity: Entity) : this(entity.currPos, entity.currPos.subtract(entity.prevPos))

    override fun getPositionInTicks(ticks: Double) = basePosition.addVector(
        velocity.xCoord * ticks,
        velocity.yCoord * ticks,
        velocity.zCoord * ticks,
    )
}

/**
 * Collision-aware protocol-47 player extrapolation. It intentionally applies no new input because
 * remote input is unknown; the last observed velocity decays using the exact legacy friction and
 * gravity constants while resolving the same axis-ordered AABB collisions as vanilla movement.
 */
class PlayerPositionExtrapolation(private val player: EntityPlayer) : PositionExtrapolation {
    private val simulation = PlayerSimulationCache(PlayerSimulation.fromOtherPlayer(player))

    override fun getPositionInTicks(ticks: Double): Vec3 {
        if (ticks <= 0.0) {
            return LinearPositionExtrapolation(player).getPositionInTicks(ticks)
        }

        val boundedTicks = ticks.coerceAtMost(30.0)
        val wholeTicks = floor(boundedTicks).toInt()
        if (boundedTicks == wholeTicks.toDouble()) return simulation.getSnapshotAt(wholeTicks).pos

        val fraction = boundedTicks - wholeTicks
        val from = simulation.getSnapshotAt(wholeTicks).pos
        val to = simulation.getSnapshotAt(wholeTicks + 1).pos
        return Vec3(
            from.xCoord + (to.xCoord - from.xCoord) * fraction,
            from.yCoord + (to.yCoord - from.yCoord) * fraction,
            from.zCoord + (to.zCoord - from.zCoord) * fraction,
        )
    }

}
