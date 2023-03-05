package net.ccbluex.liquidbounce.features.module.modules.movement.flys.matrix

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.network.play.server.S12PacketEntityVelocity
import kotlin.math.cos
import kotlin.math.sin


class OldMatrixDamageFly : FlyMode("OldMatrixDamage") {


    private val mode = ListValue("${valuePrefix}Mode", arrayOf("Stable","Test","Custom"), "Stable")
    private val warn = BoolValue("${valuePrefix}DamageWarn",true)
    private val timer = FloatValue("${valuePrefix}Timer", 1.0f, 0f, 2f).displayable { mode.equals("Custom") }
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

    override fun onEnable() {
        if (warn.get())
            ClientUtils.displayChatMessage("§8[§c§lMatrix-Dmg-Fly§8] §aGetting damage from other entities (players, arrows, snowballs, eggs...) is required to bypass.")
        velocitypacket = false
        packetymotion = 0.0
        tick = 0
    }

    private fun resetmotion() {
        if(motionreduceonend.get()) {
            mc.thePlayer.motionX = mc.thePlayer.motionX / 10
            mc.thePlayer.motionY = mc.thePlayer.motionY / 10
            mc.thePlayer.motionZ = mc.thePlayer.motionZ / 10
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        if(motionreduceonend.get()) {
            fly.needReset = false
        }
        if(velocitypacket) {
            val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
            when(mode.get().lowercase()) {
                "stable" -> {
                    mc.timer.timerSpeed = 1.0F
                    mc.thePlayer.motionX += (-sin(yaw) * 0.416)
                    mc.thePlayer.motionZ += (cos(yaw) * 0.416)
                    mc.thePlayer.motionY = packetymotion

                    if(tick++ >=27) {
                        resetmotion()
                        mc.timer.timerSpeed = 1.0f
                        velocitypacket = false
                        packetymotion = 0.0
                        tick = 0
                    }
                }
                "test"-> {
                    if (tick++ >= 4) {
                        mc.timer.timerSpeed = 1.1F
                        mc.thePlayer.motionX += (-sin(yaw) * 0.420)
                        mc.thePlayer.motionZ += (cos(yaw) * 0.420)
                    } else {
                        mc.timer.timerSpeed = 0.9F
                        mc.thePlayer.motionX += (-sin(yaw) * 0.330)
                        mc.thePlayer.motionZ += (cos(yaw) * 0.330)
                    }
                    mc.thePlayer.motionY = packetymotion
                    if (tick++ >= 27) {
                        resetmotion()
                        mc.timer.timerSpeed = 1.0f
                        velocitypacket = false
                        packetymotion = 0.0
                        tick = 0
                    }
                }
                "custom" -> {
                    if(customstrafe.get())
                        MovementUtils.strafe()
                    randomNum = if (randomize.get()) Math.random() * randomAmount.get() * 0.01 else 0.0
                    mc.timer.timerSpeed = timer.get()
                    mc.thePlayer.motionX += (-sin(yaw) * (0.3 + (speedBoost.get().toDouble() / 10 ) + randomNum))
                    mc.thePlayer.motionZ += (cos(yaw) * (0.3 + (speedBoost.get().toDouble() / 10 ) + randomNum))
                    mc.thePlayer.motionY = packetymotion
                    if(tick++ >=boostTicks.get()) {
                        resetmotion()
                        mc.timer.timerSpeed = 1.0f
                        velocitypacket = false
                        packetymotion = 0.0
                        tick = 0
                    }
                }
            }

        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
        resetmotion()
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S12PacketEntityVelocity) {
            if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer) return
            if(packet.motionY / 8000.0 > 0.2) {
                velocitypacket = true
                packetymotion = packet.motionY / 8000.0
            }
        }
    }
}
