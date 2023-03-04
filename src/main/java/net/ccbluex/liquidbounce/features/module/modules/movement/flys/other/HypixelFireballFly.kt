package net.ccbluex.liquidbounce.features.module.modules.movement.flys.matrix

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.minecraft.network.play.server.S27PacketExplosion 
import kotlin.math.cos
import kotlin.math.sin


class HypixelFireballFly : FlyMode("HypixelFireball") {


    private val warn = BoolValue("${valuePrefix}DamageWarn",true)
    private val timer = FloatValue("${valuePrefix}Timer", 1.0f, 0f, 2f)

    private var velocitypacket = false
    private var tick = 0
    private var mSpeed = 0f
    private var yaw = 0.0


    override fun onEnable() {
        if (warn.get())
            ClientUtils.displayChatMessage("§8[§c§lHypixel-Fireball-Fly§8] §aGetting exlposion from a fireball or tnt is required to bypass.")
        velocitypacket = false
        tick = 0
    }


    override fun onUpdate(event: UpdateEvent) {
        mSpeed = MovementUtils.getSpeed()

        if(velocitypacket) {
            tick++
            if (tick == 0) {
                yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
                mc.thePlayer.motionY = 1.45
                mc.thePlayer.motionX = (-sin(yaw) * 1.4)
                mc.thePlayer.motionZ = (cos(yaw) * 1.4)
            } else if (tick == 1) {
                mc.thePlayer.motionX = (-sin(yaw) * 1.85)
                mc.thePlayer.motionZ = (cos(yaw) * 1.85)
            } else if (tick < 12) {
                mc.thePlayer.motionX = (-sin(yaw) * mSpeed.toDouble() * 0.99)
                mc.thePlayer.motionZ = (cos(yaw) * mSpeed.toDouble() * 0.99)

            }
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S27PacketExplosion ) {
            tick = 0
            velocitypacket = true
        }
    }
}
