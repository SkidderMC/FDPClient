/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.event.handler
import net.minecraft.potion.Potion

object RemoveEffect : Module("RemoveEffect", Category.OTHER, Category.SubCategory.MISCELLANEOUS) {

    private val shouldRemoveSlowness by boolean("Slowness", false)
        .describe("Client-side remove the slowness effect.")
    private val shouldRemoveMiningFatigue by boolean("Mining Fatigue", false)
        .describe("Client-side remove the mining fatigue effect.")
    private val shouldRemoveBlindness by boolean("Blindness", false)
        .describe("Client-side remove the blindness effect.")
    private val shouldRemoveWeakness by boolean("Weakness", false)
        .describe("Client-side remove the weakness effect.")
    private val shouldRemoveWither by boolean("Wither", false)
        .describe("Client-side remove the wither effect.")
    private val shouldRemovePoison by boolean("Poison", false)
        .describe("Client-side remove the poison effect.")
    private val shouldRemoveWaterBreathing by boolean("Water Breathing", false)
        .describe("Client-side remove the water breathing effect.")

    override fun onEnable() {}

    val onUpdate = handler<UpdateEvent> (always = true) {

        if (mc.thePlayer != null) {

            val effectIdsToRemove = mutableListOf<Int>()
            if (shouldRemoveSlowness) mc.thePlayer.removePotionEffectClient(Potion.moveSlowdown.id)
            if (shouldRemoveMiningFatigue) mc.thePlayer.removePotionEffectClient(Potion.digSlowdown.id)
            if (shouldRemoveBlindness) mc.thePlayer.removePotionEffectClient(Potion.blindness.id)
            if (shouldRemoveWeakness) mc.thePlayer.removePotionEffectClient(Potion.weakness.id)
            if (shouldRemoveWither) effectIdsToRemove.add(Potion.wither.id)
            if (shouldRemovePoison) effectIdsToRemove.add(Potion.poison.id)
            if (shouldRemoveWaterBreathing) effectIdsToRemove.add(Potion.waterBreathing.id)

            for (effectId in effectIdsToRemove) {
                mc.thePlayer.removePotionEffectClient(effectId)
            }
        }
    }
}
