package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.matrix

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.minecraft.network.play.server.S12PacketEntityVelocity
import kotlin.math.cos
import kotlin.math.sin

class MatrixHurtLongjump : LongJumpMode("MatrixHurt") {
    private val boostSpeed = FloatValue("${valuePrefix}BoostSpeed", 0.416f, 0.1f, 1.0f)
    private val ticks = IntegerValue("${valuePrefix}Ticks", 10, 5, 20)
    private var detected = false
    private var motiony = 0.0
    private var tick = 0
    override fun onEnable() {
        detected = false
        motiony = 0.0
        tick = 0
        sendLegacy()
    }
    override fun onUpdate(event: UpdateEvent) {
        if(detected) {
            val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
            mc.thePlayer.motionY = motiony
            mc.thePlayer.motionX += -(sin(yaw) * boostSpeed.get())
            mc.thePlayer.motionZ += (cos(yaw) * boostSpeed.get())
            tick++
            if(tick>=ticks.get()) {
                tick = 0
                detected = false
                motiony = 0.0
                onAttemptDisable()
            }
        }
    }
    override fun onPacket(event: PacketEvent) {
        if(event.packet is S12PacketEntityVelocity) {
            if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(event.packet.entityID) ?: return) != mc.thePlayer) return
            if(event.packet.motionY / 8000.0 > 0.2) {
                detected = true
                motiony = event.packet.motionY / 8000.0
            }
        }
    }
    override fun onAttemptDisable() {
        longjump.state = false
    }
}
