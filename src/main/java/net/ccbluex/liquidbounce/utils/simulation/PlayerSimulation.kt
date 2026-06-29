/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.simulation

import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import net.minecraft.util.MovementInput
import net.minecraft.util.Vec3

interface PlayerSimulation {
    val pos: Vec3
    val box: AxisAlignedBB
    val velocity: Vec3
    val onGround: Boolean
    fun tick()

    /** Collision-aware no-input simulation for a remote player. */
    class Rigid private constructor(private val entity: Entity) : PlayerSimulation {
        override var box: AxisAlignedBB = entity.entityBoundingBox
            private set
        private var motionX = entity.posX - entity.prevPosX
        private var motionY = entity.posY - entity.prevPosY
        private var motionZ = entity.posZ - entity.prevPosZ
        override var onGround: Boolean = entity.onGround
            private set

        override val pos: Vec3
            get() = Vec3((box.minX + box.maxX) * 0.5, box.minY, (box.minZ + box.maxZ) * 0.5)
        override val velocity: Vec3
            get() = Vec3(motionX, motionY, motionZ)

        override fun tick() {
            var moveX = motionX
            var moveY = motionY
            var moveZ = motionZ
            val originalMotionY = motionY
            val collisions = entity.worldObj.getCollidingBoundingBoxes(entity, box.addCoord(moveX, moveY, moveZ))

            for (collision in collisions) moveY = collision.calculateYOffset(box, moveY)
            box = box.offset(0.0, moveY, 0.0)
            for (collision in collisions) moveX = collision.calculateXOffset(box, moveX)
            box = box.offset(moveX, 0.0, 0.0)
            for (collision in collisions) moveZ = collision.calculateZOffset(box, moveZ)
            box = box.offset(0.0, 0.0, moveZ)

            if (moveX != motionX) motionX = 0.0
            if (moveY != motionY) motionY = 0.0
            if (moveZ != motionZ) motionZ = 0.0
            onGround = moveY != originalMotionY && originalMotionY < 0.0

            val friction = if (onGround) {
                val position = pos
                val below = BlockPos(
                    MathHelper.floor_double(position.xCoord),
                    MathHelper.floor_double(box.minY) - 1,
                    MathHelper.floor_double(position.zCoord),
                )
                entity.worldObj.getBlockState(below).block.slipperiness.toDouble() * 0.91
            } else 0.91

            motionX *= friction
            motionZ *= friction
            motionY = (motionY - 0.08) * 0.98
        }

        companion object {
            fun fromOtherPlayer(player: EntityPlayer) = Rigid(player)
        }
    }

    class Local(private val simulation: SimulatedPlayer) : PlayerSimulation {
        override val pos get() = simulation.pos
        override val box get() = simulation.box
        override val velocity get() = Vec3(simulation.motionX, simulation.motionY, simulation.motionZ)
        override val onGround get() = simulation.onGround
        override fun tick() = simulation.tick()
    }

    companion object {
        fun fromOtherPlayer(player: EntityPlayer): PlayerSimulation = Rigid.fromOtherPlayer(player)
        fun fromClientPlayer(input: MovementInput): PlayerSimulation = Local(SimulatedPlayer.fromClientPlayer(input))
    }
}

data class PlayerSimulationSnapshot(
    val pos: Vec3,
    val box: AxisAlignedBB,
    val velocity: Vec3,
    val onGround: Boolean,
)

class PlayerSimulationCache(private val simulation: PlayerSimulation) {
    private val snapshots = ArrayList<PlayerSimulationSnapshot>().apply { add(snapshot()) }

    fun getSnapshotAt(ticks: Int): PlayerSimulationSnapshot {
        require(ticks in 0..1200) { "ticks must be between 0 and 1200" }
        while (snapshots.size <= ticks) {
            simulation.tick()
            snapshots += snapshot()
        }
        return snapshots[ticks]
    }

    fun simulateBetween(range: IntRange): List<PlayerSimulationSnapshot> {
        require(range.first >= 0 && range.last <= 1200 && !range.isEmpty()) { "invalid simulation range" }
        getSnapshotAt(range.last)
        return snapshots.subList(range.first, range.last + 1).toList()
    }

    private fun snapshot() = PlayerSimulationSnapshot(simulation.pos, simulation.box, simulation.velocity, simulation.onGround)
}
