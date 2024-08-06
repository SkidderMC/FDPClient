/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.other

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverSlot
import net.ccbluex.liquidbounce.utils.timing.TickedActions
import net.ccbluex.liquidbounce.utils.timing.WaitTickUtils
import net.minecraft.init.Items
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.util.BlockPos

object Fireball : FlyMode("Fireball") {

    override fun onMotion(event: MotionEvent) {
        val player = mc.thePlayer ?: return

        val fireballSlot = InventoryUtils.findItem(36, 44, Items.fire_charge) ?: return

        when (Flight.autoFireball.lowercase()) {
            "pick" -> {
                player.inventory.currentItem = fireballSlot - 36
                mc.playerController.updateController()
            }

            "spoof", "switch" -> serverSlot = fireballSlot - 36
        }

        if (event.eventState != EventState.POST)
            return

        val customRotation = Rotation(if (Flight.invertYaw) RotationUtils.invertYaw(player.rotationYaw) else player.rotationYaw, Flight.rotationPitch)

        if (player.onGround && !mc.theWorld.isAirBlock(BlockPos(player.posX, player.posY - 1, player.posZ))) Flight.firePosition = BlockPos(player.posX, player.posY - 1, player.posZ)

        val smartRotation = Flight.firePosition?.getVec()?.let { RotationUtils.toRotation(it, false, player) }
        val rotation = if (Flight.pitchMode == "Custom") customRotation else smartRotation

        if (Flight.rotations) {
            if (rotation != null) {
                RotationUtils.setTargetRotation(
                    rotation,
                    if (Flight.keepRotation) Flight.keepTicks else 1,
                    turnSpeed = Flight.minHorizontalSpeed.get()..Flight.maxHorizontalSpeed.get() to Flight.minVerticalSpeed.get()..Flight.maxVerticalSpeed.get(),
                    angleThresholdForReset = Flight.angleThresholdUntilReset,
                    smootherMode = Flight.smootherMode,
                    simulateShortStop = Flight.simulateShortStop,
                    startOffSlow = Flight.startFirstRotationSlow
                )
            }
        }

        if (Flight.fireBallThrowMode == "Edge" && !player.isNearEdge(Flight.edgeThreshold))
            return

        if (Flight.autoJump && player.onGround && !Flight.wasFired) {
            player.tryJump()
        }
    }

    override fun onTick() {
        val player = mc.thePlayer ?: return

        val fireballSlot = InventoryUtils.findItem(36, 44, Items.fire_charge) ?: return
        val fireBall = player.inventoryContainer.getSlot(fireballSlot).stack

        if (Flight.fireBallThrowMode == "Edge" && !player.isNearEdge(Flight.edgeThreshold))
            return

        if (Flight.wasFired) {
            return
        }

        if (isMoving) {
            TickedActions.TickScheduler(Flight) += {
                if (Flight.swing) player.swingItem() else sendPacket(C0APacketAnimation())

                // NOTE: You may increase max try to `2` if fireball doesn't work. (Ex: BlocksMC)
                repeat(Flight.fireballTry) {
                    player.sendUseItem(fireBall)
                }
            }

            WaitTickUtils.scheduleTicks(2) {
                if (Flight.autoFireball == "Pick") {
                    player.inventory.currentItem = fireballSlot - 36
                    mc.playerController.updateController()
                } else {
                    serverSlot = fireballSlot - 36
                }

                Flight.wasFired = true
            }
        }
    }
}
