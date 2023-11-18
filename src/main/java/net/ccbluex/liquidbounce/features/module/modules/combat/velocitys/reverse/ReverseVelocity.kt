package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.reverse

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.utils.MovementUtils

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
