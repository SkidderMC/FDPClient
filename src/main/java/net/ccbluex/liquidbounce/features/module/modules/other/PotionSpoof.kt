/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.potion.Potion.*
import net.minecraft.potion.PotionEffect

object PotionSpoof : Module("PotionSpoof", Category.PLAYER) {

    private val level by int("PotionLevel", 2, 1..5).onChanged {
        onDisable()
    }

    private val speedValue = boolean("Speed", false)
    private val moveSlowDownValue = boolean("Slowness", false)
    private val hasteValue = boolean("Haste", false)
    private val digSlowDownValue = boolean("MiningFatigue", false)
    private val blindnessValue = boolean("Blindness", false)
    private val strengthValue = boolean("Strength", false)
    private val jumpBoostValue = boolean("JumpBoost", false)
    private val weaknessValue = boolean("Weakness", false)
    private val regenerationValue = boolean("Regeneration", false)
    private val witherValue = boolean("Wither", false)
    private val resistanceValue = boolean("Resistance", false)
    private val fireResistanceValue = boolean("FireResistance", false)
    private val absorptionValue = boolean("Absorption", false)
    private val healthBoostValue = boolean("HealthBoost", false)
    private val poisonValue = boolean("Poison", false)
    private val saturationValue = boolean("Saturation", false)
    private val waterBreathingValue = boolean("WaterBreathing", false)

    private val potionMap = mapOf(
        moveSpeed.id to speedValue,
        moveSlowdown.id to moveSlowDownValue,
        digSpeed.id to hasteValue,
        digSlowdown.id to digSlowDownValue,
        blindness.id to blindnessValue,
        damageBoost.id to strengthValue,
        jump.id to jumpBoostValue,
        weakness.id to weaknessValue,
        regeneration.id to regenerationValue,
        wither.id to witherValue,
        resistance.id to resistanceValue,
        fireResistance.id to fireResistanceValue,
        absorption.id to absorptionValue,
        healthBoost.id to healthBoostValue,
        poison.id to poisonValue,
        saturation.id to saturationValue,
        waterBreathing.id to waterBreathingValue
    )

    override fun onDisable() {
        mc.thePlayer ?: return

        mc.thePlayer.activePotionEffects
            .filter { it.duration == 0 && potionMap[it.potionID]?.get() == true }
            .forEach { mc.thePlayer.removePotionEffect(it.potionID) }
    }

    val onUpdate = handler<UpdateEvent> {
        potionMap.forEach { (potionId, value) ->
            if (value.get())
                mc.thePlayer.addPotionEffect(PotionEffect(potionId, 0, level - 1, false, false))
            else if (mc.thePlayer.activePotionEffects.any { it.duration == 0 && it.potionID == potionId })
                mc.thePlayer.removePotionEffect(potionId)
        }
    }
}