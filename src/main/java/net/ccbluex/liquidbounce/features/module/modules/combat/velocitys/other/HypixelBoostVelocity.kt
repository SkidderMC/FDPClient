/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.utils.MovementUtils
class HypixelBoostVelocity : VelocityMode("HypixelBoost") {

    override fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.hurtTime == 8) {
            MovementUtils.strafe(MovementUtils.getSpeed() * 0.7f)
        }
    }
}
