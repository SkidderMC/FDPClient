package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.PlayerUtils
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.S02PacketChat

@ModuleInfo(name = "AutoSpam", description = "Auto spam people you hit.", category = ModuleCategory.MISC)
class AutoSpam : Module() {
    private var target=""

    @EventTarget
    fun onAttack(event: AttackEvent){
        if(event.targetEntity is EntityPlayer){
            target=event.targetEntity.name
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent){
        if(target.isNotEmpty()){
            mc.thePlayer.sendChatMessage("/tell $target "+PlayerUtils.randomUnicode("YOU HAVE A TRASH CLIENT,TRY FDP!"))
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent){
        if (event.packet is S02PacketChat) {
            val message = event.packet.chatComponent.unformattedText
            if(message.contains("Mensagem para",ignoreCase = true)&&message.contains(target,ignoreCase = true)){
                event.cancelEvent()
                return
            }
            if(message.contains("Você ignorou este jogador",ignoreCase = true)||message.contains("Este usuário não está online",ignoreCase = true)){
                target=""
            }
        }
    }
}