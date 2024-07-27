/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.verus

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.verusYPort2speedValue
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.verusYPortspeedValue
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.verusSpeed
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.minecraft.network.play.client.C03PacketPlayer

object VerusSpeeds : SpeedMode("VerusSpeeds") {
    // Variables
    private var firstHop = false
    private var ticks = 0
    private var Bypass = false
    private var IsinAir = false

    override fun onEnable() {
        Bypass = false
    }

    override fun onUpdate() {
        when (verusSpeed) {
            "OldHop" -> {
                if (isMoving) {
                    mc.gameSettings.keyBindJump.pressed = false
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump()
                        MovementUtils.strafe(0.48f)

                    }
                    MovementUtils.strafe()
                }
            }
            "YPort2" -> {
                mc.thePlayer.motionY = -0.0784000015258789
            }
        }
    }

     fun onPreMotion() {
        when (verusSpeed) {
            "Ground" -> {
                if (mc.thePlayer.onGround)
                    if (verusSpeed == "Ground") {
                        if (mc.thePlayer.ticksExisted % 12 == 0) {
                            firstHop = false
                            MovementUtils.strafe(0.69f)
                            mc.thePlayer.jump()
                            mc.thePlayer.motionY = 0.0
                            MovementUtils.strafe(0.69f)
                            mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.42, mc.thePlayer.posZ, false))
                            MovementUtils.strafe(0.41f)
                            mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false))
                        } else if (!firstHop) {
                            MovementUtils.strafe(1.01f)
                        }
                    }
            }
            "Float" -> {
                ticks++
                if (!mc.gameSettings.keyBindJump.isKeyDown) {
                    if (mc.thePlayer.onGround) {
                        ticks = 0
                        MovementUtils.strafe(0.44f)
                        mc.thePlayer.motionY = 0.42
                        mc.timer.timerSpeed = 2.1f
                        IsinAir = true
                    } else if (IsinAir) {
                        if (ticks >= 10) {
                            Bypass = true
                            MovementUtils.strafe(0.2865f)
                            IsinAir = false
                        }

                        if (Bypass) {
                            if (ticks <= 1) {
                                MovementUtils.strafe(0.45f)
                            }

                            if (ticks >= 2) {
                                MovementUtils.strafe(0.69f - (ticks - 2) * 0.019f)
                            }
                        }

                        mc.thePlayer.motionY = 0.0
                        mc.timer.timerSpeed = 0.9f

                        mc.thePlayer.onGround = true
                    }
                }
            }
        }
    }

    override fun onMove(event: MoveEvent) {
        when (verusSpeed) {
            "YPort" -> {
                if (isMoving) {
                    mc.gameSettings.keyBindJump.pressed = false
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump()
                        mc.thePlayer.motionY = 0.0
                        MovementUtils.strafe(verusYPortspeedValue)
                        event.y = 0.41999998688698
                    } else {
                        MovementUtils.strafe()
                    }
                }
            }
           "YPort2" -> {
               if (isMoving) {
                   mc.gameSettings.keyBindJump.pressed = false
                   if (mc.thePlayer.onGround) {
                       MovementUtils.strafe(verusYPort2speedValue)
                       event.y = 0.41999998688698
                   } else {
                       MovementUtils.strafe()
                   }
               }
           }

        }
    }
}