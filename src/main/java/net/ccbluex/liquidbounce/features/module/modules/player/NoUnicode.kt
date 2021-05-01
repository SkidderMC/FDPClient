package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.minecraft.network.play.server.S02PacketChat

@ModuleInfo(name = "NoUnicode", description = "change unicode chars into normal ones", category = ModuleCategory.PLAYER, array = false)
class NoUnicode : Module() {
    init {
        state = true
    }

    @EventTarget
    fun onPacket(event: PacketEvent){
        if(event.packet is S02PacketChat){
            val chat=event.packet.chatComponent.formattedText
            val sb = StringBuilder()

            for (char in chat.toCharArray()) {
                if(char.toInt() in (33+65248)..(128+65248)){
                    sb.append(Character.toChars(char.toInt() - 65248))
                }else{
                    sb.append(char)
                }
            }

            ClientUtils.displayChatMessage(sb.toString())
            event.cancelEvent()
        }
    }
}