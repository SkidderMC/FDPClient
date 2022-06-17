package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.network.play.server.S12PacketEntityVelocity
import kotlin.math.sin
import kotlin.math.cos


class MatrixDamage : FlyMode("MatrixDamage") {
    
    private val warn = BoolValue("DamageWarn",true)
    private val speedBoost = FloatValue("BoostSpeed", 1.15f, 0f, 3f)
    private val timer = FloatValue("Timer", 0.9f, 0f, 2f)
    private val boostTicks = IntegerValue("BoostTicks", 27,10,40)
    
    private var can = false
    private var can2 = false
    private var damage = false
    private var ya = false
    private var motion = 0.0
    private var tick = 0

    override fun onEnable() {
        if (warn.get()) {
            ClientUtils.displayChatMessage("[Matrix-fly] U need make some damage to boost fly : bow , snowball , eggs...")
        }
        can = false
        can2 = false
        damage = false
        motion = 0.0
        tick = 0
        ya = false
    }

    override fun onUpdate(event: UpdateEvent) {
        mc.timer.timerSpeed = timer.get()
        if(mc.thePlayer.ticksExisted == 9) {
            ya = true
        }
        if(can && can2) {
            val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
            mc.thePlayer.motionX += (-sin(yaw) * (0.3 + (speedBoost.get().toDouble() / 10 ) + Math.random() * 0.03) )
            mc.thePlayer.motionZ += (cos(yaw) * (0.3 + (speedBoost.get().toDouble() / 10 ) + Math.random() * 0.03))
            mc.thePlayer.motionY = motion
            tick++
            if(tick>=boostTicks.get()) {
                mc.timer.timerSpeed = 1.0f
                can = false
                can2 = false
                damage = false
                motion = 0.0
                tick = 0
                ya = false
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
                can = true
            }
            if(packet.motionY / 8000.0 > 0.2) {
                can2 = true
                motion = packet.motionY / 8000.0
            }
        }
    }
}
