/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speed

import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.simulation.SimulatedPlayer
import net.minecraft.util.MovementInput
import net.minecraft.util.Vec3

/** Uses the 1.8 movement simulator to reject jumps that end over a lethal drop. */
object SpeedPreventDeadlyJump : MinecraftInstance {

    fun wouldJumpToDeath(maxFallDistance: Double = 10.0): Boolean {
        val player = mc.thePlayer ?: return false
        if (!player.onGround) return false

        val jumpInput = MovementInput().apply {
            moveForward = player.movementInput.moveForward
            moveStrafe = player.movementInput.moveStrafe
            jump = true
        }
        val simulation = SimulatedPlayer.fromClientPlayer(jumpInput)
        simulation.tick()

        var firstLanding: Vec3? = null
        for (ignored in 0..40) {
            simulation.tick()
            if (simulation.onGround) {
                firstLanding = simulation.pos
                break
            }
        }
        if (firstLanding == null) return true

        simulation.movementInput = MovementInput()
        repeat(5) { simulation.tick() }

        var stableGround: Vec3? = null
        for (ignored in 0..40) {
            simulation.tick()
            if (simulation.onGround) {
                stableGround = simulation.pos
                break
            }
        }

        return stableGround == null || player.posY - stableGround!!.yCoord > maxFallDistance
    }
}
