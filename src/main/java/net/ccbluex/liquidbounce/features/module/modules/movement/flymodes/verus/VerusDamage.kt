/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.verus

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.FlyMode
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.strafe
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C0BPacketEntityAction
import kotlin.math.floor

object VerusDamage : FlyMode("VerusDamage") {
    private var ticks = 0
    private var ticks2 = 0

    override fun onEnable() {
        ticks = 0
        ticks2 = 1
    }

    override fun onMove(event: MoveEvent) {
        val player = mc.thePlayer ?: return

        when (Flight.verusDamageBoostMode) {
            "Boost1" -> {
                val pos = player.position.add(0.0, -1.5, 0.0)
                sendPacket(
                    C08PacketPlayerBlockPlacement(
                        pos,
                        1,
                        ItemStack(Blocks.stone.getItem(mc.theWorld, pos)),
                        0.0F,
                        0.5F + Math.random().toFloat() * 0.44F,
                        0.0F
                    ),
                    false
                )

                if (ticks < 3) {
                    event.cancelEvent()
                }

                if (ticks > 4) {
                    player.motionY = 0.0684
                }

                if (ticks <= 25) {
                    mc.timer.timerSpeed = 0.8f
                    player.motionX = 0.0
                    player.motionZ = 0.0
                    strafe(Flight.verusDamageSpeed)
                } else {
                    strafe(0.29F)
                }
            }

            "Boost2" -> {
                if (ticks < 3) {
                    event.cancelEvent()
                }

                if (ticks == 3) {
                    val pos = player.position.add(0.0, -1.5, 0.0)
                    sendPacket(
                        C08PacketPlayerBlockPlacement(
                            pos,
                            1,
                            ItemStack(Blocks.stone.getItem(mc.theWorld, pos)),
                            0.0F,
                            0.5F + Math.random().toFloat() * 0.44F,
                            0.0F
                        ),
                        false
                    )

                    val x = player.posX
                    val y = player.posY
                    val z = player.posZ

                    sendPacket(C06PacketPlayerPosLook(x, y + 3 + Math.random() * 0.07, z, player.rotationYaw, player.rotationPitch, false), false)
                    sendPacket(C06PacketPlayerPosLook(x, y, z, player.rotationYaw, player.rotationPitch, false), false)
                    sendPacket(C06PacketPlayerPosLook(x, y, z, player.rotationYaw, player.rotationPitch, true), false)
                    mc.timer.timerSpeed = 0.25f
                }

                if (player.hurtTime > 2) {
                    player.motionY += 0.4
                    event.y = player.motionY
                    mc.timer.timerSpeed = 1.0f
                    strafe(Flight.verusDamageSpeed)
                }

                if (player.hurtTime == 3) {
                    player.motionY = 0.42
                }

                if (player.hurtTime == 0) {
                    strafe(0.36F)
                    if (player.fallDistance > 0) {
                        player.motionY = 0.0
                    }
                }
            }
        }
    }

    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        if (Flight.verusDamageBoostMode != "Boost3") {
            return
        }

        if (ticks2 == 1) {
            sendPacket(C0BPacketEntityAction(player, C0BPacketEntityAction.Action.START_SPRINTING), false)
            sendPacket(C04PacketPlayerPosition(player.posX, player.posY, player.posZ, true), false)
            sendPacket(C04PacketPlayerPosition(player.posX, player.posY + 3.42, player.posZ, false), false)
            sendPacket(C04PacketPlayerPosition(player.posX, player.posY, player.posZ, false), false)
            sendPacket(C04PacketPlayerPosition(player.posX, player.posY, player.posZ, true), false)
            mc.timer.timerSpeed = 0.15f
            player.jump()
            player.onGround = true
        } else if (ticks2 == 2) {
            mc.timer.timerSpeed = 1f
        }

        if (player.onGround) {
            player.jump()
        }

        if (player.fallDistance > 1) {
            player.motionY = -(player.posY - floor(player.posY))
        }

        if (player.motionY == 0.0) {
            player.jump()
            player.onGround = true
            player.fallDistance = 0f
        }

        if (ticks2 < 25) {
            strafe(Flight.verusDamageSpeed)
        } else {
            if (ticks2 == 25) {
                strafe(0.48f)
            }

            if (Flight.verusDamageReDamage) {
                ticks2 = 1
            }

            strafe()
        }

        ticks2++
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (Flight.verusDamageBoostMode in arrayOf("Boost1", "Boost2") && packet is C03PacketPlayer && ticks < 3) {
            packet.onGround = true
        }
    }

    override fun onMotion(event: MotionEvent) {
        val player = mc.thePlayer ?: return

        if (event.eventState != EventState.PRE) {
            return
        }

        when (Flight.verusDamageBoostMode) {
            "Boost1" -> {
                ticks++

                if (ticks == 3) {
                    sendPacket(C04PacketPlayerPosition(player.posX, player.posY + 3.25, player.posZ, false), false)
                    sendPacket(C04PacketPlayerPosition(player.posX, player.posY, player.posZ, false), false)
                    sendPacket(C03PacketPlayer(true), false)
                    mc.timer.timerSpeed = 0.4f
                    player.jump()
                } else if (ticks == 4) {
                    player.motionY += 0.3
                }
            }

            "Boost2" -> ticks++
        }
    }

    override fun onJump(event: JumpEvent) {
        if (Flight.verusDamageBoostMode == "Boost1") {
            event.cancelEvent()
        }
    }
}
