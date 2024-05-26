/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.reverse

import me.zywl.fdpclient.event.PacketEvent
import me.zywl.fdpclient.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.value.FloatValue

class SmoothReverseVelocity : VelocityMode("SmoothReverse") {
    private val smoothReverseStrengthValue = FloatValue("SmoothReverseStrength", 0.05F, 0.02F, 0.1F)
    private var reverseHurt = false

    override fun onEnable() {
        reverseHurt = false
    }

    override fun onVelocityPacket(event: PacketEvent) {
        velocity.velocityInput = true
    }

    override fun onVelocity(event: UpdateEvent) {
        if (!velocity.velocityInput) {
            mc.thePlayer.speedInAir = 0.02F
            return
        }

        if (mc.thePlayer.hurtTime > 0) {
            reverseHurt = true
        }

        if (!mc.thePlayer.onGround) {
            if (reverseHurt) {
                mc.thePlayer.speedInAir = smoothReverseStrengthValue.get()
            }
        } else if (velocity.velocityTimer.hasTimePassed(80L)) {
            velocity.velocityInput = false
            reverseHurt = false
        }
    }
}