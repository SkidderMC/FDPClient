/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.simulation

import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MovementInput
import net.minecraft.util.Vec3

data class SimulatedPlayerSnapshot(
    val pos: Vec3,
    val box: AxisAlignedBB,
    val motionX: Double,
    val motionY: Double,
    val motionZ: Double,
    val onGround: Boolean,
    val fallDistance: Float,
    val collidedHorizontally: Boolean,
) {
    constructor(player: SimulatedPlayer) : this(
        player.pos,
        player.box,
        player.motionX,
        player.motionY,
        player.motionZ,
        player.onGround,
        player.fallDistance,
        player.isCollidedHorizontally
    )

    val velocity: Vec3
        get() = Vec3(motionX, motionY, motionZ)
}

class SimulatedPlayerCache(private val simulatedPlayer: SimulatedPlayer) {

    private var currentStep = 0
    private val steps = ArrayList<SimulatedPlayerSnapshot>().apply {
        add(SimulatedPlayerSnapshot(simulatedPlayer))
    }

    fun simulateUntil(ticks: Int) {
        require(ticks >= 0) { "ticks may not be negative" }
        require(ticks < 60 * 20) { "tried to simulate a player for more than a minute" }

        if (currentStep >= ticks) {
            return
        }

        while (currentStep < ticks) {
            simulatedPlayer.tick()
            steps.add(SimulatedPlayerSnapshot(simulatedPlayer))
            currentStep++
        }
    }

    fun getSnapshotAt(ticks: Int): SimulatedPlayerSnapshot {
        simulateUntil(ticks)
        return steps[ticks]
    }

    fun getSnapshotsBetween(tickRange: IntRange): List<SimulatedPlayerSnapshot> {
        simulateUntil(tickRange.last)
        return ArrayList(steps.subList(tickRange.first, tickRange.last + 1))
    }

    fun positions(ticks: Int): List<Vec3> {
        simulateUntil(ticks)
        return (0..ticks).map { steps[it].pos }
    }

    companion object {

        fun fromClientPlayer(input: MovementInput): SimulatedPlayerCache {
            return SimulatedPlayerCache(SimulatedPlayer.fromClientPlayer(input))
        }

        fun fromClientPlayer(input: DirectionalInputLike): SimulatedPlayerCache {
            val movementInput = MovementInput().apply {
                this.jump = input.jump
                this.sneak = input.sneak
                this.moveForward = if (input.forwards) 1.0f else if (input.backwards) -1.0f else 0.0f
                this.moveStrafe = if (input.left) 1.0f else if (input.right) -1.0f else 0.0f
            }

            return fromClientPlayer(movementInput)
        }
    }
}

data class DirectionalInputLike(
    val forwards: Boolean = false,
    val backwards: Boolean = false,
    val left: Boolean = false,
    val right: Boolean = false,
    val jump: Boolean = false,
    val sneak: Boolean = false,
)
