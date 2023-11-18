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
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.minecraft.network.play.server.S27PacketExplosion

class FireballFly : FlyMode("Fireball") {


    private val warn = BoolValue("${valuePrefix}DamageWarn",true)
    private val boostValue = FloatValue("${valuePrefix}BoostAmount", 1.4f, 0f, 2f)
    private val frictionValue = FloatValue("${valuePrefix}Friction", 0.99f, 0.9f, 1f)
    private val frictionDurationValue = IntegerValue("${valuePrefix}FrictionDuration", 12, 3, 20)
    private val modifyYmotionValue = BoolValue("${valuePrefix}ModifyYmotion", true)
    private val yMotionValue = FloatValue("${valuePrefix}Ymotion", 1.4f, 0.42f, 3f)
    private val timerValue = FloatValue("${valuePrefix}Timer", 1.0f, 0f, 2f)

    private var velocitypacket = false
    private var tick = 0
    private var mSpeed = 0f

    override fun onEnable() {
        if (warn.get())
            ClientUtils.displayChatMessage("§8[§c§lFireball-Flight§8] §aGetting exlposion from a fireball or tnt is required to bypass.")
        velocitypacket = false
        tick = 0
    }

    override fun onUpdate(event: UpdateEvent) {
        mc.timer.timerSpeed = 1.0f

        if(velocitypacket) {
            mc.timer.timerSpeed = timerValue.get()
            if (tick == 0) {
                if (modifyYmotionValue.get()) {
                    mc.thePlayer.motionY = yMotionValue.get().toDouble()
                } else {
                    mc.thePlayer.jump()
                }
                    
                MovementUtils.strafe(boostValue.get())
                mSpeed = boostValue.get()
            } else if (tick < frictionDurationValue.get()) {
                mSpeed *= frictionValue.get()
                MovementUtils.strafe(mSpeed)
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
