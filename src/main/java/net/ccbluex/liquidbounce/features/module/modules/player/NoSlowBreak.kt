/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object NoSlowBreak : Module("NoSlowBreak", Category.PLAYER, Category.SubCategory.PLAYER_ASSIST) {
    @JvmField
    val miningFatigueValue = boolean("MiningFatigue", true)
        .describe("Remove the mining-fatigue break-speed penalty.")
    @JvmField
    val onAirValue = boolean("OnAir", true)
        .describe("Remove the in-air break-speed penalty.")
    @JvmField
    val underwaterValue = boolean("Underwater", true)
        .describe("Remove the underwater break-speed penalty without Aqua Affinity.")

    @JvmStatic val miningFatigue get() = handleEvents() && miningFatigueValue.get()
    @JvmStatic val onAir get() = handleEvents() && onAirValue.get()
    @JvmStatic val underwater get() = handleEvents() && underwaterValue.get()
}
