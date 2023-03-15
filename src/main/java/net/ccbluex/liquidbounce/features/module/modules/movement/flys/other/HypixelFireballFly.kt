/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.minecraft.network.play.server.S27PacketExplosion

class HypixelFireballFly : FlyMode("HypixelFireball") {


    private val warn = BoolValue("${valuePrefix}DamageWarn",true)
    private val timerValue = FloatValue("${valuePrefix}Timer", 1.0f, 0f, 2f)

    private var velocitypacket = false
    private var tick = 0
    private var mSpeed = 0f

    override fun onEnable() {
        if (warn.get())
            ClientUtils.displayChatMessage("§8[§c§lHypixel-Fireball-Fly§8] §aGetting exlposion from a fireball or tnt is required to bypass.")
        velocitypacket = false
        tick = 0
    }

    override fun onUpdate(event: UpdateEvent) {
        mSpeed = MovementUtils.getSpeed()
        mc.timer.timerSpeed = 1.0f

        if(velocitypacket) {
            mc.timer.timerSpeed = timerValue.get()
            if (tick == 0) {
                mc.thePlayer.motionY = 1.5
                MovementUtils.strafe(1.4f)
            } else if (tick == 1) {
                MovementUtils.strafe(1.4f)
            } else if (tick < 12) {
                MovementUtils.strafe(mSpeed * 0.99f)
            } else {
                velocitypacket = false
                fly.state = false
            }
            tick++
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
