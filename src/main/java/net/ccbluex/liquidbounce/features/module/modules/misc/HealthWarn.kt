package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.IntegerValue
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType

@ModuleInfo(name = "HealthWarn", description = "Show warning when you during low health.", category = ModuleCategory.MISC, array = false)
class HealthWarn : Module() {
    private val healthValue = IntegerValue("Health", 7, 1, 20)
    private var canWarn = true

    override fun onEnable() {
        canWarn = true
    }
    
    override fun onDisable() {
        canWarn = true
    }
    
    @EventTarget
    fun onWorld(event: WorldEvent) {
        canWarn = true
    }

    @EventTarget
    fun onUpdate(event: MotionEvent){
        if(mc.thePlayer.health <= healthValue.get()){
            if(canWarn){
                LiquidBounce.hud.addNotification(
                    Notification("HP Warning", "YOU ARE AT LOW HP!", NotifyType.ERROR, 3000))
                canWarn = false
            }
        }else{
            canWarn = true
        }
    }
}
