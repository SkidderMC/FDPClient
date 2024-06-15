/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.verus

import me.zywl.fdpclient.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import me.zywl.fdpclient.value.impl.FloatValue
import me.zywl.fdpclient.value.impl.ListValue
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.network.play.client.C03PacketPlayer

class VerusSpeeds : SpeedMode("Verus") {
    private val modeValue = ListValue("Verus-Mode", arrayOf("Hop", "Float", "Ground", "YPort", "YPort2"), "Hop")

    private val yPortspeedValue = FloatValue("YPort-Speed", 0.61f, 0.1f, 1f).displayable { modeValue.equals("YPort") }
    private val yPort2speedValue = FloatValue("YPort2-Speed", 0.61f, 0.1f, 1f).displayable { modeValue.equals("YPort2") }


    // Variables
    private var firstHop = false
    private var ticks = 0
    private var bypass = false
    private var isinAir = false

    // Optimize code
    val player: EntityPlayerSP
        get() = mc.thePlayer

    override fun onEnable() {
        bypass = false
    }

    override fun onUpdate() {
        when (modeValue.get()) {
            "Hop" -> {
                if (isMoving()) {
                    mc.gameSettings.keyBindJump.pressed = false
                    if (player.onGround) {
                        player.jump()
                        MovementUtils.strafe(0.48f)

                    }
                    MovementUtils.strafe()
                }
            }
            "YPort2" -> {
                player.motionY = -0.0784000015258789
            }
        }
    }

    override fun onPreMotion() {
        when (modeValue.get()) {
            "Ground" -> {
                if (player.onGround)
                    if (modeValue.equals("Ground")) {
                        if (player.ticksExisted % 12 == 0) {
                            firstHop = false
                            MovementUtils.strafe(0.69f)
                            player.jump()
                            player.motionY = 0.0
                            MovementUtils.strafe(0.69f)
                            mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY + 0.42, player.posZ, false))
                            MovementUtils.strafe(0.41f)
                            mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY, player.posZ, false))
                        } else if (!firstHop) {
                            MovementUtils.strafe(1.01f)
                        }
                    }
            }
            "Float" -> {
                ticks++
                if (!mc.gameSettings.keyBindJump.isKeyDown) {
                    if (player.onGround) {
                        ticks = 0
                        MovementUtils.strafe(0.44f)
                        player.motionY = 0.42
                        mc.timer.timerSpeed = 2.1f
                        isinAir = true
                    } else if (isinAir) {
                        if (ticks >= 10) {
                            bypass = true
                            MovementUtils.strafe(0.2865f)
                            isinAir = false
                        }

                        if (bypass) {
                            if (ticks <= 1) {
                                MovementUtils.strafe(0.45f)
                            }

                            if (ticks >= 2) {
                                MovementUtils.strafe(0.69f - (ticks - 2) * 0.019f)
                            }
                        }

                        player.motionY = 0.0
                        mc.timer.timerSpeed = 0.9f

                        player.onGround = true
                    }
                }
            }
        }
    }

    override fun onMove(event: MoveEvent) {
        when (modeValue.get()) {
            "YPort" -> {
                if (isMoving()) {
                    mc.gameSettings.keyBindJump.pressed = false
                    if (player.onGround) {
                        player.jump()
                        player.motionY = 0.0
                        MovementUtils.strafe(yPortspeedValue.get())
                        event.y = 0.41999998688698
                    } else {
                        MovementUtils.strafe()
                    }
                }
            }
           "YPort2" -> {
               if (isMoving()) {
                   mc.gameSettings.keyBindJump.pressed = false
                   if (player.onGround) {
                       MovementUtils.strafe(yPort2speedValue.get())
                       event.y = 0.41999998688698
                   } else {
                       MovementUtils.strafe()
                   }
               }
           }

        }
    }
}