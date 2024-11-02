/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other


import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.potion.Potion

object RemoveEffect : Module("RemoveEffect", Category.OTHER, hideModule = false) {

    private val shouldRemoveSlowness by BoolValue("Slowness", false)
    private val shouldRemoveMiningFatigue by BoolValue("Mining Fatigue", false)
    private val shouldRemoveBlindness by BoolValue("Blindness", false)
    private val shouldRemoveWeakness by BoolValue("Weakness", false)
    private val shouldRemoveWither by BoolValue("Wither", false)
    private val shouldRemovePoison by BoolValue("Poison", false)
    private val shouldRemoveWaterBreathing by BoolValue("Water Breathing", false)

    override fun onEnable() {}

    @EventTarget(ignoreCondition = true)
    fun onUpdate(event: UpdateEvent?) {

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
