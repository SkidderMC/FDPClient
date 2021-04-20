package net.ccbluex.liquidbounce.features.special

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance

class CombatManager : Listenable,MinecraftInstance() {
    var inCombat=false
    private var lastAttack=-1L

    @EventTarget
    fun onUpdate(event: UpdateEvent){
        if(mc.thePlayer==null) return
        inCombat=false

        if((System.currentTimeMillis()-lastAttack)<3000){
            inCombat=true
            return
        }

        for(entity in mc.theWorld.loadedEntityList){
            if(entity.getDistanceToEntity(mc.thePlayer)<7 && EntityUtils.isSelected(entity,true)){
                inCombat=true
                break
            }
        }
    }

    @EventTarget
    fun onAttack(event: AttackEvent){
        lastAttack=System.currentTimeMillis()
    }

    override fun handleEvents(): Boolean {
        return true
    }
}