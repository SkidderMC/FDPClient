/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils

object KeepSprint : Module("KeepSprint", Category.COMBAT, Category.SubCategory.COMBAT_LEGIT) {
    private val chance by int("Chance", 100, 0..100, "%")
        .describe("Chance to keep sprint motion after an attack.")
    val motionAfterAttackOnGround by float("MotionAfterAttackOnGround", 0.6f, 0.0f..1f)
        .describe("Motion kept after attacking on the ground.")
    val motionAfterAttackInAir by float("MotionAfterAttackInAir", 0.6f, 0.0f..1f)
        .describe("Motion kept after attacking in the air.")
    private val motionWhenHurt by float("MotionWhenHurt", 0.6f, 0.0f..1f)
        .describe("Motion kept while recently hurt.")
    private val hurtTimeRange by intRange("HurtTime", 1..10, 1..10)
        .describe("Hurt-time window that uses the hurt motion value.")

    val motionAfterAttack: Float
        get() {
            if (RandomUtils.nextInt(0, 100) >= chance) return 0.6f
            val player = mc.thePlayer ?: return 0.6f
            if (player.hurtTime in hurtTimeRange) return motionWhenHurt
            return if (player.onGround) motionAfterAttackOnGround else motionAfterAttackInAir
        }
}