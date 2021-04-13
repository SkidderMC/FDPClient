package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Alert
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.value.IntegerValue

@ModuleInfo(name = "HealthWarn", description = "Show warning when you during low health.", category = ModuleCategory.FUN, array = false)
class HealthWarn : Module() {
    private val healthValue = IntegerValue("Health", 7, 1, 20)
    private var canWarn=true

    override fun onEnable() {
        canWarn=true
    }

    @EventTarget
    fun onRender2d(event: Render2DEvent){
        if(mc.thePlayer.health <= healthValue.get()){
            if(canWarn){
                LiquidBounce.hud.addAlert(Alert("HP Warning","YOU ARE AT LOW HP!",NotifyType.WARN,3000))
                canWarn=false
            }
        }else{
            canWarn=true
        }
    }
}