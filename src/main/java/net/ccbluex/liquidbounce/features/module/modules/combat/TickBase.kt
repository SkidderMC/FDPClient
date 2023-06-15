/*
 * Liquidlite Ghost Client
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.entity.EntityLivingBase

@ModuleInfo(name = "TimerBase", category = ModuleCategory.COMBAT)
class TimerBase : Module() {
    
    private var ticks = 0

    @EventTarget
    fun onAttack(event: AttackEvent) {
        
        if (event.targetEntity is EntityLivingBase) {
            val entity = event.targetEntity
            target = entity.entityId
            mc.timer.timerSpeed = 0.1F
            ticks = 10
        }
    }
    
    @EventTarget 
    fun onUpdate(event: UpdateEvent) {

        if (ticks > 1) {
            mc.timer.timerSpeed = 20f
            ticks --
        } else if (ticks == 1) {
            mc.timer.timerSpeed = 1f
            ticks -- 
        }
    }


}
