/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.reverse

import me.zywl.fdpclient.event.PacketEvent
import me.zywl.fdpclient.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue

class ReverseVelocity : VelocityMode("Reverse") {
    private val reverseStrengthValue = FloatValue("ReverseStrength", 1F, 0.1F, 1F)
    private val reverseTimeValue = IntegerValue("ReverseTime", 80, 10, 500)
    private val reverseDelayValue = IntegerValue("ReverseDelay", 30, 0, 500)

    override fun onVelocity(event: UpdateEvent) {
        if (!velocity.velocityInput) {
            return
        }
        if (velocity.velocityTimer.hasTimePassed(reverseDelayValue.get().toLong())) {
            MovementUtils.strafe(MovementUtils.getSpeed() * reverseStrengthValue.get())
        }
        if (velocity.velocityTimer.hasTimePassed(reverseTimeValue.get().toLong())) {
            velocity.velocityInput = false
        }
    }

    override fun onVelocityPacket(event: PacketEvent) {
        velocity.velocityInput = true
    }
}
