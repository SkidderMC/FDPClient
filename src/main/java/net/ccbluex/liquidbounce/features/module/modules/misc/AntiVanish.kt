package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Alert
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.S13PacketDestroyEntities

@ModuleInfo(name = "AntiVanish", description = "Anti player vanish", category = ModuleCategory.MISC)
class AntiVanish : Module() {
    private var lastAlert=-1L
    private var vanishCount=0

    @EventTarget
    fun onWorld(event: WorldEvent){
        val clonedList = mutableListOf<Alert>()
        for(alert in LiquidBounce.hud.alerts){
            if(alert.title.contains("vanished",ignoreCase = true)) {
                clonedList.add(alert)
            }
        }
        for(alert in clonedList){
            LiquidBounce.hud.removeAlert(alert)
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent){
        if(event.packet is S13PacketDestroyEntities){
            var havePlayer=false
            for(id in event.packet.entityIDs){
                val entity=mc.theWorld.getEntityByID(id) ?: return
                if(entity is EntityPlayer && entity.getDistanceToEntity(mc.thePlayer)>7){
                    havePlayer=true
                }
            }

            if(havePlayer){
                if((System.currentTimeMillis()-lastAlert)>7000){
                    vanishCount=0
                    LiquidBounce.hud.addAlert(Alert("Vanished Warning","A player is vanished!", NotifyType.INFO,1500))
                }else{
                    vanishCount++
                }
                lastAlert=System.currentTimeMillis()

                if(vanishCount>=3){
                    vanishCount=0
                    LiquidBounce.hud.addAlert(Alert("Vanished Error","Something bad happened.", NotifyType.WARN,3000))
                }
            }
        }
    }
}