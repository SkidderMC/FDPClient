package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.value.BoolValue

@ModuleInfo(name = "Target", description = "Target settings.", category = ModuleCategory.CLIENT, canEnable = false)
class Target : Module() {
    private val player=object : BoolValue("Player",true) {
        override fun onChanged(oldValue: Boolean, newValue: Boolean) {
            EntityUtils.targetPlayer=newValue
        }
    }

    private val animal=object : BoolValue("Animal",false) {
        override fun onChanged(oldValue: Boolean, newValue: Boolean) {
            EntityUtils.targetAnimals=newValue
        }
    }

    private val mob=object : BoolValue("Mob",true) {
        override fun onChanged(oldValue: Boolean, newValue: Boolean) {
            EntityUtils.targetMobs=newValue
        }
    }

    private val invisible=object : BoolValue("Invisible",false) {
        override fun onChanged(oldValue: Boolean, newValue: Boolean) {
            EntityUtils.targetInvisible=newValue
        }
    }

    private val dead=object : BoolValue("Dead",false) {
        override fun onChanged(oldValue: Boolean, newValue: Boolean) {
            EntityUtils.targetDead=newValue
        }
    }
}