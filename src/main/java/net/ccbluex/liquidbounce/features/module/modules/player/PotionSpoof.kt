package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect

object PotionSpoof : Module("PotionSpoof", Category.PLAYER, Category.SubCategory.PLAYER_ASSIST) {

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
        Potion.moveSpeed.id to speedValue,
        Potion.moveSlowdown.id to moveSlowDownValue,
        Potion.digSpeed.id to hasteValue,
        Potion.digSlowdown.id to digSlowDownValue,
        Potion.blindness.id to blindnessValue,
        Potion.damageBoost.id to strengthValue,
        Potion.jump.id to jumpBoostValue,
        Potion.weakness.id to weaknessValue,
        Potion.regeneration.id to regenerationValue,
        Potion.wither.id to witherValue,
        Potion.resistance.id to resistanceValue,
        Potion.fireResistance.id to fireResistanceValue,
        Potion.absorption.id to absorptionValue,
        Potion.healthBoost.id to healthBoostValue,
        Potion.poison.id to poisonValue,
        Potion.saturation.id to saturationValue,
        Potion.waterBreathing.id to waterBreathingValue
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