/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.verus

import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.movement.MovementUtils
import net.minecraft.potion.Potion

object LatestVerusHop : SpeedMode("LatestVerusHop") {
    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        if (!player.isMoving) {
            return
        }

        val hasPotion = player.isPotionActive(Potion.moveSpeed)
        player.jumpMovementFactor = if (Speed.latestVerusHopCustomSpeed) {
            if (hasPotion) Speed.latestVerusHopJumpMovementFactorWithPotion
            else Speed.latestVerusHopJumpMovementFactorWithoutPotion
        } else {
            0.02f
        }

        if (Speed.latestVerusHopDamageBoost && player.hurtTime == 9) {
            MovementUtils.strafe(Speed.latestVerusHopBoostSpeed)
        }

        player.speedInAir = if (Speed.latestVerusHopCustomSpeed) {
            (if (hasPotion) Speed.latestVerusHopSpeedWithPotion else Speed.latestVerusHopSpeedWithoutPotion) / 100f
        } else {
            if (hasPotion) 0.028f else 0.02f
        }

        mc.gameSettings.keyBindJump.pressed = false

        if (player.onGround) {
            player.jump()
            player.motionY = 0.41999998688697815
            MovementUtils.strafe(
                if (Speed.latestVerusHopCustomSpeed) {
                    if (hasPotion) Speed.latestVerusHopFrictionWithPotion else Speed.latestVerusHopFrictionWithoutPotion
                } else {
                    0.48f
                }
            )
        }

        MovementUtils.strafe()
    }

    override fun onDisable() {
        mc.thePlayer?.speedInAir = 0.02f
        mc.gameSettings.keyBindJump.pressed = net.minecraft.client.settings.GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
    }
}
