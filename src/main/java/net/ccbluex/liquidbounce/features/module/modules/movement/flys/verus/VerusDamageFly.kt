/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.flys.verus

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.settings.GameSettings
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.util.Timer
import kotlin.math.floor

class VerusDamageFly : FlyMode("VerusDamage") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 1.5f, 0f, 10f)
    private val boostModeValue = ListValue("${valuePrefix}BoostMode", arrayOf("Boost1", "Boost2", "Boost3"), "Boost1")
    private val reDamageValue = BoolValue("${valuePrefix}Boost3-ReDamage", true).displayable { boostModeValue.equals("Boost3") }

    private var ticks = 0
    private var ticks2 = 0

    // Optimize code
    val player: EntityPlayerSP
        get() = mc.thePlayer
    val timer: Timer
        get() = mc.timer


    override fun onEnable() {
        ticks = 0
        ticks2 = 1
    }

    override fun onMove(event: MoveEvent) {
        when (boostModeValue.get()) {
            "Boost1" -> {
                val speed = speedValue.get()
                val pos = player.position.add(0.0, -1.5, 0.0)
                PacketUtils.sendPacketNoEvent(
                    C08PacketPlayerBlockPlacement(pos, 1,
                        ItemStack(Blocks.stone.getItem(mc.theWorld, pos)), 0.0F, 0.5F + Math.random().toFloat() * 0.44.toFloat(), 0.0F)
                )
                if (ticks < 3)
                    event.cancelEvent()
                if(ticks > 4)
                    player.motionY = 0.0684
                if(ticks <= 25) {
                    timer.timerSpeed = 0.8f
                    player.motionX = 0.0
                    player.motionZ = 0.0
                    MovementUtils.strafe(speed)
                }else {
                    MovementUtils.strafe(0.29F)
                }
            }

            "Boost2" -> {
                if(ticks < 3) {
                    event.cancelEvent()
                }
                if(ticks == 3) {
                    val pos = player.position.add(0.0, -1.5, 0.0)
                    PacketUtils.sendPacketNoEvent(
                        C08PacketPlayerBlockPlacement(pos, 1,
                            ItemStack(Blocks.stone.getItem(mc.theWorld, pos)), 0.0F, 0.5F + Math.random().toFloat() * 0.44.toFloat(), 0.0F)
                    )
                    val x = player.posX
                    val y = player.posY
                    val z = player.posZ
                    PacketUtils.sendPacketNoEvent(C03PacketPlayer.C06PacketPlayerPosLook(x, y+3+Math.random()*0.07, z, player.rotationYaw, player.rotationPitch, false))
                    PacketUtils.sendPacketNoEvent(C03PacketPlayer.C06PacketPlayerPosLook(x, y, z, player.rotationYaw, player.rotationPitch, false))
                    PacketUtils.sendPacketNoEvent(C03PacketPlayer.C06PacketPlayerPosLook(x, y, z, player.rotationYaw, player.rotationPitch, true))
                    timer.timerSpeed = 0.25f
                }
                if(player.hurtTime > 2) {
                    player.motionY += 0.4f
                    event.y = player.motionY
                    timer.timerSpeed = 1.0f
                    MovementUtils.strafe(speedValue.get())
                }
                if(player.hurtTime == 3) player.motionY = 0.42

                if(player.hurtTime == 0) {
                    MovementUtils.strafe(0.36F)
                    if(player.fallDistance > 0) player.motionY = 0.0
                }
            }
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        if(boostModeValue.get() === "Boost3") {
            if (ticks2 == 1) {
                mc.netHandler.addToSendQueue(C0BPacketEntityAction(player, C0BPacketEntityAction.Action.START_SPRINTING))
                mc.netHandler.addToSendQueue(
                    C03PacketPlayer.C04PacketPlayerPosition(
                        player.posX,
                        player.posY,
                        player.posZ,
                        true
                    )
                )
                mc.netHandler.addToSendQueue(
                    C03PacketPlayer.C04PacketPlayerPosition(
                        player.posX,
                        player.posY + 3.42,
                        player.posZ,
                        false
                    )
                )
                mc.netHandler.addToSendQueue(
                    C03PacketPlayer.C04PacketPlayerPosition(
                        player.posX,
                        player.posY,
                        player.posZ,
                        false
                    )
                )
                mc.netHandler.addToSendQueue(
                    C03PacketPlayer.C04PacketPlayerPosition(
                        player.posX,
                        player.posY,
                        player.posZ,
                        true
                    )
                )
                timer.timerSpeed = 0.15f
                player.jump()
                player.onGround = true
            } else if (ticks2 == 2) {
                timer.timerSpeed = 1f
            }

            if (player.onGround) {
                player.jump()
            }

            if (player.fallDistance > 1) {
                player.motionY = -((player.posY) - floor(player.posY))
            }

            if (player.motionY == 0.0) {
                player.jump()

                player.onGround = true
                player.fallDistance = 0f
            }

            if (ticks2 < 25) {
                MovementUtils.strafe(speedValue.get())
            } else {
                if (ticks2 == 25){
                    MovementUtils.strafe(0.48f)
                }
                if (reDamageValue.get()) {
                    ticks2 = 1
                }
                MovementUtils.strafe()
            }

            ticks2 ++
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        when (boostModeValue.get()) {
            "Boost1", "Boost2" -> {
                if (packet is C03PacketPlayer && ticks < 3) {
                    packet.onGround = true
                }
            }
        }
    }

    override fun onMotion(event: MotionEvent) {
        when (boostModeValue.get()) {
            "Boost1" -> {
                if(!event.isPre())
                    return
                ticks++
                if(ticks == 3) {
                    PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(player.posX,player.posY+3.25,player.posZ,false))
                    PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(player.posX,player.posY,player.posZ,false))
                    PacketUtils.sendPacketNoEvent(C03PacketPlayer(true))
                    timer.timerSpeed = 0.4f
                    player.jump()
                }else {
                    if(ticks == 4) player.motionY += 0.3
                }
            }

            "Boost2" -> {
                if(!event.isPre()) return

                ticks++
            }
        }
    }

    override fun onJump(event: JumpEvent) {
        if(boostModeValue.get() === "Boost1") event.cancelEvent()
    }
}
