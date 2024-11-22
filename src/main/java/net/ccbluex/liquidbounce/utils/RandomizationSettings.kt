/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.boolean
import net.ccbluex.liquidbounce.value.floatRange

class RandomizationSettings(owner: Module, generalApply: () -> Boolean = { true }) {

    val randomize by boolean("RandomizeRotations", false) { generalApply() }
    val yawRandomizationChance by floatRange("YawRandomizationChance", 0.8f..1.0f, 0f..1f) { randomize }
    val yawRandomizationRange by floatRange("YawRandomizationRange", 5f..10f, 0f..30f)
    { randomize && yawRandomizationChance.endInclusive != 0f }
    val pitchRandomizationChance by floatRange("PitchRandomizationChance", 0.8f..1.0f, 0f..1f) { randomize }
    val pitchRandomizationRange by floatRange("PitchRandomizationRange", 5f..10f, 0f..30f)
    { randomize && pitchRandomizationChance.endInclusive != 0f }

    init {
        owner.addConfigurable(this)
    }
}