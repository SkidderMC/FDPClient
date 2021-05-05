package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.minecraft.network.play.server.S14PacketEntity
import net.minecraft.network.play.server.S1DPacketEntityEffect

@ModuleInfo(name = "AntiVanish", description = "Anti player vanish", category = ModuleCategory.MISC)
class AntiVanish : Module() {
    private var lastNotify=-1L

    @EventTarget
    fun onPacket(event: PacketEvent){
        if(event.packet is S1DPacketEntityEffect){
            if(mc.theWorld.getEntityByID(event.packet.entityId)==null){
                vanish()
            }
        }else if(event.packet is S14PacketEntity){
            if(event.packet.getEntity(mc.theWorld)==null){
                vanish()
            }
        }
    }

    private fun vanish(){
        if((System.currentTimeMillis()-lastNotify)>7000){
            LiquidBounce.hud.addNotification(Notification("Vanished Warning","A player is vanished!", NotifyType.WARNING,1500))
        }
        lastNotify=System.currentTimeMillis()

    }
}