package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect
import net.ccbluex.liquidbounce.config.Configurable

object PotionSpoof : Module("PotionSpoof", Category.PLAYER, Category.SubCategory.PLAYER_ASSIST) {

    private val level by int("PotionLevel", 2, 1..5).onChanged {
        onDisable()
    }

    private val speedValue = boolean("Speed", false)
        .describe("Spoof the speed potion effect.")
    private val moveSlowDownValue = boolean("Slowness", false)
        .describe("Spoof the slowness potion effect.")
    private val hasteValue = boolean("Haste", false)
        .describe("Spoof the haste potion effect.")
    private val digSlowDownValue = boolean("MiningFatigue", false)
        .describe("Spoof the mining fatigue potion effect.")
    private val blindnessValue = boolean("Blindness", false)
        .describe("Spoof the blindness potion effect.")
    private val strengthValue = boolean("Strength", false)
        .describe("Spoof the strength potion effect.")
    private val jumpBoostValue = boolean("JumpBoost", false)
        .describe("Spoof the jump boost potion effect.")
    private val weaknessValue = boolean("Weakness", false)
        .describe("Spoof the weakness potion effect.")
    private val regenerationValue = boolean("Regeneration", false)
        .describe("Spoof the regeneration potion effect.")
    private val witherValue = boolean("Wither", false)
        .describe("Spoof the wither potion effect.")
    private val resistanceValue = boolean("Resistance", false)
        .describe("Spoof the resistance potion effect.")
    private val fireResistanceValue = boolean("FireResistance", false)
        .describe("Spoof the fire resistance potion effect.")
    private val absorptionValue = boolean("Absorption", false)
        .describe("Spoof the absorption potion effect.")
    private val healthBoostValue = boolean("HealthBoost", false)
        .describe("Spoof the health boost potion effect.")
    private val poisonValue = boolean("Poison", false)
        .describe("Spoof the poison potion effect.")
    private val saturationValue = boolean("Saturation", false)
        .describe("Spoof the saturation potion effect.")
    private val waterBreathingValue = boolean("WaterBreathing", false)
        .describe("Spoof the water breathing potion effect.")

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

    private val generalGroup = Configurable("General")
    private val effectsGroup = Configurable("Effects")

    init {
        moveValues(generalGroup, "PotionLevel")
        moveValues(effectsGroup, "Speed", "Slowness", "Haste", "MiningFatigue", "Blindness", "Strength", "JumpBoost", "Weakness", "Regeneration", "Wither", "Resistance", "FireResistance", "Absorption", "HealthBoost", "Poison", "Saturation", "WaterBreathing")
        addValues(listOf(generalGroup, effectsGroup))
    }

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