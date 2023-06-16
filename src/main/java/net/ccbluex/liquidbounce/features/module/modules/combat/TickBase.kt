/*
 * Liquidlite Ghost Client
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.minecraft.entity.EntityLivingBase

object TickBase : Module(name = "TickBase", category = ModuleCategory.COMBAT) {
    
    private var ticks = 0

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (event.targetEntity is EntityLivingBase) {
            ticks = 10
        }
    }
    
    @EventTarget 
    fun onUpdate(event: UpdateEvent) {
        if (ticks == 10) {
            mc.timer.timerSpeed = 0.11F
            ticks --
        } else if (ticks > 1) {
            mc.timer.timerSpeed = 20f
            ticks --
        } else if (ticks == 1) {
            mc.timer.timerSpeed = 1f
            ticks -- 
        }
    }


}
