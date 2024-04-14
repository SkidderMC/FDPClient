/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.flys.matrix

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.utils.TransferUtils
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.settings.GameSettings
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.Timer
import kotlin.math.cos
import kotlin.math.sin


class OldMatrixBoostFly : FlyMode("OldMatrixBoost") {
    private val bypassMode = ListValue("${valuePrefix}BypassMode", arrayOf("New", "Stable", "Test", "Custom"), "New")
    private val speed = FloatValue("${valuePrefix}Speed", 2.0f, 1.0f, 3.0f)
    private val customYMotion = FloatValue("${valuePrefix}CustomJumpMotion", 0.6f, 0.2f, 5f).displayable { bypassMode.equals("Custom") }
    private val jumpTimer = FloatValue("${valuePrefix}JumpTimer", 0.1f, 0.1f, 2f)
    private val boostTimer = FloatValue("${valuePrefix}BoostTimer", 1f, 0.5f, 3f)
    private var boostMotion = 0

    // Optimize code
    val player: EntityPlayerSP
        get() = mc.thePlayer

    override fun onEnable() {
        boostMotion = 0
    }
    


    override fun onUpdate(event: UpdateEvent) {
        if (boostMotion == 0) {
            val yaw = Math.toRadians(player.rotationYaw.toDouble())
            mc.netHandler.addToSendQueue(
                C03PacketPlayer.C04PacketPlayerPosition(
                    player.posX,
                    player.posY,
                    player.posZ,
                    true
                )
            )
            if (bypassMode.equals("Test")) {
                MovementUtils.strafe(5f)
                player.motionY = 2.0
            } else {
                mc.netHandler.addToSendQueue(
                    C03PacketPlayer.C04PacketPlayerPosition(
                        player.posX + -sin(yaw) * 1.5,
                        player.posY + 1,
                        player.posZ + cos(yaw) * 1.5,
                        false
                    )
                )
            }
            boostMotion = 1
            mc.timer.timerSpeed = jumpTimer.get()
        } else if (boostMotion == 1 && bypassMode.equals("Test")) {
            MovementUtils.strafe(1.89f)
            player.motionY = 2.0
        } else if (boostMotion == 2) {
            MovementUtils.strafe(speed.get())
            when (bypassMode.get().lowercase()) {
                "stable" -> player.motionY = 0.8
                "new" -> player.motionY = 0.48
                "test" -> {
                    val yaw = Math.toRadians(player.rotationYaw.toDouble())
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(player.posX + -sin(yaw) * 2,player.posY + 2.0,player.posZ + cos(yaw) * 2,true))
                    player.motionY = 2.0
                    MovementUtils.strafe(1.89f)
                }
                "custom" -> player.motionY = customYMotion.get().toDouble()
            }
            boostMotion = 3
        } else if (boostMotion < 5) {
            boostMotion++
        } else if (boostMotion >= 5) {
            mc.timer.timerSpeed = boostTimer.get()
            if (player.posY < fly.launchY - 1.0) {
                boostMotion = 0
            }
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if(mc.currentScreen == null && packet is S08PacketPlayerPosLook) {
            TransferUtils.noMotionSet = true
            if (boostMotion == 1) {
                boostMotion = 2
            }
        }
    }
}
