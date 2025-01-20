/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.rotation

import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.features.module.Module

class RandomizationSettings(owner: Module, generalApply: () -> Boolean = { true }): Configurable("Randomization") {

    val randomize by boolean("RandomizeRotations", false) { generalApply() }
    val yawRandomizationChance by floatRange("YawRandomizationChance", 0.8f..1.0f, 0f..1f) { randomize }
    val yawRandomizationRange by floatRange("YawRandomizationRange", 5f..10f, 0f..30f)
    { randomize && yawRandomizationChance.start != 1F }
    val pitchRandomizationChance by floatRange("PitchRandomizationChance", 0.8f..1.0f, 0f..1f) { randomize }
    val pitchRandomizationRange by floatRange("PitchRandomizationRange", 5f..10f, 0f..30f)
    { randomize && pitchRandomizationChance.start != 1F }

    init {
        owner.addValues(this.values)
    }
}