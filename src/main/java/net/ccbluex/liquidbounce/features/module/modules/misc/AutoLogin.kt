package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.TextValue
import net.minecraft.network.play.server.S45PacketTitle

@ModuleInfo(name = "AutoLogin", description = "Automatic login(only redesky).", category = ModuleCategory.MISC)
class AutoLogin : Module() {
    private val passwordValue=TextValue("Password","password")

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if(event.packet is S45PacketTitle && event.packet.type==S45PacketTitle.Type.SUBTITLE){
            val msg=event.packet.message.unformattedText
            val passwd=passwordValue.get()
            if(msg.contains("/register",ignoreCase = true)){
                mc.thePlayer.sendChatMessage("/register $passwd $passwd")
            }else if(msg.contains("/login",ignoreCase = true)){
                mc.thePlayer.sendChatMessage("/login $passwd")
            }
        }
    }
}