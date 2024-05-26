/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.aac

import me.zywl.fdpclient.FDPClient
import me.zywl.fdpclient.event.JumpEvent
import me.zywl.fdpclient.event.PacketEvent
import me.zywl.fdpclient.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode

class AACZeroVelocity : VelocityMode("AACZero") {

    override fun onVelocity(event: UpdateEvent) {
        if (mc.thePlayer.hurtTime > 0) {
            if (!velocity.velocityInput || mc.thePlayer.onGround || mc.thePlayer.fallDistance > 2F) {
                return
            }

            mc.thePlayer.addVelocity(0.0, -1.0, 0.0)
            mc.thePlayer.onGround = true
        } else {
            velocity.velocityInput = false
        }
    }

    override fun onVelocityPacket(event: PacketEvent) {
        velocity.velocityInput = true
    }

    override fun onJump(event: JumpEvent) {
        if (mc.thePlayer.isInWater || mc.thePlayer.isInLava || mc.thePlayer.isInWeb || (velocity.onlyGroundValue.get() && !mc.thePlayer.onGround)) {
            return
        }

        if ((velocity.onlyGroundValue.get() && !mc.thePlayer.onGround) || (velocity.onlyCombatValue.get() && !FDPClient.combatManager.inCombat)) {
            return
        }

        if (mc.thePlayer.hurtTime > 0) {
            event.cancelEvent()
        }
    }
}