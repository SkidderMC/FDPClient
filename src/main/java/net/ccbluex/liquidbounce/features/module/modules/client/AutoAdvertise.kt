/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import java.util.*

@ModuleInfo(name = "AutoAdvertise", category = ModuleCategory.CLIENT, array = false, defaultOn = true)
class AutoAdvertise : Module() {
    var waiting=false

    @EventTarget
    fun onWorld(event: WorldEvent){
        if(waiting) return

        Timer().schedule(object : TimerTask(){
            override fun run() {
                waiting=false
                if(mc.thePlayer!=null){
                    mc.thePlayer.sendChatMessage("["+ RandomUtils.randomString(3)+"] Try FDPClient! ${LiquidBounce.CLIENT_WEBSITE} ["+ RandomUtils.randomString(3)+"]")
                }
            }
        },2000L)
        waiting=true
    }
}
