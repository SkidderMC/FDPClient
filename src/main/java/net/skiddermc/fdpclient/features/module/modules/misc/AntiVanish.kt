/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.features.module.modules.misc

import net.skiddermc.fdpclient.FDPClient
import net.skiddermc.fdpclient.event.EventTarget
import net.skiddermc.fdpclient.event.PacketEvent
import net.skiddermc.fdpclient.features.module.Module
import net.skiddermc.fdpclient.features.module.ModuleCategory
import net.skiddermc.fdpclient.features.module.ModuleInfo
import net.skiddermc.fdpclient.ui.client.hud.element.elements.Notification
import net.skiddermc.fdpclient.ui.client.hud.element.elements.NotifyType
import net.minecraft.network.play.server.S14PacketEntity
import net.minecraft.network.play.server.S1DPacketEntityEffect
import sun.audio.AudioPlayer.player

@ModuleInfo(name = "AntiVanish", category = ModuleCategory.MISC)
class AntiVanish : Module() {
    private var lastNotify=-1L

    @EventTarget
    fun onPacket(event: PacketEvent){
        if (mc.theWorld == null || mc.thePlayer == null) return
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
        if((System.currentTimeMillis()-lastNotify)>5000){
            FDPClient.hud.addNotification(Notification("Found a vanished entity!", player.name + " is vanished", NotifyType.WARNING, 4000, 500))

        }
        lastNotify=System.currentTimeMillis()

    }
}
