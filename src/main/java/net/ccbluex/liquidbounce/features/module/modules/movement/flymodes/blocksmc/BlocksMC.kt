/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.blocksmc

import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.boostSpeed
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.debugFly
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.extraBoost
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.stable
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.stopOnLanding
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.stopOnNoMove
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.timerSlowed
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.FlyMode
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.extensions.airTicks
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.tryJump
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.strafe
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.world.World

/**
 * Fly concept originally taken from CrossSine:
 * https://github.com/shxp3/CrossSine/blob/main/src/main/java/net/ccbluex/liquidbounce/features/module/modules/movement/flights/ncp/LatestNCP.java
 *
 * Modified by @Eclipses and mixed with code from NextGen:
 * https://github.com/CCBlueX/LiquidBounce/blob/nextgen/src/main/kotlin/net/ccbluex/liquidbounce/features/module/modules/movement/fly/modes/specific/FlyNcpClip.kt
 *
 * NOTE (Before using this fly mode, READ THIS):
 * Caution: Prolonged flying over long distances is not recommended.
 *
 * Additionally, ensure that you avoid flight before you got flagged or
 * (S08 Packet) teleported, as this will flag u more, or you can wait till
 * you get the Fly message Line(153). Also avoid flying too many times (At long distance).
 *
 * @author EclipsesDev
 */
object BlocksMC : FlyMode("BlocksMC"), Listenable {

    private var isFlying = false
    private var isNotUnder = false
    private var isTeleported = false
    private var jumped = false

    override fun onUpdate() {
        val player = mc.thePlayer ?: return
        val world = mc.theWorld ?: return

        if (isFlying) {
            if (player.onGround && stopOnLanding) {
                if (debugFly)
                    chat("Ground Detected.. Stopping Fly")
                Flight.state = false
            }

            if (!player.isMoving && stopOnNoMove) {
                if (debugFly)
                    chat("No Movement Detected.. Stopping Fly. (Could be flagged)")
                Flight.state = false
            }
        }

        if (shouldFly(player, world)) {
            if (isTeleported) {

                if (stable)
                    player.motionY = 0.0

                handleTimerSlow(player)
                handlePlayerFlying(player)
            } else {
                if (debugFly)
                    chat("Waiting to be Teleported.. Please ensure you're below a block.")
            }
        } else {
            handleTeleport(player)
        }

        strafe()
    }

    override fun onDisable() {
        isNotUnder = false
        isFlying = false
        isTeleported = false
        jumped = false
    }

    val onWorld = handler<WorldEvent> {
        Flight.state = false
    }

    private fun handleTimerSlow(player: EntityPlayerSP) {
        if (!player.onGround && timerSlowed) {
            if (player.ticksExisted % 7 == 0) {
                mc.timer.timerSpeed = 0.415f
            } else {
                mc.timer.timerSpeed = 0.35f
            }
        } else {
            mc.timer.timerSpeed = 1.0f
        }
    }

    private fun shouldFly(player: EntityPlayerSP, world: World): Boolean {
        return world.getCollidingBoundingBoxes(player, player.entityBoundingBox.offset(0.0, 1.0, 0.0))
            .isEmpty() || isFlying
    }

    private fun handlePlayerFlying(player: EntityPlayerSP) {
        when (player.airTicks) {
            0 -> {
                if (isNotUnder && isTeleported) {
                    strafe(boostSpeed + extraBoost)
                    player.tryJump()
                    isFlying = true
                    isNotUnder = false
                }
            }

            1 -> {
                if (isFlying) {
                    strafe(boostSpeed)
                }
            }
        }
    }

    private fun handleTeleport(player: EntityPlayerSP) {
        isNotUnder = true
        if (!isTeleported) {
            sendPackets(
                C04PacketPlayerPosition(
                    player.posX,
                    // Clipping is now patch in BlocksMC
                    player.posY - 0.05,
                    player.posZ,
                    false
                )
            )
            sendPackets(
                C04PacketPlayerPosition(
                    player.posX,
                    player.posY,
                    player.posZ,
                    false
                )
            )

            isTeleported = true
            if (debugFly)
                chat("Teleported.. Fly Now!")
        }
    }
}