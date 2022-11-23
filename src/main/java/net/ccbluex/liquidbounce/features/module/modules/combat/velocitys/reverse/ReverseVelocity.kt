package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.reverse

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.utils.MovementUtils

class ReverseVelocity : VelocityMode("Reverse") {
    private val reverseStrengthValue = FloatValue("ReverseStrength", 1F, 0.1F, 1F)

    override fun onVelocity(event: UpdateEvent) {
        if (!velocity.velocityInput) {
            return
        }

        if (!mc.thePlayer.onGround) {
            MovementUtils.strafe(MovementUtils.getSpeed() * reverseStrengthValue.get())
        } else if (velocity.velocityTimer.hasTimePassed(80L)) {
            velocity.velocityInput = false
        }
    }

    override fun onVelocityPacket(event: PacketEvent) {
        velocity.velocityInput = true
    }
}