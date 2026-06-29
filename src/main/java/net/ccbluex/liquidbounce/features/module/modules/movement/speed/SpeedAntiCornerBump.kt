/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speed

import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.simulation.SimulatedPlayer
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MovementInput
import net.minecraft.util.Vec3

/** Predicts whether an immediate speed jump would clip a reachable block corner. */
object SpeedAntiCornerBump : MinecraftInstance {

    fun shouldDelayJump(jumpsToInspect: Int = 2): Boolean {
        val player = mc.thePlayer ?: return false
        if (!player.onGround) return false

        val input = MovementInput().apply {
            moveForward = player.movementInput.moveForward
            moveStrafe = player.movementInput.moveStrafe
            jump = true
            sneak = player.movementInput.sneak
        }
        val simulation = SimulatedPlayer.fromClientPlayer(input)
        var jumps = 1
        var lastGround = simulation.pos

        repeat(66) {
            simulation.tick()

            if (simulation.onGround) {
                if (jumps++ >= jumpsToInspect) return false
                lastGround = simulation.pos
            }

            if (simulation.isCollidedHorizontally) {
                if (jumps == 1 && simulation.motionY > 0.0) return false
                return canJumpOnCollidingBlock(simulation, lastGround)
            }
        }

        return false
    }

    private fun canJumpOnCollidingBlock(simulation: SimulatedPlayer, lastGround: Vec3): Boolean {
        val player = mc.thePlayer ?: return false
        val world = mc.theWorld ?: return false
        val nearby = world.getCollidingBoundingBoxes(player, simulation.box.expand(0.06, 0.02, 0.06))

        return nearby.any { obstacle ->
            val rise = obstacle.maxY - lastGround.yCoord
            if (rise !in 0.5..1.3) return@any false

            val halfWidth = player.width / 2.0
            val standingBox = AxisAlignedBB(
                simulation.posX - halfWidth,
                obstacle.maxY + 1.0E-4,
                simulation.posZ - halfWidth,
                simulation.posX + halfWidth,
                obstacle.maxY + player.height,
                simulation.posZ + halfWidth,
            ).contract(0.01, 0.0, 0.01)

            world.getCollidingBoundingBoxes(player, standingBox).isEmpty()
        }
    }
}
