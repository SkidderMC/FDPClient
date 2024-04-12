/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.flys.matrix

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.settings.GameSettings
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.util.Timer
import kotlin.math.cos
import kotlin.math.sin


class OldMatrixDamageFly : FlyMode("OldMatrixDamage") {


    private val mode = ListValue("${valuePrefix}Mode", arrayOf("Stable","Test","Custom"), "Stable")
    private val warn = BoolValue("${valuePrefix}DamageWarn",true)
    private val timerspeed = FloatValue("${valuePrefix}Timer", 1.0f, 0f, 2f).displayable { mode.equals("Custom") }
    private val speedBoost = FloatValue("${valuePrefix}Custom-BoostSpeed", 0.5f, 0f, 3f).displayable { mode.equals("Custom") }
    private val boostTicks = IntegerValue("${valuePrefix}Custom-BoostTicks", 27,10,40).displayable { mode.equals("Custom") }
    private val randomize = BoolValue("${valuePrefix}Custom-Randomize", true).displayable { mode.equals("Custom") }
    private val randomAmount = IntegerValue("${valuePrefix}Custom-RandomAmount", 1, 0, 30).displayable { mode.equals("Custom") }
    private val customstrafe = BoolValue("${valuePrefix}Custom-Strafe", true).displayable { mode.equals("Custom") }
    private val motionreduceonend = BoolValue("${valuePrefix}MotionReduceOnEnd", true)

    private var velocitypacket = false
    private var packetymotion = 0.0
    private var tick = 0
    private var randomNum = 0.2

    // Optimize code
    val player: EntityPlayerSP
        get() = mc.thePlayer
    val timer: Timer
        get() = mc.timer

    override fun onEnable() {
        if (warn.get())
            ClientUtils.displayChatMessage("§8[§c§lMatrix-Dmg-Flight§8] §aGetting damage from other entities (players, arrows, snowballs, eggs...) is required to bypass.")
        velocitypacket = false
        packetymotion = 0.0
        tick = 0
    }

    private fun resetmotion() {
        if(motionreduceonend.get()) {
            player.motionX = player.motionX / 10
            player.motionY = player.motionY / 10
            player.motionZ = player.motionZ / 10
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        if(motionreduceonend.get()) {
            fly.needReset = false
        }
        if(velocitypacket) {
            val yaw = Math.toRadians(player.rotationYaw.toDouble())
            when(mode.get().lowercase()) {
                "stable" -> {
                    timer.timerSpeed = 1.0F
                    player.motionX += (-sin(yaw) * 0.416)
                    player.motionZ += (cos(yaw) * 0.416)
                    player.motionY = packetymotion

                    if(tick++ >=27) {
                        resetmotion()
                        timer.timerSpeed = 1.0f
                        velocitypacket = false
                        packetymotion = 0.0
                        tick = 0
                    }
                }
                "test"-> {
                    if (tick++ >= 4) {
                        timer.timerSpeed = 1.1F
                        player.motionX += (-sin(yaw) * 0.420)
                        player.motionZ += (cos(yaw) * 0.420)
                    } else {
                        timer.timerSpeed = 0.9F
                        player.motionX += (-sin(yaw) * 0.330)
                        player.motionZ += (cos(yaw) * 0.330)
                    }
                    player.motionY = packetymotion
                    if (tick++ >= 27) {
                        resetmotion()
                        timer.timerSpeed = 1.0f
                        velocitypacket = false
                        packetymotion = 0.0
                        tick = 0
                    }
                }
                "custom" -> {
                    if(customstrafe.get())
                        MovementUtils.strafe()
                    randomNum = if (randomize.get()) Math.random() * randomAmount.get() * 0.01 else 0.0
                    timer.timerSpeed = timerspeed.get()
                    player.motionX += (-sin(yaw) * (0.3 + (speedBoost.get().toDouble() / 10 ) + randomNum))
                    player.motionZ += (cos(yaw) * (0.3 + (speedBoost.get().toDouble() / 10 ) + randomNum))
                    player.motionY = packetymotion
                    if(tick++ >=boostTicks.get()) {
                        resetmotion()
                        timer.timerSpeed = 1.0f
                        velocitypacket = false
                        packetymotion = 0.0
                        tick = 0
                    }
                }
            }

        }
    }

    override fun onDisable() {
        timer.timerSpeed = 1f
        resetmotion()
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S12PacketEntityVelocity) {
            if (player == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != player) return
            if(packet.motionY / 8000.0 > 0.2) {
                velocitypacket = true
                packetymotion = packet.motionY / 8000.0
            }
        }
    }
}
