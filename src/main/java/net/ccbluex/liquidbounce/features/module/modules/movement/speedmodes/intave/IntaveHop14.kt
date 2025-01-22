/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.intave

import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.extensions.isInLiquid
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.extensions.isMoving

/*
* Working on Intave: 14
* Tested on: mc.mineblaze.net
* Credit: @thatonecoder & @larryngton / Intave14
*/
object IntaveHop14 : SpeedMode("IntaveHop14") {

    private const val BOOST_CONSTANT = 0.003

    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        if (!player.isMoving || player.isInLiquid || player.isInWeb || player.isOnLadder) return

        if (player.onGround) {
            player.motionY = 0.42 - if (Speed.intaveLowHop) 1.7E-14 else 0.0

            if (player.isSprinting) strafe(strength = Speed.strafeStrength.toDouble())

            mc.timer.timerSpeed = Speed.groundTimer
        } else {
            mc.timer.timerSpeed = Speed.airTimer
        }

        if (Speed.boost && player.motionY > 0.003 && player.isSprinting) {
            player.motionX *= 1f + (BOOST_CONSTANT * Speed.initialBoostMultiplier)
            player.motionZ *= 1f + (BOOST_CONSTANT * Speed.initialBoostMultiplier)
        }
    }
}