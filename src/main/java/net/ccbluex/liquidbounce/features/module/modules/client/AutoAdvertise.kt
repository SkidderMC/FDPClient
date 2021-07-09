package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import java.util.*

@ModuleInfo(name = "AutoAdvertise", description = "Can u keep enable this?We need ur support thx", category = ModuleCategory.CLIENT, array = false)
class AutoAdvertise : Module() {
    init {
        state=true
    }

    var waiting=false

    @EventTarget
    fun onWorld(event: WorldEvent){
        if(waiting) return

        Timer().schedule(object : TimerTask(){
            override fun run() {
                waiting=false
                if(mc.thePlayer!=null){
                    mc.thePlayer.sendChatMessage("["+ RandomUtils.randomString(3)+"] Try FDPClient! fdp.liulihaocai.pw ["+ RandomUtils.randomString(3)+"]")
                }
            }
        },2000L)
        waiting=true
    }
}