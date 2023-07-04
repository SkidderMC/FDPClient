/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.minecraft.potion.Potion

@ModuleInfo(name = "RemoveEffect", category = ModuleCategory.COMBAT)
object RemoveEffect : Module() {

    private val shouldRemoveSlowness = BoolValue("Slowness", false)
    private val shouldRemoveMiningFatigue = BoolValue("MiningFatigue", false)
    private val shouldRemoveBlindness = BoolValue("Blindness", false)
    private val shouldRemoveWeakness = BoolValue("Weakness", false)
    private val shouldRemoveWither = BoolValue("Wither", false)
    private val shouldRemovePoison = BoolValue("Poison", false)
    private val shouldRemoveWaterBreathing = BoolValue("WaterBreathing", false)

    override fun onEnable() {}

    @EventTarget(ignoreCondition = true)
    fun onUpdate(event: UpdateEvent?) {

        if (mc.thePlayer != null) {

            val effectIdsToRemove = mutableListOf<Int>()
            if (shouldRemoveSlowness.get()) effectIdsToRemove.add(Potion.moveSlowdown.id)
            if (shouldRemoveMiningFatigue.get()) effectIdsToRemove.add(Potion.digSlowdown.id)
            if (shouldRemoveBlindness.get()) effectIdsToRemove.add(Potion.blindness.id)
            if (shouldRemoveWeakness.get()) effectIdsToRemove.add(Potion.weakness.id)
            if (shouldRemoveWither.get()) effectIdsToRemove.add(Potion.wither.id)
            if (shouldRemovePoison.get()) effectIdsToRemove.add(Potion.poison.id)
            if (shouldRemoveWaterBreathing.get()) effectIdsToRemove.add(Potion.waterBreathing.id)

            for (effectId in effectIdsToRemove) {
                mc.thePlayer.removePotionEffectClient(effectId)
            }
        }
    }
}
