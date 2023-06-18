/*
 * Liquidlite Ghost Client
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.minecraft.entity.EntityLivingBase

object TickBase : Module(name = "TickBase", category = ModuleCategory.COMBAT) {

    private var ticks = 0
    val BoostAmount = FloatValue("BoostTimer", 10f, 1f, 50f)
    val ChargeAmount = FloatValue("ChargeTimer", 0.11f, 0.05f, 1f)

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (event.targetEntity is EntityLivingBase && ticks == 0) {
            ticks = 10
        }
    }

    override fun onEnable() {
        mc.timer.timerSpeed = 1f
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }


    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (ticks == 10) {
            mc.timer.timerSpeed = ChargeAmount.get()
            ticks --
        } else if (ticks > 1) {
            mc.timer.timerSpeed = BoostAmount.get()
            ticks --
        } else if (ticks == 1) {
            mc.timer.timerSpeed = 1f
            ticks --
        }
    }


}
