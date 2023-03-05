package net.ccbluex.liquidbounce.features.module.modules.movement.flys.verus

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C0BPacketEntityAction
import kotlin.math.floor

class VerusBoostFly : FlyMode("VerusBoost") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 1.5f, 0f, 10f)
    private val boostModeValue = ListValue("${valuePrefix}BoostMode", arrayOf("Boost1", "Boost2", "Boost3"), "Boost1")
    private val reDamageValue = BoolValue("${valuePrefix}Boost3-ReDamage", true).displayable { boostModeValue.equals("Boost3") }

    private var ticks = 0
    private var ticks2 = 0

    override fun onEnable() {
        ticks = 0
        ticks2 = 1
    }

    override fun onMove(event: MoveEvent) {
        when (boostModeValue.get()) {
            "Boost1" -> {
                val speed = speedValue.get()
                val pos = mc.thePlayer.position.add(0.0, -1.5, 0.0)
                PacketUtils.sendPacketNoEvent(
                    C08PacketPlayerBlockPlacement(pos, 1,
                        ItemStack(Blocks.stone.getItem(mc.theWorld, pos)), 0.0F, 0.5F + Math.random().toFloat() * 0.44.toFloat(), 0.0F)
                )
                if (ticks < 3)
                    event.cancelEvent()
                if(ticks > 4)
                    mc.thePlayer.motionY = 0.0684
                if(ticks <= 25) {
                    mc.timer.timerSpeed = 0.8f
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
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
                    val pos = mc.thePlayer.position.add(0.0, -1.5, 0.0)
                    PacketUtils.sendPacketNoEvent(
                        C08PacketPlayerBlockPlacement(pos, 1,
                            ItemStack(Blocks.stone.getItem(mc.theWorld, pos)), 0.0F, 0.5F + Math.random().toFloat() * 0.44.toFloat(), 0.0F)
                    )
                    val x = mc.thePlayer.posX
                    val y = mc.thePlayer.posY
                    val z = mc.thePlayer.posZ
                    PacketUtils.sendPacketNoEvent(C03PacketPlayer.C06PacketPlayerPosLook(x, y+3+Math.random()*0.07, z, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false))
                    PacketUtils.sendPacketNoEvent(C03PacketPlayer.C06PacketPlayerPosLook(x, y, z, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false))
                    PacketUtils.sendPacketNoEvent(C03PacketPlayer.C06PacketPlayerPosLook(x, y, z, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, true))
                    mc.timer.timerSpeed = 0.25f
                }
                if(mc.thePlayer.hurtTime > 2) {
                    mc.thePlayer.motionY += 0.4f
                    event.y = mc.thePlayer.motionY
                    mc.timer.timerSpeed = 1.0f
                    MovementUtils.strafe(speedValue.get())
                }
                if(mc.thePlayer.hurtTime == 3) mc.thePlayer.motionY = 0.42

                if(mc.thePlayer.hurtTime == 0) {
                    MovementUtils.strafe(0.36F)
                    if(mc.thePlayer.fallDistance > 0) mc.thePlayer.motionY = 0.0
                }
            }
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        if(boostModeValue.get() === "Boost3") {
            if (ticks2 == 1) {
                mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                mc.netHandler.addToSendQueue(
                    C03PacketPlayer.C04PacketPlayerPosition(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY,
                        mc.thePlayer.posZ,
                        true
                    )
                )
                mc.netHandler.addToSendQueue(
                    C03PacketPlayer.C04PacketPlayerPosition(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY + 3.42,
                        mc.thePlayer.posZ,
                        false
                    )
                )
                mc.netHandler.addToSendQueue(
                    C03PacketPlayer.C04PacketPlayerPosition(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY,
                        mc.thePlayer.posZ,
                        false
                    )
                )
                mc.netHandler.addToSendQueue(
                    C03PacketPlayer.C04PacketPlayerPosition(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY,
                        mc.thePlayer.posZ,
                        true
                    )
                )
                mc.timer.timerSpeed = 0.15f
                mc.thePlayer.jump()
                mc.thePlayer.onGround = true
            } else if (ticks2 == 2) {
                mc.timer.timerSpeed = 1f
            }

            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()
            }

            if (mc.thePlayer.fallDistance > 1) {
                mc.thePlayer.motionY = -((mc.thePlayer.posY) - floor(mc.thePlayer.posY))
            }

            if (mc.thePlayer.motionY == 0.0) {
                mc.thePlayer.jump()

                mc.thePlayer.onGround = true
                mc.thePlayer.fallDistance = 0f
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
                    PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX,mc.thePlayer.posY+3.25,mc.thePlayer.posZ,false))
                    PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX,mc.thePlayer.posY,mc.thePlayer.posZ,false))
                    PacketUtils.sendPacketNoEvent(C03PacketPlayer(true))
                    mc.timer.timerSpeed = 0.4f
                    mc.thePlayer.jump()
                }else {
                    if(ticks == 4) mc.thePlayer.motionY += 0.3
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
