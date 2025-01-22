/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object KeepSprint : Module("KeepSprint", Category.COMBAT) {
    val motionAfterAttackOnGround by float("MotionAfterAttackOnGround", 0.6f, 0.0f..1f)
    val motionAfterAttackInAir by float("MotionAfterAttackInAir", 0.6f, 0.0f..1f)

    val motionAfterAttack
        get() = if (mc.thePlayer.onGround) motionAfterAttackOnGround else motionAfterAttackInAir
}