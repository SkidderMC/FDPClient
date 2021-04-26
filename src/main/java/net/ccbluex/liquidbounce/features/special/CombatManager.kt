package net.ccbluex.liquidbounce.features.special

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.timer.MSTimer

class CombatManager : Listenable,MinecraftInstance() {
    private var scanResult=false
    var inCombat=false
    private val lastAttackTimer=MSTimer()
    private val entityScanTimer=MSTimer()

    @EventTarget
    fun onUpdate(event: UpdateEvent){
        if(mc.thePlayer==null) return
        inCombat=false

        if(lastAttackTimer.hasTimePassed(1500)){
            inCombat=true
            return
        }

        if(entityScanTimer.hasTimePassed(500)) {
            scanResult=false
            for (entity in mc.theWorld.loadedEntityList) {
                if (entity.getDistanceToEntity(mc.thePlayer) < 7 && EntityUtils.isSelected(entity, true)) {
                    scanResult = true
                    break
                }
            }
        }

        if(scanResult){
            inCombat=true
        }
    }

    @EventTarget
    fun onAttack(event: AttackEvent){
        lastAttackTimer.reset()
    }

    override fun handleEvents(): Boolean {
        return true
    }
}