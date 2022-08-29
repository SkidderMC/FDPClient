/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */

package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.verus

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.ccbluex.liquidbounce.value.*

class VerusHop : SpeedMode("VerusHop") {

    private val modeValue = ListValue("VerusMode", arrayOf("Normal", "LowHop", "VerusHard", "FastHop", "Bhop", "Test"), "Normal")
    private val timerBoost = BoolValue("${valuePrefix}TimerBoost",true)

    private var jumps = 0
    private var lastY = 0.0
    private var damagedTicks = 0
    
    private var verusHopStage = 1
    
    override fun onEnable() {
        verusHopStage = 1
    }

                                        
    override fun onPreMotion() {

        if (MovementUtils.isMoving()) {
            if (timerBoost.get() && (jumps >= 1)) {
                mc.timer.timerSpeed = if (mc.thePlayer.motionY < 0) { 0.88f } else { 1.25f }
            }

            when {
                mc.thePlayer.onGround -> {
                    if (modeValue.equals("Normal") || modeValue.equals("LowHop") || modeValue.equals("FastHop")) {
                        MovementUtils.strafe(0.4848f)
                        if (modeValue.equals("LowHop")) {
                            mc.thePlayer.motionY = 0.38
                        } else {
                            mc.thePlayer.motionY = 0.42
                        }
                     } else if (modeValue.equals("VerusHard")) {
                         mc.thePlayer.jump()
                         if(mc.thePlayer.isSprinting()) {
                            MovementUtils.strafe(MovementUtils.getSpeed().toFloat() + 0.2F)
                         }
                     } else if (modeValue.equals("Bhop")) {
                          MovementUtils.strafe(0.35f)
                          mc.thePlayer.jump()
                     } else if (modeValue.equals("Test")) {
                            if (verusHopStage == 2) {
                                MovementUtils.strafe(0.61f)
                                mc.thePlayer.jump()
                                // MovementUtils.strafe(1.708f)
                                verusHopStage = 1
                            } else if (verusHopStage == 1) {
                                mc.thePlayer.posY += 0.42
                                MovementUtils.strafe(0.61f)
                                mc.timer.timerSpeed = 2.0f
                                verusHopStage = 2
                            }
                    }

                    if (mc.thePlayer.posY == lastY) {
                        jumps++
                    } else {
                        jumps = 0
                    }

                    lastY = mc.thePlayer.posY
                }
                else -> {
                    if (modeValue.equals("FastHop")) {
                        MovementUtils.strafe(0.3772f)
                    } else if (modeValue.equals("Bhop")) {
                        if (mc.thePlayer.fallDistance >= 1.5) {
                            if (damagedTicks > 0) {
                                MovementUtils.strafe(1.0f)
                            } else {
                                MovementUtils.strafe(0.26f)
                            }
                        } else if (damagedTicks > 0) {
                            MovementUtils.strafe(1.0f)
                        } else {
                            MovementUtils.strafe(0.33f)
                            if (mc.thePlayer.posY - lastY < 0.35) {
                                MovementUtils.strafe(0.5f)
                            }
                        }
                    } else if (modeValue.equals("Test")) {
                        if (verusHopStage == 2) {
                            MovementUtils.strafe(0.61f)
                            mc.timer.timerSpeed = 2.0f
                        } else {
                            mc.timer.timerSpeed = 0.95f
                        }
                    }
                }
            }
        }
    }
    
     override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S12PacketEntityVelocity) {
            if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer) {
                return
            }
            damagedTicks = 20
        }
     }
}
