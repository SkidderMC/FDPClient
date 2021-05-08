package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.BoolValue
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.EntityUtils

@ModuleInfo(name = "Target", description = "Target settings.", category = ModuleCategory.CLIENT, canEnable = false)
class Target : Module() {
    val player=object : BoolValue("Player", EntityUtils.targetPlayer) {
        override fun onChanged(oldValue: Boolean, newValue: Boolean) {
            EntityUtils.targetPlayer=newValue
        }
    }

    val animal=object : BoolValue("Animal",EntityUtils.targetAnimals) {
        override fun onChanged(oldValue: Boolean, newValue: Boolean) {
            EntityUtils.targetAnimals=newValue
        }
    }

    val mob=object : BoolValue("Mob",EntityUtils.targetMobs) {
        override fun onChanged(oldValue: Boolean, newValue: Boolean) {
            EntityUtils.targetMobs=newValue
        }
    }

    val invisible=object : BoolValue("Invisible",EntityUtils.targetInvisible) {
        override fun onChanged(oldValue: Boolean, newValue: Boolean) {
            EntityUtils.targetInvisible=newValue
        }
    }

    val dead=object : BoolValue("Dead",EntityUtils.targetDead) {
        override fun onChanged(oldValue: Boolean, newValue: Boolean) {
            EntityUtils.targetDead=newValue
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent){
        player.set(EntityUtils.targetPlayer)
        animal.set(EntityUtils.targetAnimals)
        mob.set(EntityUtils.targetMobs)
        invisible.set(EntityUtils.targetInvisible)
        dead.set(EntityUtils.targetDead)
    }

    //always handle event
    override fun handleEvents() = true
}