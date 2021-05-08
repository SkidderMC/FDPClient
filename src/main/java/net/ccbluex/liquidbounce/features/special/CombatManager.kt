package net.ccbluex.liquidbounce.features.special

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.entity.EntityLivingBase

class CombatManager : Listenable,MinecraftInstance() {
    var inCombat=false
    private val lastAttackTimer=MSTimer()
    var target: EntityLivingBase? = null

    @EventTarget
    fun onUpdate(event: UpdateEvent){
        if(mc.thePlayer==null) return
        MovementUtils.updateBlocksPerSecond()

        inCombat=false

        if(!lastAttackTimer.hasTimePassed(1000)){
            inCombat=true
            return
        }

        for (entity in mc.theWorld.loadedEntityList) {
            if (entity is EntityLivingBase
                    && entity.getDistanceToEntity(mc.thePlayer) < 7 && EntityUtils.isSelected(entity, true)) {
                inCombat = true
                break
            }
        }

        if(target!=null){
            if(mc.thePlayer.getDistanceToEntity(target)>7||!inCombat||target!!.isDead){
                target=null
            }
        }
    }

    @EventTarget
    fun onAttack(event: AttackEvent){
        if(event.targetEntity is EntityLivingBase && EntityUtils.isSelected(event.targetEntity,true)){
            target=event.targetEntity
        }
        lastAttackTimer.reset()
    }

    override fun handleEvents(): Boolean {
        return true
    }
}