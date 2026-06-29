/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object NoMissCooldown : Module("NoMissCooldown", Category.COMBAT, Category.SubCategory.COMBAT_LEGIT) {
    @JvmField
    val removeAttackCooldownValue = boolean("RemoveAttackCooldown", true)
        .describe("Remove the vanilla ten-tick cooldown after an attack misses.")

    @JvmField
    val cancelAttackOnMissValue = boolean("CancelAttackOnMiss", false)
        .describe("Do not send or animate attacks when the crosshair has no target.")

    @JvmStatic
    val removeAttackCooldown get() = handleEvents() && removeAttackCooldownValue.get()

    @JvmStatic
    val cancelAttackOnMiss get() = handleEvents() && cancelAttackOnMissValue.get()
}
