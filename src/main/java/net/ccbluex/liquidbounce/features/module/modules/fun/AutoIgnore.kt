package net.ccbluex.liquidbounce.features.module.modules.`fun`

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.network.play.server.S02PacketChat

@ModuleInfo(name = "AutoIgnore", description = "Auto ignore spammers(only redesky).", category = ModuleCategory.FUN)
class AutoIgnore : Module() {
    private val maxTimeValue=IntegerValue("MaxTimes",3,2,10)

    private val chatTimes=HashMap<String,Int>()
    private val timer=MSTimer()

    @EventTarget
    fun onUpdate(){
        if(timer.hasTimePassed(3000)) {
            for((name,time) in chatTimes){
                if(time>maxTimeValue.get()){
                    mc.thePlayer.sendChatMessage("/ignorar add $name")
                    chat("$name ignored for spamming...")
                }
            }
            timer.reset()
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent){
        if(event.packet is S02PacketChat){
            val msg=event.packet.chatComponent.unformattedText
            if(msg.contains("Mensagem de",ignoreCase = true)){
                val name=msg.split(":")[0].replace("Mensagem de ","")
                chatTimes[name] = chatTimes.getOrDefault(name,0)+1
            }
        }
    }
}