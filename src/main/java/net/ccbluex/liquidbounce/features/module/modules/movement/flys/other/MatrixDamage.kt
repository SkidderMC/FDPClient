package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.network.play.server.S12PacketEntityVelocity
import kotlin.math.sin
import kotlin.math.cos


class MatrixDamage : FlyMode("MatrixDamage") {

    private val warn = BoolValue("${valuePrefix}DamageWarn",true)
    private val speedBoost = FloatValue("${valuePrefix}BoostSpeed", 1.15f, 0f, 3f)
    private val timer = FloatValue("${valuePrefix}Timer", 0.9f, 0f, 2f)
    private val boostTicks = IntegerValue("${valuePrefix}BoostTicks", 27,10,40)

    private var velocitydetect = false
    private var velocitypacket = false
    private var damage = false
    private var ticks = false
    private var packetymotion = 0.0
    private var tick = 0

    override fun onEnable() {
        if (warn.get()) {
            ClientUtils.displayChatMessage("§8[§c§lMatrix-Dmg-Fly§8] §aU need make some damage to boost : bow , snowball , eggs...")
        }
        velocitydetect = false
        velocitypacket = false
        damage = false
        packetymotion = 0.0
        tick = 0
        ticks = false
    }

    override fun onUpdate(event: UpdateEvent) {
        if(mc.thePlayer.ticksExisted == 9) {
            ticks = true
        }
        if(velocitydetect && velocitypacket && ticks) {
            mc.timer.timerSpeed = timer.get()
            val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
            mc.thePlayer.motionX += (-sin(yaw) * (0.3 + (speedBoost.get().toDouble() / 10 ) + Math.random() * 0.03))
            mc.thePlayer.motionZ += (cos(yaw) * (0.3 + (speedBoost.get().toDouble() / 10 ) + Math.random() * 0.03))
            mc.thePlayer.motionY = packetymotion
            tick++
            if(tick>=boostTicks.get()) {
                mc.timer.timerSpeed = 1.0f
                velocitydetect = false
                velocitypacket = false
                damage = false
                packetymotion = 0.0
                tick = 0
                ticks = false
            }

        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S12PacketEntityVelocity) {
            if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer) return
            if(packet.motionY / 8000.0 > 0) {
                velocitydetect = true
            }
            if(packet.motionY / 8000.0 > 0.2) {
                velocitypacket = true
                packetymotion = packet.motionY / 8000.0
            }
        }
    }
}
